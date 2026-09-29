package com.flowforge.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end API tests: real security filter chain, services and Flyway schema on H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FlowForgeApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    // ---------------------------------------------------------------- helpers

    private String register(String name) throws Exception {
        String email = name.toLowerCase() + "-" + UUID.randomUUID() + "@example.com";
        String body = """
                {"name":"%s","email":"%s","password":"password123"}""".formatted(name, email);
        String res = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res).get("token").asText();
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder req, String token) {
        return req.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }

    private JsonNode createProject(String token, String name) throws Exception {
        String res = mvc.perform(auth(post("/api/projects"), token)
                        .content("{\"name\":\"" + name + "\",\"description\":\"d\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res);
    }

    private JsonNode createTask(String token, long projectId, String title) throws Exception {
        String res = mvc.perform(auth(post("/api/projects/" + projectId + "/tasks"), token)
                        .content("{\"title\":\"" + title + "\",\"priority\":\"HIGH\",\"dueDate\":\"2026-10-15\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res);
    }

    // ------------------------------------------------------------------ tests

    @Test
    void healthEndpointIsPublic() throws Exception {
        mvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("FlowForge backend is running!"));
    }

    @Test
    void protectedEndpointsRequireToken() throws Exception {
        mvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        mvc.perform(get("/api/projects").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerRejectsDuplicateEmailAndLoginWorks() throws Exception {
        String email = "dup-" + UUID.randomUUID() + "@example.com";
        String body = "{\"name\":\"Alex\",\"email\":\"" + email + "\",\"password\":\"password123\"}";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace(email, email.toUpperCase())))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void validationErrorsReturn400WithFieldMessages() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"nope\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").value("Email must be valid"))
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        String token = register("Val");
        mvc.perform(auth(post("/api/projects"), token).content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Project name cannot be empty"));
        mvc.perform(auth(post("/api/projects"), token).content("{\"name\":\"x\",\"status\":\"NOPE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void projectAndTaskLifecycleWithProgress() throws Exception {
        String token = register("Owner");
        long projectId = createProject(token, "Website Redesign").get("id").asLong();

        long t1 = createTask(token, projectId, "Create wireframes").get("id").asLong();
        createTask(token, projectId, "Build homepage");

        mvc.perform(auth(patch("/api/tasks/" + t1 + "/status"), token).content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        mvc.perform(auth(get("/api/projects/" + projectId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(2))
                .andExpect(jsonPath("$.taskCounts.DONE").value(1))
                .andExpect(jsonPath("$.progress").value(50));

        mvc.perform(auth(get("/api/projects/" + projectId + "/tasks?status=TODO"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Build homepage"));

        mvc.perform(auth(put("/api/projects/" + projectId), token)
                        .content("{\"name\":\"Website Redesign 2.0\",\"status\":\"ON_HOLD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Website Redesign 2.0"))
                .andExpect(jsonPath("$.status").value("ON_HOLD"));

        mvc.perform(auth(get("/api/dashboard"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(1))
                .andExpect(jsonPath("$.totalTasks").value(2))
                .andExpect(jsonPath("$.tasksByStatus.DONE").value(1));

        mvc.perform(auth(delete("/api/projects/" + projectId), token)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/projects/" + projectId), token)).andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/tasks/" + t1), token)).andExpect(status().isNotFound());
    }

    @Test
    void usersCannotSeeOrChangeOtherUsersProjectsOrTasks() throws Exception {
        String alice = register("Alice");
        String mallory = register("Mallory");
        long projectId = createProject(alice, "Alice's project").get("id").asLong();
        long taskId = createTask(alice, projectId, "Secret task").get("id").asLong();

        mvc.perform(auth(get("/api/projects"), mallory))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(auth(get("/api/projects/" + projectId), mallory)).andExpect(status().isNotFound());
        mvc.perform(auth(put("/api/projects/" + projectId), mallory).content("{\"name\":\"pwned\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(auth(delete("/api/projects/" + projectId), mallory)).andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/projects/" + projectId + "/tasks"), mallory)).andExpect(status().isNotFound());
        mvc.perform(auth(post("/api/projects/" + projectId + "/tasks"), mallory).content("{\"title\":\"x\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(auth(patch("/api/tasks/" + taskId + "/status"), mallory).content("{\"status\":\"DONE\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(auth(delete("/api/tasks/" + taskId), mallory)).andExpect(status().isNotFound());

        // Alice's data is untouched.
        mvc.perform(auth(get("/api/projects/" + projectId), alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice's project"))
                .andExpect(jsonPath("$.taskCounts.TODO").value(1));
    }
}
