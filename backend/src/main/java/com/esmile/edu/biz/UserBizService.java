package com.esmile.edu.biz;

import com.esmile.edu.common.auth.JwtService;
import com.esmile.edu.common.auth.PasswordService;
import com.esmile.edu.common.auth.RateLimitService;
import com.esmile.edu.common.auth.VerificationCodeService;
import com.esmile.edu.common.email.EmailService;
import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.user.CannotModifyAdminStatusException;
import com.esmile.edu.common.exception.user.EmailAlreadyExistsException;
import com.esmile.edu.common.exception.user.InvalidCredentialsException;
import com.esmile.edu.common.exception.user.InvalidVerificationCodeException;
import com.esmile.edu.common.exception.user.UserDisabledException;
import com.esmile.edu.common.exception.user.UserIsNotTeacherException;
import com.esmile.edu.common.exception.user.UserNotFoundException;
import com.esmile.edu.common.exception.user.UserPendingApprovalException;
import com.esmile.edu.dto.request.LoginRequest;
import com.esmile.edu.dto.request.RegisterRequest;
import com.esmile.edu.dto.response.AuthResponse;
import com.esmile.edu.dto.response.UserResponse;
import com.esmile.edu.module.user.Role;
import com.esmile.edu.module.user.UserEntity;
import com.esmile.edu.module.user.UserRepository;
import com.esmile.edu.module.user.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserBizService {
    private static final Logger log = LoggerFactory.getLogger(UserBizService.class);

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final VerificationCodeService verificationCodeService;
    private final RateLimitService rateLimitService;
    private final EmailService emailService;

    public UserBizService(
            UserRepository userRepository,
            PasswordService passwordService,
            JwtService jwtService,
            VerificationCodeService verificationCodeService,
            RateLimitService rateLimitService,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.verificationCodeService = verificationCodeService;
        this.rateLimitService = rateLimitService;
        this.emailService = emailService;
    }

    @Transactional
    public UserResponse registerStudent(RegisterRequest request) {
        return register(request, Role.STUDENT);
    }

    @Transactional
    public UserResponse registerTeacher(RegisterRequest request) {
        return register(request, Role.TEACHER);
    }

    private UserResponse register(RegisterRequest request, Role role) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }
        UserEntity user = new UserEntity(
            request.email(),
            passwordService.encode(request.password()),
            request.nickname(),
            role
        );
        return UserResponse.from(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new InvalidCredentialsException());
        if (!passwordService.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new UserDisabledException();
        }
        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            throw new UserPendingApprovalException();
        }
        String token = jwtService.generateToken(user.getId(), user.getRole().name());
        return new AuthResponse(token, jwtService.getExpiration(), UserResponse.from(user));
    }

    /**
     * Sends a verification code to the email with rate limiting.
     * Auto-creates user if not exists.
     */
    @Transactional
    public void sendCode(String email, Role role, String clientIp) {
        // Check rate limits
        rateLimitService.checkEmailRateLimit(email);
        rateLimitService.checkIpRateLimit(clientIp);

        // Generate and persist verification code
        String code = verificationCodeService.generateCode(email, role);

        // Send email
        emailService.sendVerificationCode(email, code);

        log.info("Verification code sent to {} ({}) via {}", email, role, emailService.getProviderName());
    }

    /**
     * Verifies the code and returns JWT token.
     * Auto-creates user with default nickname if not exists.
     */
    @Transactional
    public AuthResponse verifyCode(String email, String code, Role role) {
        if (!verificationCodeService.verifyCode(email, code)) {
            throw new InvalidVerificationCodeException();
        }

        UserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            // Auto-create user with default nickname
            String nickname = role == Role.TEACHER ? "Teacher" : "Student";
            UserEntity newUser = new UserEntity(
                email,
                passwordService.encode(""), // No password for verification code users
                nickname,
                role
            );
            return userRepository.save(newUser);
        });

        // Check user status - DISABLED users cannot verify
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new UserDisabledException();
        }
        // PENDING_APPROVAL is allowed for teachers (they get token but can't perform actions until approved)
        // Students should always be ACTIVE after verification
        if (role == Role.STUDENT && user.getStatus() != UserStatus.ACTIVE) {
            throw new UserPendingApprovalException();
        }

        String token = jwtService.generateToken(user.getId(), user.getRole().name());
        return new AuthResponse(token, jwtService.getExpiration(), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        UserEntity user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        return UserResponse.from(user);
    }

    @Transactional
    public void approveTeacher(Long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        if (user.getRole() != Role.TEACHER) {
            throw new UserIsNotTeacherException();
        }
        user.approve();
        userRepository.save(user);
    }

    @Transactional
    public void disableUser(Long userId, String reason) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        if (user.getRole() == Role.ADMIN) {
            throw new CannotModifyAdminStatusException();
        }
        user.disable(reason);
        userRepository.save(user);
    }

    @Transactional
    public void enableUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        user.enable();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Role role, UserStatus status, Pageable pageable) {
        Page<UserEntity> users;
        if (role != null && status != null) {
            users = userRepository.findByRoleAndStatus(role, status, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(UserResponse::from);
    }
}
