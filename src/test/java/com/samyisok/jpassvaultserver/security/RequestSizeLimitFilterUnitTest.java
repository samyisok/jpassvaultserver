package com.samyisok.jpassvaultserver.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.PrintWriter;
import com.samyisok.jpassvaultserver.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RequestSizeLimitFilterUnitTest {

  @Mock
  AppProperties appProperties;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Mock
  FilterChain chain;

  @Mock
  PrintWriter writer;

  RequestSizeLimitFilter filter;

  @BeforeEach
  void setUp() throws Exception {
    filter = new RequestSizeLimitFilter(appProperties);
    when(appProperties.getMaxPayloadSize()).thenReturn(100L);
    when(response.getWriter()).thenReturn(writer);
  }

  @Test
  void rejectsOversizedRequestWith413() throws Exception {
    when(request.getContentLengthLong()).thenReturn(101L);

    filter.doFilterInternal(request, response, chain);

    verify(response).setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
    verify(chain, never()).doFilter(any(), any());
  }

  @Test
  void allowsRequestWithinLimit() throws Exception {
    when(request.getContentLengthLong()).thenReturn(100L);

    filter.doFilterInternal(request, response, chain);

    verify(chain).doFilter(request, response);
    verify(response, never()).setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
  }

  @Test
  void rejectsBodyOfUnknownLengthWith411() throws Exception {
    when(request.getContentLengthLong()).thenReturn(-1L);
    when(request.getMethod()).thenReturn("POST");

    filter.doFilterInternal(request, response, chain);

    verify(response).setStatus(HttpServletResponse.SC_LENGTH_REQUIRED);
    verify(chain, never()).doFilter(any(), any());
  }

  @Test
  void allowsBodylessRequestWithoutContentLength() throws Exception {
    when(request.getContentLengthLong()).thenReturn(-1L);
    when(request.getMethod()).thenReturn("GET");

    filter.doFilterInternal(request, response, chain);

    verify(chain).doFilter(request, response);
  }
}
