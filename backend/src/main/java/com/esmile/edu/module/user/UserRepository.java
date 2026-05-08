package com.esmile.edu.module.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<UserEntity> findByRoleAndStatus(Role role, UserStatus status, Pageable pageable);
    @Query("SELECT u.id, u.nickname FROM UserEntity u WHERE u.id IN :ids")
    List<Object[]> findNicknamesByIds(@Param("ids") List<Long> ids);
}
