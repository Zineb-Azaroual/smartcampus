
package com.smartcampus.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String EMAIL = "etudiant@smartcampus.ma";
    private static final String PASSWORD = "Campus2026!";

    @Test
    void healthShouldReturnUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void loginShouldReturnToken() throws Exception {
        mockMvc.perform(post("/api/login")
                        .param("email", EMAIL)
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void loginWithWrongPasswordShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/login")
                        .param("email", EMAIL)
                        .param("password", "WrongPassword"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profileWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutShouldInvalidateToken() throws Exception {

        MvcResult result = mockMvc.perform(post("/api/login")
                        .param("email", EMAIL)
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        String token = response.get("token").asText();

        mockMvc.perform(post("/api/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
