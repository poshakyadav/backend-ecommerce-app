package com.ecommerce.bkd.config;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Getter;

@Configuration
@Getter
public class ShiprocketConfig {

    @Value("${shiprocket.api.base-url}")
    private String baseUrl;

    @Value("${shiprocket.api.email}")
    private String apiEmail;

    @Value("${shiprocket.api.password}")
    private String apiPassword;

    /** New: the Shiprocket channelCode you want to default to, e.g. "CUSTOM" **/
    @Value("${shiprocket.channel.default-code}")
    private String channelDefaultCode;

    @Value("${shiprocket.webhook.secret}")
    private String webhookSecret;

    @Value("#{'${shiprocket.whitelisted-ips}'.split(',')}")
    private Set<String> whitelistedIps;

    @Bean
    public WebClient shiprocketWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public RateLimiter shiprocketRateLimiter() {
        return RateLimiter.create(100.0); // 100 requests per second
    }
}
