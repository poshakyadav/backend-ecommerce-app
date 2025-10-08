package com.ecommerce.bkd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import com.ecommerce.bkd.filter.JwtAuthenticationFilter;
import com.ecommerce.bkd.services.impl.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final UserDetailsServiceImpl userDetailsServiceImp;

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        private final CustomLogoutHandler logoutHandler;

        public SecurityConfig(UserDetailsServiceImpl userDetailsServiceImp,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        CustomLogoutHandler logoutHandler) {
                this.userDetailsServiceImp = userDetailsServiceImp;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.logoutHandler = logoutHandler;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                return http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(Customizer.withDefaults())
                                .authorizeHttpRequests(
                                                req -> req.requestMatchers("/api/auth/login/**",
                                                                "/api/auth/register/**",
                                                                "/api/auth/refresh_token/**",

                                                                // "/api/auth/**",
                                                                "/v2/api-docs",
                                                                "/v3/api-docs",
                                                                "/v3/api-docs/**",
                                                                "/swagger-resources",
                                                                "/swagger-resources/**",
                                                                // "/configuration/ui",
                                                                // "/configuration/security",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/webjars/**",
                                                                "/api/products/**"
                                                                )
                                                                .permitAll()
                                                                .requestMatchers(HttpMethod.GET, "/api/content/**", "/api/admin/categories/**")
                                                                .permitAll() // Allow public access to /api/content GET
                                                                             // requests

                                                                // .requestMatchers("/admin_only/**").hasAuthority("ADMIN")
                                                                .anyRequest()
                                                                .authenticated())
                                .userDetailsService(userDetailsServiceImp)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .exceptionHandling(
                                                e -> e.accessDeniedHandler(
                                                                (request, response, accessDeniedException) -> response
                                                                                .setStatus(403))
                                                                .authenticationEntryPoint(new HttpStatusEntryPoint(
                                                                                HttpStatus.UNAUTHORIZED)))
                                .logout(l -> l
                                                .logoutUrl("/logout")
                                                .addLogoutHandler(logoutHandler)
                                                .logoutSuccessHandler((request, response,
                                                                authentication) -> SecurityContextHolder
                                                                                .clearContext()))
                                .build();

        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }

        @Bean
        public MultipartResolver multipartResolver() {
                return new StandardServletMultipartResolver();
        }
}
