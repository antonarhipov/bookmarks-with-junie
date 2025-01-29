package org.crud.bookmarks;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
public class LoggingConfigurationTest {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfigurationTest.class);

    @Test
    void testLoggingLevels() {
        logger.trace("Test TRACE message");
        logger.debug("Test DEBUG message");
        logger.info("Test INFO message");
        logger.warn("Test WARN message");
        logger.error("Test ERROR message");

        try {
            throw new RuntimeException("Test exception");
        } catch (Exception e) {
            logger.error("Test exception logging", e);
        }
    }
}