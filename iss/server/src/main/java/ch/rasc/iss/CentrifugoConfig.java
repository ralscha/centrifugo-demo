package ch.rasc.iss;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "centrifugo")
public record CentrifugoConfig(String apiBaseUrl, String apiKey, String hmacSecret) {
}
