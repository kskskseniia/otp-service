package org.example.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.example.config.AppConfig;
import org.example.model.Role;
import org.example.model.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Сервис для генерации и проверки JWT-токенов.
 */
public class JwtService {
    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final int expirationMinutes;

    public JwtService() {
        String secret = AppConfig.get("jwt.secret");
        this.expirationMinutes = AppConfig.getInt("jwt.expiration.minutes");

        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm)
                .withIssuer("otp-service")
                .build();
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        return JWT.create()
                .withIssuer("otp-service")
                .withSubject(user.getId().toString())
                .withClaim("username", user.getUsername())
                .withClaim("role", user.getRole().name())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiresAt))
                .sign(algorithm);
    }

    public AuthContext validateToken(String token) {
        DecodedJWT decodedJWT = verifier.verify(token);

        Long userId = Long.valueOf(decodedJWT.getSubject());
        String username = decodedJWT.getClaim("username").asString();
        Role role = Role.valueOf(decodedJWT.getClaim("role").asString());

        return new AuthContext(userId, username, role);
    }
}