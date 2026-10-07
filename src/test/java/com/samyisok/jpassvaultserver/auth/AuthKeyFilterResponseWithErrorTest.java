package com.samyisok.jpassvaultserver.auth;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest
class AuthKeyFilterResponseWithErrorTest {

  @MockitoSpyBean
  AuthKeyFilter authKeyFilter;

  @Mock
  HttpServletResponse response;

  @Mock
  PrintWriter printWriter;

  Method method;

  @BeforeEach
  public void setUp() throws Exception {
    when(response.getWriter()).thenReturn(printWriter);

    method = AuthKeyFilter.class.getDeclaredMethod("responseWithError",
        ServletResponse.class);

    method.setAccessible(true);
  }

  @Test
  void shouldCallReset() throws Exception {
    assertDoesNotThrow(() -> method.invoke(authKeyFilter, response));
    verify(response, times(1)).reset();
  }

  @Test
  void shouldSetStatusWithParams() throws Exception {
    assertDoesNotThrow(() -> method.invoke(authKeyFilter, response));
    verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  void shouldSetContentLengthWithParams() throws Exception {
    assertDoesNotThrow(() -> method.invoke(authKeyFilter, response));
    verify(response, times(1)).setContentLength(AuthKeyFilter.ERROR.length());
  }

  @Test
  void shouldGetWriter() throws Exception {
    assertDoesNotThrow(() -> method.invoke(authKeyFilter, response));
    verify(response, times(1)).getWriter();
  }

  @Test
  void shouldWriteWithParams() throws Exception {
    assertDoesNotThrow(() -> method.invoke(authKeyFilter, response));
    verify(printWriter, times(1)).write(AuthKeyFilter.ERROR);
  }
}
