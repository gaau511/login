package dev.gaau.login.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.time.Instant;
import java.util.Optional;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final String issuer;
    private final Long accessTokenExpTime;
    private final Long refreshTokenExpTime;

    public JwtUtil(@Value("${jwt.secret-key}") String key,
                   @Value("${jwt.issuer}") String issuer,
                   @Value("${jwt.expiration-time.access}") Long accessTokenExpTime,
                   @Value("${jwt.expiration-time.refresh}") Long refreshTokenExpTime) {
        byte[] decodedKey = Decoders.BASE64.decode(key);
        this.secretKey = Keys.hmacShaKeyFor(decodedKey);
        this.issuer = issuer;
        this.accessTokenExpTime = accessTokenExpTime;
        this.refreshTokenExpTime = refreshTokenExpTime;
    }

    public String createAccessToken(Long memberId, String audience) {

        String accessToken = Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(accessTokenExpTime)))

                .signWith(secretKey)
                .compact();

        return accessToken;
    }

    public String createRefreshToken(Long memberId, String audience) {

        String refreshToken = Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(refreshTokenExpTime)))

                .signWith(secretKey)
                .compact();

        return refreshToken;
    }

    public Optional<Claims> resolveToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token).getPayload();
            return Optional.of(claims);
        } catch (IllegalArgumentException | JwtException e) {
            return Optional.empty();
        }
    }

    public Boolean isValidToken(String token) {
        if (resolveToken(token).isEmpty())
            return false;

        return true;
    }

}
