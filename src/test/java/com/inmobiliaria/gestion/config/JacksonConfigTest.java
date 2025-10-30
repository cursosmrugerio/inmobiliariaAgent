package com.inmobiliaria.gestion.config;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.gestion.persona.domain.PersonaTipo;
import com.inmobiliaria.gestion.persona.dto.PersonaResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JacksonConfigTest {

  @Autowired private ObjectMapper objectMapper;

  @Test
  void testObjectMapperCanSerializeLocalDateTime() throws Exception {
    // Given - a PersonaResponse with LocalDateTime
    PersonaResponse persona =
        new PersonaResponse(
            1L,
            PersonaTipo.FISICA,
            "Juan",
            "Pérez",
            null,
            "PEXJ800101XXX",
            "PEXJ800101HDFRNN01",
            "juan@test.com",
            "555-1234",
            LocalDateTime.of(2024, 1, 15, 10, 30),
            true);

    // When - wrapping in a Map and converting to JSON (simulating FunctionTool.call behavior)
    Map<String, Object> result = new HashMap<>();
    result.put("personas", List.of(persona));

    // Then - convertValue should not throw IllegalArgumentException about LocalDateTime
    assertDoesNotThrow(
        () -> {
          Map<String, Object> converted = objectMapper.convertValue(result, Map.class);
          assertNotNull(converted);
        },
        "ObjectMapper should handle LocalDateTime with JSR-310 module");

    // Also verify serialization to JSON string works
    String json = objectMapper.writeValueAsString(persona);
    assertNotNull(json);
    assertTrue(json.contains("2024-01-15"));
    assertTrue(json.contains("Juan"));
  }

  @Test
  void testObjectMapperDeserializeLocalDateTime() throws Exception {
    // Given
    String json =
        "{"
            + "\"id\": 1,"
            + "\"tipoPersona\": \"FISICA\","
            + "\"nombre\": \"Juan\","
            + "\"apellidos\": \"Pérez\","
            + "\"razonSocial\": null,"
            + "\"rfc\": \"PEXJ800101XXX\","
            + "\"curp\": \"PEXJ800101HDFRNN01\","
            + "\"email\": \"juan@test.com\","
            + "\"telefono\": \"555-1234\","
            + "\"fechaAlta\": \"2024-01-15T10:30:00\","
            + "\"activo\": true"
            + "}";

    // When
    PersonaResponse persona = objectMapper.readValue(json, PersonaResponse.class);

    // Then
    assertNotNull(persona);
    assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30), persona.getFechaAlta());
    assertEquals("Juan", persona.getNombre());
  }
}
