package com.samyisok.jpassvaultserver.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import com.samyisok.jpassvaultserver.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthKeyFilterLogIpUnitTest {

  @Mock
  AuthCheck authCheck;

  @Mock
  AuthThrottle authThrottle;

  @Mock
  AppProperties appProperties;

  @Mock
  HttpServletRequest request;

  AuthKeyFilter authKeyFilter;

  private String remoteAddress = "10.0.0.5";
  private String forwardedAddress = "203.0.113.7";

  @BeforeEach
  void setUp() {
    authKeyFilter = new AuthKeyFilter(authCheck, authThrottle, appProperties);
    when(request.getRemoteAddr()).thenReturn(remoteAddress);
  }

  @Test
  void ignoresForgedForwardedHeaderWhenTrustDisabled() {
    when(request.getHeader(AuthKeyFilter.HEADER)).thenReturn(forwardedAddress);

    String address = authKeyFilter.resolveClientAddress(request, false);

    assertEquals(remoteAddress, address);
  }

  @Test
  void honorsForwardedHeaderWhenTrustEnabled() {
    when(request.getHeader(AuthKeyFilter.HEADER)).thenReturn(forwardedAddress);

    String address = authKeyFilter.resolveClientAddress(request, true);

    assertEquals(forwardedAddress, address);
  }

  @Test
  void takesRightMostForwardedValue() {
    when(request.getHeader(AuthKeyFilter.HEADER))
        .thenReturn("10.0.0.1, 10.0.0.2, " + forwardedAddress);

    String address = authKeyFilter.resolveClientAddress(request, true);

    assertEquals(forwardedAddress, address);
  }

  @Test
  void fallsBackToRemoteAddressWhenNoForwardedHeader() {
    when(request.getHeader(AuthKeyFilter.HEADER)).thenReturn(null);
    when(request.getHeader(AuthKeyFilter.FORWARDED_HEADER)).thenReturn(null);

    String address = authKeyFilter.resolveClientAddress(request, true);

    assertEquals(remoteAddress, address);
  }

  @Test
  void parsesStandardForwardedHeader() {
    when(request.getHeader(AuthKeyFilter.HEADER)).thenReturn(null);
    when(request.getHeader(AuthKeyFilter.FORWARDED_HEADER))
        .thenReturn("for=" + forwardedAddress + ";proto=https");

    String address = authKeyFilter.resolveClientAddress(request, true);

    assertEquals(forwardedAddress, address);
  }
}
