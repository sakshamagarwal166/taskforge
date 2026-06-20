package com.taskforge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskforge.auth.dto.AuthResponse;
import com.taskforge.auth.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TenantIsolationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tenantAToken;
    private String tenantBToken;

    @BeforeEach
    void setUp() throws Exception {
        tenantAToken = registerAndGetToken("acme-corp", "Acme Corp",
                "admin@acme.com", "password123", "Alice", "Admin");

        tenantBToken = registerAndGetToken("beta-inc", "Beta Inc",
                "admin@beta.com", "password123", "Bob", "Boss");
    }

    @Test
    void tenantA_cannotSee_tenantB_projects() throws Exception {
        String projectId = createProject(tenantAToken, "Acme Project");

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + tenantBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + tenantBToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void tenantB_cannotSee_tenantA_users() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + tenantAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + tenantBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void tenantA_cannotSee_tenantB_tasks() throws Exception {
        String projectId = createProject(tenantAToken, "Task Project");

        String taskBody = objectMapper.writeValueAsString(new CreateTaskBody("Fix login bug", null, null, null, null));
        mockMvc.perform(post("/api/projects/" + projectId + "/tasks")
                        .header("Authorization", "Bearer " + tenantAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/projects/" + projectId + "/tasks")
                        .header("Authorization", "Bearer " + tenantBToken))
                .andExpect(status().isNotFound());
    }

    private String registerAndGetToken(String slug, String name, String email,
                                        String password, String firstName, String lastName) throws Exception {
        RegisterRequest request = new RegisterRequest(slug, name, email, password, firstName, lastName);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        return response.accessToken();
    }

    private String createProject(String token, String name) throws Exception {
        String body = objectMapper.writeValueAsString(new CreateProjectBody(name, null));

        MvcResult result = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    record CreateProjectBody(String name, String description) {}
    record CreateTaskBody(String title, String description, String priority, String assigneeId, String dueDate) {}
}
