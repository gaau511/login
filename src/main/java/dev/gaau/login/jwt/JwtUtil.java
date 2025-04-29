package dev.gaau.login.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.time.Instant;

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
                .expiration(Date.from(Instant.now().plusMillis(accessTokenExpTime)))

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
                .expiration(Date.from(Instant.now().plusMillis(refreshTokenExpTime)))

                .signWith(secretKey)
                .compact();

        return refreshToken;
    }

    public Boolean validateToken(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();

        if (!validateExpirationTime(claims.getExpiration()))
            return false;

        return true;
    }

    public Boolean validateExpirationTime(Date exp) {
        Date now = Date.from(Instant.now());
        return exp.before(now);
    }

}
