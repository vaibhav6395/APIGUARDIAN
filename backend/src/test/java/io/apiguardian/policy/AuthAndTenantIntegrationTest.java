package io.apiguardian.policy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
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
class AuthAndTenantIntegrationTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper mapper;

 @Test
 void registrationLoginAndWorkspaceIsolationWork() throws Exception {
  String suffix=UUID.randomUUID().toString().substring(0,8);
  String email="owner-"+suffix+"@example.test";
  String registration=mapper.writeValueAsString(java.util.Map.of(
    "email",email,"password","A-Strong-Test-Password-2026","displayName","Test Owner","workspaceName","Test Workspace"));
  String ownerResponse=mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(registration))
    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  JsonNode owner=mapper.readTree(ownerResponse);
  String ownerToken=owner.get("accessToken").asText();
  long workspaceId=owner.get("workspaceId").asLong();

  String projectJson=mvc.perform(post("/api/v1/workspaces/"+workspaceId+"/projects")
    .header("Authorization","Bearer "+ownerToken).contentType(MediaType.APPLICATION_JSON)
    .content("{\"name\":\"Payments API\",\"description\":\"Synthetic test project\"}"))
    .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Payments API"))
    .andReturn().getResponse().getContentAsString();
  long projectId=mapper.readTree(projectJson).get("id").asLong();

  mvc.perform(post("/api/v1/projects/"+projectId+"/policies")
    .header("Authorization","Bearer "+ownerToken).contentType(MediaType.APPLICATION_JSON)
    .content("{\"role\":\"USER\",\"resource\":\"admin:settings\",\"action\":\"READ\",\"expected\":\"DENY\"}"))
    .andExpect(status().isCreated()).andExpect(jsonPath("$.expected").value("DENY"));

  mvc.perform(post("/api/v1/projects/"+projectId+"/policies/evaluate")
    .header("Authorization","Bearer "+ownerToken).contentType(MediaType.APPLICATION_JSON)
    .content("{\"rule\":{\"role\":\"USER\",\"resource\":\"admin:settings\",\"action\":\"READ\",\"expected\":\"DENY\"},\"observed\":\"ALLOW\"}"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.matched").value(false));

  String findingsJson=mvc.perform(get("/api/v1/projects/"+projectId+"/findings")
    .header("Authorization","Bearer "+ownerToken))
    .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
    .andExpect(jsonPath("$[0].severity").value("CRITICAL"))
    .andReturn().getResponse().getContentAsString();
  long findingId=mapper.readTree(findingsJson).get(0).get("id").asLong();
  mvc.perform(patch("/api/v1/projects/"+projectId+"/findings/"+findingId)
    .header("Authorization","Bearer "+ownerToken).contentType(MediaType.APPLICATION_JSON)
    .content("{\"status\":\"RESOLVED\"}"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESOLVED"));

  String login=mapper.writeValueAsString(java.util.Map.of("email",email,"password","A-Strong-Test-Password-2026"));
  mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
    .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty());

  String otherEmail="other-"+suffix+"@example.test";
  String otherReg=mapper.writeValueAsString(java.util.Map.of(
    "email",otherEmail,"password","Another-Strong-Password-2026","displayName","Other User","workspaceName","Other Workspace"));
  String otherResponse=mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(otherReg))
    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  String otherToken=mapper.readTree(otherResponse).get("accessToken").asText();

  mvc.perform(get("/api/v1/workspaces/"+workspaceId+"/projects").header("Authorization","Bearer "+otherToken))
    .andExpect(status().isNotFound());
 }

 @Test
 void registrationRejectsWeakPasswords() throws Exception {
  mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
    .content("{\"email\":\"weak@example.test\",\"password\":\"short\",\"displayName\":\"Weak\",\"workspaceName\":\"Nope\"}"))
    .andExpect(status().isBadRequest());
 }
}
