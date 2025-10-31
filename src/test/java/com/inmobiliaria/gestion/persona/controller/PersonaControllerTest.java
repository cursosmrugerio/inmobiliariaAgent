package com.inmobiliaria.gestion.persona.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.gestion.persona.domain.Persona;
import com.inmobiliaria.gestion.persona.domain.PersonaTipo;
import com.inmobiliaria.gestion.persona.dto.CreatePersonaRequest;
import com.inmobiliaria.gestion.persona.dto.UpdatePersonaRequest;
import com.inmobiliaria.gestion.persona.repository.PersonaRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
class PersonaControllerTest {

  private static final LocalDateTime DEFAULT_FECHA = LocalDateTime.of(2024, 1, 1, 12, 30, 0);

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private PersonaRepository personaRepository;

  @Test
  @DisplayName("Debe crear una persona")
  void shouldCreatePersona() throws Exception {
    CreatePersonaRequest request =
        new CreatePersonaRequest(
            PersonaTipo.FISICA,
            "María",
            "Gómez",
            null,
            "GOMG850101AA1",
            "GOMG850101HDFRRS08",
            "maria@example.com",
            "555-1111",
            DEFAULT_FECHA,
            true);

    mockMvc
        .perform(
            post("/api/personas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.tipoPersona").value("FISICA"))
        .andExpect(jsonPath("$.nombre").value("María"))
        .andExpect(jsonPath("$.fechaAlta").value("2024-01-01T12:30:00"))
        .andExpect(jsonPath("$.activo").value(true));
  }

  @Test
  @DisplayName("Debe listar las personas registradas")
  void shouldListPersonas() throws Exception {
    personaRepository.save(buildPersona("Ana", PersonaTipo.FISICA));
    personaRepository.save(buildPersona("Comercial Delta", PersonaTipo.MORAL));

    mockMvc
        .perform(get("/api/personas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  @DisplayName("Debe obtener una persona por su id")
  void shouldGetPersonaById() throws Exception {
    Persona persona = personaRepository.save(buildPersona("Ana", PersonaTipo.FISICA));

    mockMvc
        .perform(get("/api/personas/{id}", persona.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(persona.getId()))
        .andExpect(jsonPath("$.nombre").value("Ana"));
  }

  @Test
  @DisplayName("Debe devolver 404 al consultar una persona inexistente")
  void shouldReturnNotFoundWhenPersonaMissing() throws Exception {
    mockMvc.perform(get("/api/personas/{id}", 999)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Debe actualizar una persona existente")
  void shouldUpdatePersona() throws Exception {
    Persona persona = personaRepository.save(buildPersona("Ana", PersonaTipo.FISICA));

    UpdatePersonaRequest request =
        new UpdatePersonaRequest(
            PersonaTipo.MORAL,
            "Servicios",
            null,
            "Servicios Globales",
            "SERG920101AA1",
            null,
            "servicios@example.com",
            "555-2222",
            DEFAULT_FECHA.plusDays(1),
            false);

    mockMvc
        .perform(
            put("/api/personas/{id}", persona.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tipoPersona").value("MORAL"))
        .andExpect(jsonPath("$.razonSocial").value("Servicios Globales"))
        .andExpect(jsonPath("$.activo").value(false));
  }

  @Test
  @DisplayName("Debe eliminar una persona")
  void shouldDeletePersona() throws Exception {
    Persona persona = personaRepository.save(buildPersona("Ana", PersonaTipo.FISICA));

    mockMvc
        .perform(delete("/api/personas/{id}", persona.getId()))
        .andExpect(status().isNoContent());

    assertThat(personaRepository.existsById(persona.getId())).isFalse();
  }

  @Test
  @DisplayName("Debe devolver 400 cuando falta un dato obligatorio")
  void shouldReturnBadRequestWhenMissingRequiredData() throws Exception {
    CreatePersonaRequest invalidRequest =
        new CreatePersonaRequest(
            null, "María", null, null, null, null, null, null, DEFAULT_FECHA, true);

    mockMvc
        .perform(
            post("/api/personas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  private Persona buildPersona(String nombre, PersonaTipo tipo) {
    Persona persona = new Persona();
    persona.setTipoPersona(tipo);
    persona.setNombre(nombre);
    persona.setFechaAlta(DEFAULT_FECHA);
    persona.setActivo(true);
    return persona;
  }
}
