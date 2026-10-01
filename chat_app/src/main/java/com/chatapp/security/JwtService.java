package com.chatapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expiration;
    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {
        if (secret == null || secret.isBlank()) throw new IllegalStateException("Set JWT_SECRET to a secure secret of at least 32 bytes.");
        byte[] bytes;
        try { bytes = Decoders.BASE64.decode(secret); } catch (RuntimeException ex) { bytes = secret.getBytes(StandardCharsets.UTF_8); }
        if (bytes.length < 32) throw new IllegalStateException("JWT_SECRET must provide at least 256 bits (32 bytes).");
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expiration = expiration;
    }
    public String generate(UserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.getUsername()).issuedAt(Date.from(now)).expiration(Date.from(now.plusMillis(expiration))).signWith(key).compact();
    }
    public String subject(String token) { return claims(token).getSubject(); }
    public boolean isValid(String token, UserDetails user) {
        try { Claims claims = claims(token); return claims.getSubject().equals(user.getUsername()) && claims.getExpiration().after(new Date()); }
        catch (RuntimeException ex) { return false; }
    }
    private Claims claims(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
}
