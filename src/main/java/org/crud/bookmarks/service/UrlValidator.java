package org.crud.bookmarks.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Component responsible for validating URLs by checking their accessibility.
 * Performs both format validation and HTTP accessibility check.
 */
@Component
public class UrlValidator {
    private static final Logger logger = LoggerFactory.getLogger(UrlValidator.class);
    private static final String USER_AGENT = "Bookmark-Manager/1.0";

    private final WebClient webClient;
    private final Duration timeout;

    /**
     * Creates a new URL validator with the specified timeout.
     *
     * @param webClientBuilder The WebClient.Builder to use for HTTP requests
     * @param timeoutSeconds The timeout in seconds for HTTP requests (default: 10)
     */
    public UrlValidator(WebClient.Builder webClientBuilder,
                       @Value("${bookmark.url.timeout-seconds:10}") int timeoutSeconds) {
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.webClient = webClientBuilder
                .defaultHeader("User-Agent", USER_AGENT)
                .build();
    }

    /**
     * Validates a URL by checking its format and accessibility.
     *
     * @param url The URL to validate
     * @throws InvalidUrlException if the URL is null, empty, malformed, or not accessible
     */
    public void validateUrl(String url) {
        logger.debug("Validating URL: {}", url);

        if (!StringUtils.hasText(url)) {
            throw new InvalidUrlException("URL cannot be null or empty");
        }

        // Validate URL format
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            logger.error("Invalid URL format for {}: {}", url, e.getMessage());
            throw new InvalidUrlException("Invalid URL format: " + e.getMessage(), e);
        }

        // Validate URL accessibility
        try {
            webClient.get()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(timeout)
                    .block();
            logger.debug("URL validation successful for: {}", url);
        } catch (WebClientResponseException e) {
            logger.error("URL validation failed for {}: {} - {}", url, e.getStatusCode(), e.getMessage());
            throw new InvalidUrlException("Resource not accessible (HTTP " + e.getStatusCode() + ")", e);
        } catch (Exception e) {
            logger.error("URL validation failed for {}: {}", url, e.getMessage());
            throw new InvalidUrlException("Failed to access URL: " + e.getMessage(), e);
        }
    }
}
