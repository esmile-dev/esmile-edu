package com.esmile.edu.api;

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
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAndListCourse() throws Exception {
        // 先创建一个课程
        String createJson = """
            {
                "title": "Java 基础",
                "description": "Java 编程入门课程",
                "cover": "https://example.com/cover.jpg"
            }
            """;

        mockMvc.perform(post("/api/v1/teacher/courses")
                .header("X-User-Id", "1")
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
