package com.samyisok.jpassvaultserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
class AppPropertiesTest {

	@SpyBean
	AppProperties appProperties;

	@BeforeEach
	void setUp() {
		appProperties.setSecretKey("secretKey");
		appProperties.setUseSecretKeyFromEnv(true);
	}

	@Test
	void shouldGetSecretKey() {
		String secretKey = appProperties.getSecretKey();
		assertEquals(secretKey, "secretKey");
	}

	@Test
	void shouldGetUseSecretKeyFromEnv() {
		assertTrue(appProperties.getUseSecretKeyFromEnv());
	}

	@Test
	void shouldGetObjectDumpFromToString() {
		String dump = appProperties.toString();

		assertEquals(dump, "AppProperties [secretKey=secretKey, useSecretKeyFromEnv=true]");
	}

}
