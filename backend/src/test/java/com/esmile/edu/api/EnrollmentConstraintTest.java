package com.esmile.edu.api;

import com.esmile.edu.common.auth.VerificationCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EnrollmentConstraintTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VerificationCodeService verificationCodeService;

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
        mockMvc.perform(post("/api/v1/teacher/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + teacherEmail + "\", \"code\": \"" + teacherCode + "\"}"))
            .andExpect(status().isOk());

        // Create course
        String courseJson = """
            {
                "title": "Unique Course",
                "description": "Test",
                "cover": "http://test.com/cover.jpg"
            }
            """;
        mockMvc.perform(post("/api/v1/teacher/courses")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(courseJson))
            .andExpect(status().isOk());

        // Publish course
        mockMvc.perform(put("/api/v1/teacher/courses/1/publish")
                .header("X-User-Id", "1"))
            .andExpect(status().isOk());

        // Register student
        mockMvc.perform(post("/api/v1/student/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + studentEmail + "\"}"))
            .andExpect(status().isOk());

        String studentCode = verificationCodeService.getStoredCode(studentEmail);
        mockMvc.perform(post("/api/v1/student/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + studentEmail + "\", \"code\": \"" + studentCode + "\"}"))
            .andExpect(status().isOk());

        // Enrollment should succeed
        mockMvc.perform(post("/api/v1/student/courses/1/enroll")
                .header("X-User-Id", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(201));
    }
}
