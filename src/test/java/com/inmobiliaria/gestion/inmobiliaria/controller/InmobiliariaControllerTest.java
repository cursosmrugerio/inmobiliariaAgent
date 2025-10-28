package com.inmobiliaria.gestion.inmobiliaria.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.gestion.inmobiliaria.domain.Inmobiliaria;
import com.inmobiliaria.gestion.inmobiliaria.dto.CreateInmobiliariaRequest;
import com.inmobiliaria.gestion.inmobiliaria.dto.UpdateInmobiliariaRequest;
import com.inmobiliaria.gestion.inmobiliaria.repository.InmobiliariaRepository;
import java.util.List;
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
class InmobiliariaControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private InmobiliariaRepository inmobiliariaRepository;

  @Test
  @DisplayName("Debe crear una inmobiliaria")
  void shouldCreateInmobiliaria() throws Exception {
    CreateInmobiliariaRequest request =
        new CreateInmobiliariaRequest(
            "Inmo Norte", "INO930101AA1", "Luis Perez", "luis@inmo.mx", "555-0000");

    mockMvc
        .perform(
            post("/inmobiliarias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.nombre").value("Inmo Norte"))
        .andExpect(jsonPath("$.rfc").value("INO930101AA1"))
        .andExpect(jsonPath("$.nombreContacto").value("Luis Perez"))
        .andExpect(jsonPath("$.correo").value("luis@inmo.mx"))
        .andExpect(jsonPath("$.telefono").value("555-0000"));
  }

  @Test
  @DisplayName("Debe listar las inmobiliarias registradas")
  void shouldListInmobiliarias() throws Exception {
    Inmobiliaria first = new Inmobiliaria();
    first.setNombre("Primera");
    Inmobiliaria second = new Inmobiliaria();
    second.setNombre("Segunda");
    inmobiliariaRepository.saveAll(List.of(first, second));

    mockMvc
        .perform(get("/inmobiliarias"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  @DisplayName("Debe obtener una inmobiliaria por su id")
  void shouldGetInmobiliariaById() throws Exception {
    Inmobiliaria entity = new Inmobiliaria();
    entity.setNombre("Objetivo");
    entity = inmobiliariaRepository.save(entity);

    mockMvc
        .perform(get("/inmobiliarias/{id}", entity.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(entity.getId()))
        .andExpect(jsonPath("$.nombre").value("Objetivo"));
  }

  @Test
  @DisplayName("Debe actualizar los datos de una inmobiliaria existente")
  void shouldUpdateInmobiliaria() throws Exception {
    Inmobiliaria entity = new Inmobiliaria();
    entity.setNombre("Original");
    entity = inmobiliariaRepository.save(entity);

    UpdateInmobiliariaRequest request =
        new UpdateInmobiliariaRequest(
            "Actualizado", "ACT920101AA1", "Laura Ruiz", "laura@inmo.mx", "55-9999");

    mockMvc
        .perform(
            put("/inmobiliarias/{id}", entity.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(entity.getId()))
        .andExpect(jsonPath("$.nombre").value("Actualizado"))
        .andExpect(jsonPath("$.rfc").value("ACT920101AA1"))
        .andExpect(jsonPath("$.nombreContacto").value("Laura Ruiz"))
        .andExpect(jsonPath("$.correo").value("laura@inmo.mx"))
        .andExpect(jsonPath("$.telefono").value("55-9999"));
  }

  @Test
  @DisplayName("Debe eliminar una inmobiliaria")
  void shouldDeleteInmobiliaria() throws Exception {
    Inmobiliaria entity = new Inmobiliaria();
    entity.setNombre("Eliminar");
    entity = inmobiliariaRepository.save(entity);

    mockMvc
        .perform(delete("/inmobiliarias/{id}", entity.getId()))
        .andExpect(status().isNoContent());

    assertThat(inmobiliariaRepository.existsById(entity.getId())).isFalse();
  }
}
