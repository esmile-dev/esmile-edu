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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Test
    void studentRegister() throws Exception {
        String email = "student_" + System.currentTimeMillis() + "@test.com";

        mockMvc.perform(post("/api/v1/student/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\"}"))
            .andExpect(status().isOk());

        String actualCode = verificationCodeService.getStoredCode(email);
        mockMvc.perform(post("/api/v1/student/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\", \"code\": \"" + actualCode + "\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void duplicateEmailShouldFail() throws Exception {
        String email = "duplicate_" + System.currentTimeMillis() + "@test.com";

        mockMvc.perform(post("/api/v1/student/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\"}"))
            .andExpect(status().isOk());

        String actualCode = verificationCodeService.getStoredCode(email);
        mockMvc.perform(post("/api/v1/student/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\", \"code\": \"" + actualCode + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/student/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void teacherRegisterRequiresApproval() throws Exception {
        String email = "teacher_" + System.currentTimeMillis() + "@test.com";

        mockMvc.perform(post("/api/v1/teacher/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\"}"))
            .andExpect(status().isOk());

        String actualCode = verificationCodeService.getStoredCode(email);
        mockMvc.perform(post("/api/v1/teacher/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\", \"code\": \"" + actualCode + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.user.role").value("TEACHER"))
            .andExpect(jsonPath("$.data.user.status").value("PENDING_APPROVAL"));
    }
}
