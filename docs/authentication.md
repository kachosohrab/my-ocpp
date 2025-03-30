# Authentication in OCPP Server

## Overview
This document provides an overview of the authentication mechanism in the OCPP server, focusing on JWT-based authentication for both HTTP and WebSocket connections.

## Authentication Components
The authentication system consists of the following components:
- **JWT Utility (`JwtUtility`)**: Handles JWT token creation and validation.
- **Authentication Filter (`JwtAuthenticationFilter`)**: Verifies JWT tokens for HTTP requests.
- **WebSocket Authentication Interceptor (`WebSocketAuthInterceptor`)**: Ensures authentication for WebSocket connections.
- **Security Configuration (`SecurityConfig`)**: Configures Spring Security to enforce authentication on API endpoints.

## Generating a Test Token
For testing purposes, you can generate a JWT token using the following endpoint:
```bash
curl --location 'http://localhost:8080/api/generate-test-token?sub=testUser&expirySeconds=3600'
```
This will return a JWT token that can be used for authentication.

## HTTP Request Authentication
The server uses a JWT filter to authenticate API requests. The security configuration is defined as:
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```
### Validating JWT Token in HTTP Requests
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtility.isTokenValid(token)) {
                String subject = jwtUtility.extractSubject(token);
                UserDetails userDetails = new User(subject, "", Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities())
                );
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

## WebSocket Authentication
WebSocket connections are authenticated using an interceptor:
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ocppWebSocketHandler, "/ocpp")
                .setAllowedOrigins("*")
                .addInterceptors(webSocketAuthInterceptor);
    }
}
```
### Validating JWT Token in WebSocket Connections
```java
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                if (jwtUtility.isTokenValid(token)) {
                    attributes.put("sub", jwtUtility.extractSubject(token));
                    return true;
                }
            }
        }
        return false;
    }
}
```

## Token Validation
### Extracting Subject from Token
```java
public String extractSubject(String token) {
    return parseClaims(token).getSubject();
}
```
### Checking Token Expiry
```java
public boolean isTokenValid(String token) {
    try {
        return parseClaims(token).getExpiration().after(new Date());
    } catch (Exception e) {
        return false;
    }
}
```

## Conclusion
- API requests require a valid JWT token in the `Authorization` header.
- WebSocket connections also require a JWT token, verified using the `WebSocketAuthInterceptor`.
- This approach ensures secure authentication across both HTTP and WebSocket connections.

