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
class RedeemCourseStatusTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Test
    void redeemCourseStatusValidation() throws Exception {
        String teacherEmail = "teacher_stat_" + System.currentTimeMillis() + "@test.com";
        String studentEmail = "student_stat_" + System.currentTimeMillis() + "@test.com";

        // Create teacher
        mockMvc.perform(post("/api/v1/teacher/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + teacherEmail + "\"}"))
            .andExpect(status().isOk());

        String teacherCode = verificationCodeService.getStoredCode(teacherEmail);
        mockMvc.perform(post("/api/v1/teacher/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + teacherEmail + "\", \"code\": \"" + teacherCode + "\"}"))
            .andExpect(status().isOk());

        // Create a draft course (NOT published)
        String courseJson = """
            {
                "title": "Draft Course",
                "description": "Test",
                "cover": "http://test.com/cover.jpg"
            }
            """;
        mockMvc.perform(post("/api/v1/teacher/courses")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(courseJson))
            .andExpect(status().isOk());

        // Generate redeem code WITHOUT publishing
        String generateJson = """
            {
                "courseId": 1,
                "quantity": 1,
                "expiresAt": "2027-12-31T23:59:59"
            }
            """;
        mockMvc.perform(post("/api/v1/teacher/redeem-codes/generate")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateJson))
            .andExpect(status().isOk());

        // Create student
        mockMvc.perform(post("/api/v1/student/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + studentEmail + "\"}"))
            .andExpect(status().isOk());

        String studentCode = verificationCodeService.getStoredCode(studentEmail);
        mockMvc.perform(post("/api/v1/student/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + studentEmail + "\", \"code\": \"" + studentCode + "\"}"))
            .andExpect(status().isOk());
    }
}
