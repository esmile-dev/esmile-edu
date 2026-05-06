package com.esmile.edu.api;

import com.esmile.edu.common.email.MockEmailProvider;
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
class RedeemCourseStatusTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MockEmailProvider mockEmailProvider;

    @Autowired
    private UserRepository userRepository;

    @Test
    void redeemCourseStatusValidation() throws Exception {
        String teacherEmail = "teacher_stat_" + System.currentTimeMillis() + "@test.com";
        String studentEmail = "student_stat_" + System.currentTimeMillis() + "@test.com";

        // Create teacher
        mockMvc.perform(post("/api/v1/teacher/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + teacherEmail + "\"}"))
            .andExpect(status().isOk());

        String teacherCode = mockEmailProvider.getLastSentCode(teacherEmail);
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

        // Create a draft course (NOT published)
        String courseJson = """
            {
                "title": "Draft Course",
                "description": "Test",
                "cover": "http://test.com/cover.jpg"
            }
            """;
        mockMvc.perform(post("/api/v1/teacher/courses")
                .header("Authorization", "Bearer " + teacherToken)
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
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateJson))
            .andExpect(status().isOk());

        // Create student
        mockMvc.perform(post("/api/v1/student/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + studentEmail + "\"}"))
            .andExpect(status().isOk());

        String studentCode = mockEmailProvider.getLastSentCode(studentEmail);
        mockMvc.perform(post("/api/v1/student/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + studentEmail + "\", \"code\": \"" + studentCode + "\"}"))
            .andExpect(status().isOk());
    }
}
