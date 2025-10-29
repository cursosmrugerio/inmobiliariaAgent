package com.inmobiliaria.gestion.auth.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.gestion.auth.domain.UserAccount;
import com.inmobiliaria.gestion.exception.JwtValidationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
  private static final String HMAC_SHA256 = "HmacSHA256";

  private final byte[] secretKey;
  private final long expirationSeconds;
  private final ObjectMapper objectMapper;
  private final Clock clock;
  private final Base64.Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();
  private final Base64.Decoder urlDecoder = Base64.getUrlDecoder();

  public JwtUtil(
      @Value("${app.security.jwt.secret}") String secret,
      @Value("${app.security.jwt.expiration-seconds:3600}") long expirationSeconds,
      ObjectMapper objectMapper,
      Clock clock) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalArgumentException("La clave secreta JWT no puede estar vacía");
    }
    this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
    this.expirationSeconds = expirationSeconds;
    this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  public String generateToken(UserAccount userAccount) {
    Instant issuedAt = clock.instant();
    Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);

    Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
    Map<String, Object> payload =
        Map.of(
            "sub", userAccount.getEmail(),
            "uid", userAccount.getId(),
            "name", userAccount.getFullName(),
            "role", userAccount.getRole().name(),
            "iat", issuedAt.getEpochSecond(),
            "exp", expiresAt.getEpochSecond());

    try {
      String headerPart = urlEncoder.encodeToString(objectMapper.writeValueAsBytes(header));
      String payloadPart = urlEncoder.encodeToString(objectMapper.writeValueAsBytes(payload));
      String signature = sign(headerPart + "." + payloadPart);
      return headerPart + "." + payloadPart + "." + signature;
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Error serializando token JWT", ex);
    }
  }

  public JwtPayload validateToken(String token) {
    if (token == null || token.isBlank()) {
      throw new JwtValidationException("Token JWT ausente");
    }

    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      throw new JwtValidationException("Token JWT con formato inválido");
    }

    String headerPart = parts[0];
    String payloadPart = parts[1];
    String signaturePart = parts[2];

    verifySignature(headerPart, payloadPart, signaturePart);

    try {
      byte[] payloadBytes = urlDecoder.decode(payloadPart);
      JsonNode payload = objectMapper.readTree(payloadBytes);

      Instant expiresAt = readInstant(payload, "exp");
      if (expiresAt.isBefore(clock.instant())) {
        throw new JwtValidationException("El token JWT ha expirado");
      }

      Instant issuedAt = readInstant(payload, "iat");
      Long userId = readLong(payload, "uid");
      String email = readText(payload, "sub");
      String fullName = readText(payload, "name");
      String role = readText(payload, "role");

      return new JwtPayload(userId, email, fullName, role, issuedAt, expiresAt);
    } catch (IllegalArgumentException ex) {
      throw new JwtValidationException("Token JWT mal codificado", ex);
    } catch (IOException ex) {
      throw new JwtValidationException("No se pudo leer el contenido del token JWT", ex);
    }
  }

  private void verifySignature(String headerPart, String payloadPart, String signaturePart) {
    String expectedSignature = sign(headerPart + "." + payloadPart);
    if (!Objects.equals(expectedSignature, signaturePart)) {
      throw new JwtValidationException("Firma del token JWT inválida");
    }
  }

  private String sign(String content) {
    try {
      Mac mac = Mac.getInstance(HMAC_SHA256);
      mac.init(new SecretKeySpec(secretKey, HMAC_SHA256));
      byte[] signatureBytes = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
      return urlEncoder.encodeToString(signatureBytes);
    } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
      log.error("Error firmando token JWT", ex);
      throw new IllegalStateException("No fue posible firmar el token JWT", ex);
    }
  }

  private Long readLong(JsonNode payload, String fieldName) {
    if (!payload.hasNonNull(fieldName)) {
      throw new JwtValidationException("Campo " + fieldName + " ausente en el token");
    }
    return payload.get(fieldName).asLong();
  }

  private String readText(JsonNode payload, String fieldName) {
    if (!payload.hasNonNull(fieldName)) {
      throw new JwtValidationException("Campo " + fieldName + " ausente en el token");
    }
    return payload.get(fieldName).asText();
  }

  private Instant readInstant(JsonNode payload, String fieldName) {
    Long value = readLong(payload, fieldName);
    return Instant.ofEpochSecond(value);
  }
}
