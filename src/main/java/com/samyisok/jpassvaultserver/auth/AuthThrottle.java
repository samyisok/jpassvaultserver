package com.samyisok.jpassvaultserver.auth;

import java.time.Clock;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AuthThrottle {
  static final int DEFAULT_MAX_FAILURES = 5;
  static final Duration DEFAULT_WINDOW = Duration.ofMinutes(15);
  static final int MAX_TRACKED_SOURCES = 10_000;
  private static final String UNKNOWN_SOURCE = "unknown";

  private final int maxFailures;
  private final long windowMillis;
  private final Clock clock;
  private final Map<String, FailureState> failures;

  public AuthThrottle() {
    this(DEFAULT_MAX_FAILURES, DEFAULT_WINDOW, Clock.systemUTC());
  }

  AuthThrottle(int maxFailures, Duration window, Clock clock) {
    this.maxFailures = maxFailures;
    this.windowMillis = window.toMillis();
    this.clock = clock;
    this.failures = Collections.synchronizedMap(new LinkedHashMap<>() {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, FailureState> eldest) {
        return size() > MAX_TRACKED_SOURCES;
      }
    });
  }

  public boolean isBlocked(String source) {
    String key = normalize(source);
    long now = now();
    synchronized (failures) {
      FailureState state = failures.get(key);
      if (state == null) {
        return false;
      }
      if (isExpired(state, now)) {
        failures.remove(key);
        return false;
      }
      return state.failures >= maxFailures;
    }
  }

  public void recordFailure(String source) {
    String key = normalize(source);
    long now = now();
    synchronized (failures) {
      FailureState state = failures.get(key);
      if (state == null || isExpired(state, now)) {
        failures.put(key, new FailureState(1, now));
      } else {
        state.failures++;
      }
    }
  }

  public void reset(String source) {
    synchronized (failures) {
      failures.remove(normalize(source));
    }
  }

  private boolean isExpired(FailureState state, long now) {
    return now - state.firstFailureAt >= windowMillis;
  }

  private long now() {
    return clock.millis();
  }

  private static String normalize(String source) {
    return source == null || source.isBlank() ? UNKNOWN_SOURCE : source;
  }

  private static final class FailureState {
    private int failures;
    private final long firstFailureAt;

    private FailureState(int failures, long firstFailureAt) {
      this.failures = failures;
      this.firstFailureAt = firstFailureAt;
    }
  }
}
