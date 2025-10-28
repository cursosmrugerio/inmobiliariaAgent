package com.inmobiliaria.gestion.propiedad.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.inmobiliaria.gestion.exception.ResourceNotFoundException;
import com.inmobiliaria.gestion.inmobiliaria.domain.Inmobiliaria;
import com.inmobiliaria.gestion.inmobiliaria.repository.InmobiliariaRepository;
import com.inmobiliaria.gestion.propiedad.domain.Propiedad;
import com.inmobiliaria.gestion.propiedad.domain.PropiedadTipo;
import com.inmobiliaria.gestion.propiedad.dto.CreatePropiedadRequest;
import com.inmobiliaria.gestion.propiedad.dto.PropiedadResponse;
import com.inmobiliaria.gestion.propiedad.dto.UpdatePropiedadRequest;
import com.inmobiliaria.gestion.propiedad.repository.PropiedadRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PropiedadServiceTest {

  @Mock private PropiedadRepository propiedadRepository;
  @Mock private InmobiliariaRepository inmobiliariaRepository;
  @InjectMocks private PropiedadService propiedadService;

  private Inmobiliaria inmobiliaria;

  @BeforeEach
  void setUp() {
    inmobiliaria = new Inmobiliaria();
    inmobiliaria.setId(10L);
    inmobiliaria.setNombre("Inmo Norte");
  }

  @Test
  @DisplayName("Debe crear una propiedad asociada a una inmobiliaria existente")
  void shouldCreatePropiedad() {
    CreatePropiedadRequest request =
        new CreatePropiedadRequest(
            "Residencia Las Palmas", PropiedadTipo.CASA, "Av. Central 123", "Con alberca", 10L);
    given(inmobiliariaRepository.findById(10L)).willReturn(Optional.of(inmobiliaria));
    given(propiedadRepository.save(any(Propiedad.class)))
        .willAnswer(
            invocation -> {
              Propiedad entidad = invocation.getArgument(0);
              entidad.setId(1L);
              return entidad;
            });

    PropiedadResponse response = propiedadService.create(request);

    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getNombre()).isEqualTo("Residencia Las Palmas");
    assertThat(response.getTipo()).isEqualTo(PropiedadTipo.CASA);
    assertThat(response.getInmobiliariaId()).isEqualTo(10L);
    verify(propiedadRepository).save(any(Propiedad.class));
  }

  @Test
  @DisplayName("Debe lanzar excepción si la inmobiliaria no existe al crear una propiedad")
  void shouldFailCreatingWhenInmobiliariaMissing() {
    CreatePropiedadRequest request =
        new CreatePropiedadRequest(
            "Residencia Las Palmas", PropiedadTipo.CASA, "Av. Central 123", "Con alberca", 99L);
    given(inmobiliariaRepository.findById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> propiedadService.create(request))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Inmobiliaria con id 99 no encontrada");
  }

  @Test
  @DisplayName("Debe actualizar los datos de una propiedad existente")
  void shouldUpdatePropiedad() {
    Propiedad existente = new Propiedad();
    existente.setId(5L);
    existente.setNombre("Anterior");
    existente.setTipo(PropiedadTipo.CASA);
    existente.setDireccion("Antigua 12");
    existente.setObservaciones("Sin observaciones");
    existente.setInmobiliaria(inmobiliaria);

    UpdatePropiedadRequest request =
        new UpdatePropiedadRequest(
            "Actualizada", PropiedadTipo.DEPARTAMENTO, "Nueva 100", "Remodelada", null);

    given(propiedadRepository.findById(5L)).willReturn(Optional.of(existente));
    given(propiedadRepository.save(any(Propiedad.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    PropiedadResponse response = propiedadService.update(5L, request);

    assertThat(response.getNombre()).isEqualTo("Actualizada");
    assertThat(response.getTipo()).isEqualTo(PropiedadTipo.DEPARTAMENTO);
    assertThat(response.getDireccion()).isEqualTo("Nueva 100");
    assertThat(response.getObservaciones()).isEqualTo("Remodelada");
    assertThat(response.getInmobiliariaId()).isEqualTo(10L);
  }

  @Test
  @DisplayName("Debe permitir reasignar la inmobiliaria de una propiedad existente")
  void shouldUpdateInmobiliariaOnPropiedad() {
    Propiedad existente = new Propiedad();
    existente.setId(6L);
    existente.setNombre("Departamento Centro");
    existente.setTipo(PropiedadTipo.DEPARTAMENTO);
    existente.setInmobiliaria(inmobiliaria);

    Inmobiliaria nuevaInmobiliaria = new Inmobiliaria();
    nuevaInmobiliaria.setId(20L);
    nuevaInmobiliaria.setNombre("Inmo Sur");

    UpdatePropiedadRequest request = new UpdatePropiedadRequest(null, null, null, null, 20L);

    given(propiedadRepository.findById(6L)).willReturn(Optional.of(existente));
    given(inmobiliariaRepository.findById(20L)).willReturn(Optional.of(nuevaInmobiliaria));
    given(propiedadRepository.save(any(Propiedad.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    PropiedadResponse response = propiedadService.update(6L, request);

    assertThat(response.getInmobiliariaId()).isEqualTo(20L);
    assertThat(response.getInmobiliariaNombre()).isEqualTo("Inmo Sur");
  }

  @Test
  @DisplayName("Debe lanzar excepción si la propiedad no existe al actualizar")
  void shouldFailUpdatingWhenPropiedadMissing() {
    UpdatePropiedadRequest request = new UpdatePropiedadRequest(null, null, null, null, null);
    given(propiedadRepository.findById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> propiedadService.update(99L, request))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Propiedad con id 99 no encontrada");
  }

  @Test
  @DisplayName("Debe lanzar excepción si la nueva inmobiliaria no existe al actualizar")
  void shouldFailUpdatingWhenNewInmobiliariaMissing() {
    Propiedad existente = new Propiedad();
    existente.setId(7L);
    existente.setNombre("Local Comercial");
    existente.setTipo(PropiedadTipo.LOCAL);
    existente.setInmobiliaria(inmobiliaria);

    UpdatePropiedadRequest request = new UpdatePropiedadRequest(null, null, null, null, 55L);

    given(propiedadRepository.findById(7L)).willReturn(Optional.of(existente));
    given(inmobiliariaRepository.findById(55L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> propiedadService.update(7L, request))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Inmobiliaria con id 55 no encontrada");
  }

  @Test
  @DisplayName("Debe obtener una propiedad por su identificador")
  void shouldFindPropiedadById() {
    Propiedad existente = new Propiedad();
    existente.setId(8L);
    existente.setNombre("Oficina Centro");
    existente.setTipo(PropiedadTipo.OFICINA);
    existente.setInmobiliaria(inmobiliaria);

    given(propiedadRepository.findById(8L)).willReturn(Optional.of(existente));

    PropiedadResponse response = propiedadService.findById(8L);

    assertThat(response.getNombre()).isEqualTo("Oficina Centro");
  }

  @Test
  @DisplayName("Debe lanzar excepción si la propiedad no existe al consultar por id")
  void shouldFailFindingPropiedadById() {
    given(propiedadRepository.findById(111L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> propiedadService.findById(111L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Propiedad con id 111 no encontrada");
  }

  @Test
  @DisplayName("Debe eliminar una propiedad existente")
  void shouldDeletePropiedad() {
    given(propiedadRepository.existsById(12L)).willReturn(true);

    propiedadService.delete(12L);

    verify(propiedadRepository).deleteById(12L);
  }

  @Test
  @DisplayName("Debe lanzar excepción si la propiedad no existe al eliminar")
  void shouldFailDeletingPropiedadMissing() {
    given(propiedadRepository.existsById(13L)).willReturn(false);

    assertThatThrownBy(() -> propiedadService.delete(13L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Propiedad con id 13 no encontrada");
  }

  @Test
  @DisplayName("Debe listar todas las propiedades")
  void shouldListPropiedades() {
    Propiedad una = new Propiedad();
    una.setId(1L);
    una.setNombre("Casa");
    una.setTipo(PropiedadTipo.CASA);
    una.setInmobiliaria(inmobiliaria);

    Propiedad dos = new Propiedad();
    dos.setId(2L);
    dos.setNombre("Departamento");
    dos.setTipo(PropiedadTipo.DEPARTAMENTO);
    dos.setInmobiliaria(inmobiliaria);

    given(propiedadRepository.findAll()).willReturn(List.of(una, dos));

    List<PropiedadResponse> responses = propiedadService.findAll();

    assertThat(responses).hasSize(2);
  }

  @Test
  @DisplayName("Debe listar las propiedades de una inmobiliaria específica")
  void shouldListPropiedadesByInmobiliaria() {
    Propiedad una = new Propiedad();
    una.setId(1L);
    una.setNombre("Casa");
    una.setTipo(PropiedadTipo.CASA);
    una.setInmobiliaria(inmobiliaria);

    given(inmobiliariaRepository.findById(10L)).willReturn(Optional.of(inmobiliaria));
    given(propiedadRepository.findByInmobiliariaId(eq(10L))).willReturn(List.of(una));

    List<PropiedadResponse> responses = propiedadService.findAllByInmobiliaria(10L);

    assertThat(responses).hasSize(1);
    verify(propiedadRepository).findByInmobiliariaId(10L);
  }
}
