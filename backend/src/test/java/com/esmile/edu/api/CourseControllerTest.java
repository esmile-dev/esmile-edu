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
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MockEmailProvider mockEmailProvider;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createAndListCourse() throws Exception {
        // 创建教师用户
        String teacherEmail = "teacher_course_" + System.currentTimeMillis() + "@test.com";

        mockMvc.perform(post("/api/v1/teacher/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + teacherEmail + "\"}"))
            .andExpect(status().isOk());

        String teacherCode = mockEmailProvider.getLastSentCode(teacherEmail);
        MvcResult result = mockMvc.perform(post("/api/v1/teacher/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + teacherEmail + "\", \"code\": \"" + teacherCode + "\"}"))
            .andExpect(status().isOk())
            .andReturn();

        // 获取 token
        String token = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");

        // 教师创建后是PENDING_APPROVAL状态，需要直接设为ACTIVE才能创建课程
        userRepository.findByEmail(teacherEmail).ifPresent(user -> {
            user.setStatus(com.esmile.edu.module.user.UserStatus.ACTIVE);
            userRepository.saveAndFlush(user);
        });

        // 先创建一个课程
        String createJson = """
            {
                "title": "Java 基础",
                "description": "Java 编程入门课程",
                "cover": "https://example.com/cover.jpg"
            }
            """;

        mockMvc.perform(post("/api/v1/teacher/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(201))
            .andExpect(jsonPath("$.data.title").value("Java 基础"));
    }

    @Test
    void listPublishedCourses() throws Exception {
        mockMvc.perform(get("/api/v1/courses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
