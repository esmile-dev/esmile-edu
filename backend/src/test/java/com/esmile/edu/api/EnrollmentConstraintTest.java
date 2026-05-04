package com.esmile.edu.api;

import com.esmile.edu.common.auth.VerificationCodeService;
import com.esmile.edu.module.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EnrollmentConstraintTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void duplicateEnrollmentConstraint() throws Exception {
        String teacherEmail = "teacher_" + System.currentTimeMillis() + "@test.com";
        String studentEmail = "student_" + System.currentTimeMillis() + "@test.com";

        // Register teacher
        mockMvc.perform(post("/api/v1/teacher/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + teacherEmail + "\"}"))
            .andExpect(status().isOk());

        String teacherCode = verificationCodeService.getStoredCode(teacherEmail);
        MvcResult teacherResult = mockMvc.perform(post("/api/v1/teacher/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + teacherEmail + "\", \"code\": \"" + teacherCode + "\"}"))
            .andExpect(status().isOk())
            .andReturn();

        String teacherToken = com.jayway.jsonpath.JsonPath.read(teacherResult.getResponse().getContentAsString(), "$.data.token");

        // 教师创建后是PENDING_APPROVAL状态，需要设为ACTIVE才能创建课程
        userRepository.findByEmail(teacherEmail).ifPresent(user -> {
            user.setStatus(com.esmile.edu.module.user.UserStatus.ACTIVE);
            userRepository.saveAndFlush(user);
        });

        // Create course
        String courseJson = """
            {
                "title": "Unique Course",
                "description": "Test",
                "cover": "http://test.com/cover.jpg"
            }
            """;
        mockMvc.perform(post("/api/v1/teacher/courses")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(courseJson))
            .andExpect(status().isOk());

        // Publish course
        mockMvc.perform(put("/api/v1/teacher/courses/1/publish")
                .header("Authorization", "Bearer " + teacherToken))
            .andExpect(status().isOk());

        // Register student
        mockMvc.perform(post("/api/v1/student/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + studentEmail + "\"}"))
            .andExpect(status().isOk());

        String studentCode = verificationCodeService.getStoredCode(studentEmail);
        MvcResult studentResult = mockMvc.perform(post("/api/v1/student/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + studentEmail + "\", \"code\": \"" + studentCode + "\"}"))
            .andExpect(status().isOk())
            .andReturn();

        String studentToken = com.jayway.jsonpath.JsonPath.read(studentResult.getResponse().getContentAsString(), "$.data.token");

        // Enrollment should succeed
        mockMvc.perform(post("/api/v1/student/courses/1/enroll")
                .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(201));
    }
}
