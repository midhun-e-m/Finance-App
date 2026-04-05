package com.Fin.FinApp.controller;

import com.Fin.FinApp.dto.AuthRequestDTO;
import com.Fin.FinApp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // @MockBean was retired; @MockitoBean is the correct modern annotation
    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testDuplicateEmailRegistrationReturnsConflict() throws Exception {
        // Arrange
        String testEmail = "duplicate@example.com";
        when(userRepository.existsByEmail(testEmail)).thenReturn(true);

        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail(testEmail);
        request.setPassword("securePassword123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}