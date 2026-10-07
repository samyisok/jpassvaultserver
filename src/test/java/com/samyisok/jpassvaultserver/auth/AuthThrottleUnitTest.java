package com.samyisok.jpassvaultserver.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthThrottleUnitTest {

  private MutableClock clock;
  private AuthThrottle throttle;

  @BeforeEach
  void setUp() {
    clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    throttle = new AuthThrottle(3, Duration.ofMinutes(1), clock);
  }

  @Test
  void blocksSourceAfterThreshold() {
    for (int attempt = 0; attempt < 3; attempt++) {
      throttle.recordFailure("203.0.113.7");
    }

    assertTrue(throttle.isBlocked("203.0.113.7"));
    assertFalse(throttle.isBlocked("198.51.100.9"));
  }

  @Test
  void resetsThresholdAfterWindow() {
    for (int attempt = 0; attempt < 3; attempt++) {
      throttle.recordFailure("203.0.113.7");
    }
    assertTrue(throttle.isBlocked("203.0.113.7"));

    clock.advance(Duration.ofMinutes(2));

    assertFalse(throttle.isBlocked("203.0.113.7"));
  }

  @Test
  void resetClearsFailures() {
    throttle.recordFailure("203.0.113.7");
    throttle.reset("203.0.113.7");

    assertFalse(throttle.isBlocked("203.0.113.7"));
  }

  @Test
  void treatsNullSourceAsUnknown() {
    for (int attempt = 0; attempt < 3; attempt++) {
      throttle.recordFailure(null);
    }

    assertTrue(throttle.isBlocked(null));
  }

  private static final class MutableClock extends Clock {
    private Instant instant;

    private MutableClock(Instant instant) {
      this.instant = instant;
    }

    private void advance(Duration duration) {
      instant = instant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return instant;
    }
  }
}
