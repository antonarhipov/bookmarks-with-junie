package org.crud.bookmarks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BookmarksApplication {

    private static final Logger logger = LoggerFactory.getLogger(BookmarksApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BookmarksApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner loggerTest() {
//        return args -> {
//            logger.trace("This is a TRACE level message");
//            logger.debug("This is a DEBUG level message");
//            logger.info("This is an INFO level message");
//            logger.warn("This is a WARN level message");
//            logger.error("This is an ERROR level message");
//
//            // Test exception logging
//            try {
//                throw new RuntimeException("Test exception for logging");
//            } catch (Exception e) {
//                logger.error("Caught test exception", e);
//            }
//        };
//    }
}
