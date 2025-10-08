package com.ecommerce.bkd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer webMvcConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // .allowedOrigins("http://localhost:5173") // Specify the frontend origin
                        .allowedOrigins("*")
                        // .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Include OPTIONS for preflight
                        .allowedHeaders("*")
                        // .allowCredentials(true) // If you need cookies/auth headers
                        .maxAge(3600); // Cache preflight response for 1 hour
            }
        };
    }
}
