package com.samyisok.jpassvaultserver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.samyisok.jpassvaultserver.auth.AuthThrottle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"app-properties.max-payload-size=100"})
@AutoConfigureMockMvc
class SecurityFilterChainIntegrationTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  AuthThrottle authThrottle;

  private static final String TOKEN = "test-secret-0123456789";
  private static final String SOURCE = "127.0.0.1";

  @BeforeEach
  void resetThrottle() {
    authThrottle.reset(SOURCE);
  }

  @Test
  void validTokenReturnsCheckOk() throws Exception {
    mockMvc.perform(get("/check").header("token", TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.check").value("ok"));
  }

  @Test
  void missingTokenIsUnauthorizedWithDocumentedBody() throws Exception {
    mockMvc.perform(get("/check"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Invalid API KEY"))
        .andExpect(header().string("X-Content-Type-Options", "nosniff"))
        .andExpect(header().string("Cache-Control", "no-store"));
  }

  @Test
  void blankUploadIsBadRequest() throws Exception {
    mockMvc.perform(post("/files").header("token", TOKEN)
        .contentType(MediaType.APPLICATION_JSON).content("{\"file\":\"   \"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void oversizedUploadWithValidTokenIsPayloadTooLarge() throws Exception {
    String body = "{\"file\":\"" + "x".repeat(200) + "\"}";

    mockMvc.perform(post("/files").header("token", TOKEN)
        .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().is(413));
  }

  @Test
  void oversizedUploadWithInvalidTokenIsUnauthorized() throws Exception {
    String body = "{\"file\":\"" + "x".repeat(200) + "\"}";

    mockMvc.perform(post("/files").header("token", "wrong-token")
        .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void repeatedFailuresAreThrottled() throws Exception {
    for (int attempt = 0; attempt < 5; attempt++) {
      mockMvc.perform(get("/check").header("token", "wrong-token"))
          .andExpect(status().isUnauthorized());
    }

    mockMvc.perform(get("/check").header("token", "wrong-token"))
        .andExpect(status().is(429));
  }
}
