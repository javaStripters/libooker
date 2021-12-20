package ru.thecntgfy.libooker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.xml.crypto.Data;
import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtProvider {
    //Base64 encoded
    @Value("${app.jwtSecret}")
    private byte[] secret;

    @Value("${app.jwtExpirationInMs}")
    private int expirationInMs;

    protected Key getKey() {
        return Keys.hmacShaKeyFor(secret);
    }

    public String generateToken(Authentication authentication) {
        //TODO: No conversion?
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        //TODO: java.time API?
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getKey())
                .compact();
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
        } catch (Exception e) {
            log.debug("Token not valid: " + e);
            return false;
        }
        return true;
    }
}
