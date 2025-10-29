package com.inmobiliaria.gestion.persona.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.inmobiliaria.gestion.exception.ResourceNotFoundException;
import com.inmobiliaria.gestion.persona.domain.Persona;
import com.inmobiliaria.gestion.persona.domain.PersonaTipo;
import com.inmobiliaria.gestion.persona.dto.CreatePersonaRequest;
import com.inmobiliaria.gestion.persona.dto.PersonaResponse;
import com.inmobiliaria.gestion.persona.dto.UpdatePersonaRequest;
import com.inmobiliaria.gestion.persona.repository.PersonaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonaServiceTest {

  @Mock private PersonaRepository personaRepository;

  @InjectMocks private PersonaService personaService;

  @Test
  @DisplayName("Debe crear una persona y devolver su representación")
  void shouldCreatePersona() {
    LocalDateTime now = LocalDateTime.now();
    CreatePersonaRequest request =
        new CreatePersonaRequest(
            PersonaTipo.FISICA,
            "María",
            "Gómez",
            null,
            "GOMM850101AA1",
            "GOMM850101HDFRRS08",
            "maria@example.com",
            "555-1111",
            now,
            true);

    Persona persisted = new Persona();
    persisted.setId(1L);
    persisted.setTipoPersona(PersonaTipo.FISICA);
    persisted.setNombre("María");
    persisted.setApellidos("Gómez");
    persisted.setRfc("GOMM850101AA1");
    persisted.setCurp("GOMM850101HDFRRS08");
    persisted.setEmail("maria@example.com");
    persisted.setTelefono("555-1111");
    persisted.setFechaAlta(now);
    persisted.setActivo(true);

    org.mockito.BDDMockito.given(personaRepository.save(any(Persona.class))).willReturn(persisted);

    PersonaResponse response = personaService.create(request);

    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getTipoPersona()).isEqualTo(PersonaTipo.FISICA);
    assertThat(response.isActivo()).isTrue();
    verify(personaRepository).save(any(Persona.class));
  }

  @Test
  @DisplayName("Debe actualizar los campos proporcionados")
  void shouldUpdatePersona() {
    Persona existing = buildPersona();
    existing.setId(5L);
    LocalDateTime newDate = LocalDateTime.now().minusDays(1);
    UpdatePersonaRequest request =
        new UpdatePersonaRequest(
            PersonaTipo.MORAL,
            "Servicios",
            null,
            "Servicios Delta",
            "DEL123456789",
            null,
            "servicios@delta.com",
            "555-2222",
            newDate,
            false);

    org.mockito.BDDMockito.given(personaRepository.findById(5L)).willReturn(Optional.of(existing));
    org.mockito.BDDMockito.given(personaRepository.save(any(Persona.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    PersonaResponse response = personaService.update(5L, request);

    assertThat(response.getTipoPersona()).isEqualTo(PersonaTipo.MORAL);
    assertThat(response.getNombre()).isEqualTo("Servicios");
    assertThat(response.getRazonSocial()).isEqualTo("Servicios Delta");
    assertThat(response.getFechaAlta()).isEqualTo(newDate);
    assertThat(response.isActivo()).isFalse();
  }

  @Test
  @DisplayName("Debe lanzar excepción si la persona no existe al actualizar")
  void shouldFailUpdatingWhenPersonaMissing() {
    UpdatePersonaRequest request =
        new UpdatePersonaRequest(null, null, null, null, null, null, null, null, null, null);
    org.mockito.BDDMockito.given(personaRepository.findById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> personaService.update(99L, request))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Persona con id 99 no encontrada");
  }

  @Test
  @DisplayName("Debe encontrar una persona por su identificador")
  void shouldFindPersonaById() {
    Persona existing = buildPersona();
    existing.setId(7L);
    org.mockito.BDDMockito.given(personaRepository.findById(7L)).willReturn(Optional.of(existing));

    PersonaResponse response = personaService.findById(7L);

    assertThat(response.getId()).isEqualTo(7L);
    assertThat(response.getNombre()).isEqualTo("Ana");
  }

  @Test
  @DisplayName("Debe lanzar excepción si la persona no existe al consultar")
  void shouldFailFindingPersonaById() {
    org.mockito.BDDMockito.given(personaRepository.findById(123L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> personaService.findById(123L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Persona con id 123 no encontrada");
  }

  @Test
  @DisplayName("Debe eliminar una persona existente")
  void shouldDeletePersona() {
    org.mockito.BDDMockito.given(personaRepository.existsById(3L)).willReturn(true);

    personaService.delete(3L);

    verify(personaRepository).deleteById(3L);
  }

  @Test
  @DisplayName("Debe lanzar excepción si la persona no existe al eliminar")
  void shouldFailDeletingPersonaMissing() {
    org.mockito.BDDMockito.given(personaRepository.existsById(4L)).willReturn(false);

    assertThatThrownBy(() -> personaService.delete(4L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Persona con id 4 no encontrada");
  }

  @Test
  @DisplayName("Debe listar todas las personas registradas")
  void shouldListPersonas() {
    Persona one = buildPersona();
    Persona two = buildPersona();
    two.setId(2L);
    two.setNombre("Lucía");
    org.mockito.BDDMockito.given(personaRepository.findAll()).willReturn(List.of(one, two));

    List<PersonaResponse> responses = personaService.findAll();

    assertThat(responses).hasSize(2);
  }

  private Persona buildPersona() {
    Persona persona = new Persona();
    persona.setTipoPersona(PersonaTipo.FISICA);
    persona.setNombre("Ana");
    persona.setApellidos("Ramírez");
    persona.setFechaAlta(LocalDateTime.now());
    persona.setActivo(true);
    return persona;
  }
}
