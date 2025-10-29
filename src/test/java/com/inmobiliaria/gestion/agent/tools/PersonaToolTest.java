package com.inmobiliaria.gestion.agent.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inmobiliaria.gestion.persona.domain.PersonaTipo;
import com.inmobiliaria.gestion.persona.dto.CreatePersonaRequest;
import com.inmobiliaria.gestion.persona.dto.PersonaResponse;
import com.inmobiliaria.gestion.persona.dto.UpdatePersonaRequest;
import com.inmobiliaria.gestion.persona.service.PersonaService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonaToolTest {

  @Mock private PersonaService personaService;

  @InjectMocks private PersonaTool personaTool;

  private PersonaResponse personaResponse;

  @BeforeEach
  void setUp() {
    personaResponse =
        new PersonaResponse(
            1L,
            PersonaTipo.FISICA,
            "María",
            "Gómez",
            null,
            "GOMM850101AA1",
            "GOMM850101HDFRRS08",
            "maria@example.com",
            "5551234",
            LocalDateTime.of(2024, 1, 1, 10, 0),
            true);
  }

  @Test
  @DisplayName("Debe listar todas las personas")
  void shouldListAllPersonas() {
    when(personaService.findAll()).thenReturn(List.of(personaResponse));

    Map<String, Object> result = personaTool.listAllPersonas();

    assertThat(result).containsEntry("success", true).containsEntry("count", 1);
    verify(personaService, times(1)).findAll();
  }

  @Test
  @DisplayName("Debe manejar errores al listar personas")
  void shouldHandleErrorListingPersonas() {
    when(personaService.findAll()).thenThrow(new RuntimeException("DB error"));

    Map<String, Object> result = personaTool.listAllPersonas();

    assertThat(result).containsEntry("success", false);
    assertThat(result.get("error").toString()).contains("Error listing personas");
  }

  @Test
  @DisplayName("Debe obtener una persona por id")
  void shouldGetPersonaById() {
    when(personaService.findById(1L)).thenReturn(personaResponse);

    Map<String, Object> result = personaTool.getPersonaById(1);

    assertThat(result).containsEntry("success", true);
    assertThat(result.get("persona")).isEqualTo(personaResponse);
    verify(personaService).findById(1L);
  }

  @Test
  @DisplayName("Debe manejar errores al obtener una persona")
  void shouldHandleErrorGettingPersona() {
    when(personaService.findById(99L)).thenThrow(new RuntimeException("Not found"));

    Map<String, Object> result = personaTool.getPersonaById(99);

    assertThat(result).containsEntry("success", false);
    assertThat(result.get("error").toString()).contains("Error retrieving persona");
  }

  @Test
  @DisplayName("Debe crear una persona")
  void shouldCreatePersona() {
    when(personaService.create(any(CreatePersonaRequest.class))).thenReturn(personaResponse);

    Map<String, Object> result =
        personaTool.createPersona(
            "FISICA",
            "María",
            "Gómez",
            null,
            "GOMM850101AA1",
            "GOMM850101HDFRRS08",
            "maria@example.com",
            "5551234",
            "2024-01-01T10:00:00",
            true);

    assertThat(result).containsEntry("success", true);
    assertThat(result.get("message")).isEqualTo("Persona created successfully");
    verify(personaService).create(any(CreatePersonaRequest.class));
  }

  @Test
  @DisplayName("Debe reportar error cuando los datos de creación son inválidos")
  void shouldReturnErrorWhenCreateFails() {
    when(personaService.create(any(CreatePersonaRequest.class)))
        .thenThrow(new IllegalArgumentException("Datos inválidos"));

    Map<String, Object> result =
        personaTool.createPersona(
            "FISICA",
            "María",
            "Gómez",
            null,
            "GOMM850101AA1",
            "GOMM850101HDFRRS08",
            "maria@example.com",
            "5551234",
            "2024-01-01T10:00:00",
            true);

    assertThat(result).containsEntry("success", false);
    assertThat(result.get("error").toString()).contains("Error creating persona");
  }

  @Test
  @DisplayName("Debe actualizar una persona de forma parcial")
  void shouldUpdatePersona() {
    when(personaService.update(any(Long.class), any(UpdatePersonaRequest.class)))
        .thenReturn(personaResponse);

    Map<String, Object> result =
        personaTool.updatePersona(
            1,
            "MORAL",
            null,
            null,
            "Servicios Delta",
            "DEL920101AA1",
            null,
            "contacto@delta.com",
            null,
            null,
            false);

    assertThat(result).containsEntry("success", true);
    assertThat(result.get("message")).isEqualTo("Persona updated successfully");
    verify(personaService).update(any(Long.class), any(UpdatePersonaRequest.class));
  }

  @Test
  @DisplayName("Debe manejar errores al actualizar una persona")
  void shouldHandleUpdateError() {
    when(personaService.update(any(Long.class), any(UpdatePersonaRequest.class)))
        .thenThrow(new RuntimeException("Error"));

    Map<String, Object> result =
        personaTool.updatePersona(1, null, null, null, null, null, null, null, null, null, null);

    assertThat(result).containsEntry("success", false);
    assertThat(result.get("error").toString()).contains("Error updating persona");
  }

  @Test
  @DisplayName("Debe eliminar una persona")
  void shouldDeletePersona() {
    doNothing().when(personaService).delete(1L);

    Map<String, Object> result = personaTool.deletePersona(1);

    assertThat(result).containsEntry("success", true);
    assertThat(result.get("message").toString()).contains("deleted successfully");
    verify(personaService).delete(1L);
  }

  @Test
  @DisplayName("Debe manejar errores al eliminar una persona")
  void shouldHandleDeleteError() {
    doThrow(new RuntimeException("No existe")).when(personaService).delete(99L);

    Map<String, Object> result = personaTool.deletePersona(99);

    assertThat(result).containsEntry("success", false);
    assertThat(result.get("error").toString()).contains("Error deleting persona");
  }
}
