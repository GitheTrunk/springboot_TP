package example.demo.Jwt;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private JwtProperties jwtProperties;
    private SecretKey secretKey;

    public JwtUtil(JwtProperties properties) {
        this.jwtProperties = properties;
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(properties.getSecret()));
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExp());
        return Jwts.builder().subject(username).issuedAt(new Date()).expiration(expiryDate).signWith(secretKey).compact();
    }
    
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }
}
