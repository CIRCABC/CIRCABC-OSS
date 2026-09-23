package com.sii.eucaptcha.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * @author mousab.aidoud
 * @version 1.0
 * Jwt token
 */
@Component
public class JwtToken {

  private SecretKey key;
  private final UUID id = UUID.randomUUID();

  /**
   * Generating Token
   *
   * @return jwtToken
   */
  public String generateJwtToken() {
    key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    return Jwts.builder().setSubject(id.toString()).signWith(key).compact();
  }

  /**
   * Verify token
   *
   * @param jwtString token string to validate
   * @return true|false
   */
  public Boolean verifyToken(String jwtString) {
    try {
      Jws<Claims> claims = decodeToken(jwtString);
      return id.toString().equals(claims.getBody().getSubject());
    } catch (JwtException ex) {
      return false;
    }
  }

  private Jws<Claims> decodeToken(String jwtString) {
    return Jwts.parserBuilder()
      .setSigningKey(key)
      .build()
      .parseClaimsJws(jwtString);
  }
}
