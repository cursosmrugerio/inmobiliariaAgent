package com.inmobiliaria.gestion.agent.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.inmobiliaria.gestion.propiedad.domain.PropiedadTipo;
import com.inmobiliaria.gestion.propiedad.dto.CreatePropiedadRequest;
import com.inmobiliaria.gestion.propiedad.dto.PropiedadResponse;
import com.inmobiliaria.gestion.propiedad.dto.UpdatePropiedadRequest;
import com.inmobiliaria.gestion.propiedad.service.PropiedadService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PropiedadToolTest {

  @Mock private PropiedadService propiedadService;

  @InjectMocks private PropiedadTool propiedadTool;

  private PropiedadResponse sampleResponse;

  @BeforeEach
  void setUp() {
    sampleResponse =
        new PropiedadResponse(
            1L,
            "Residencia Azul",
            PropiedadTipo.CASA,
            "Av. Central 123",
            "Con alberca",
            10L,
            "Inmo Norte");
  }

  @Test
  void listAllPropiedades_success() {
    when(propiedadService.findAll()).thenReturn(List.of(sampleResponse));

    Map<String, Object> result = propiedadTool.listAllPropiedades();

    assertThat(result.get("success")).isEqualTo(true);
    assertThat(result.get("count")).isEqualTo(1);
    assertThat(result.get("propiedades")).isInstanceOf(List.class);
  }

  @Test
  void listAllPropiedades_error() {
    when(propiedadService.findAll()).thenThrow(new RuntimeException("DB down"));

    Map<String, Object> result = propiedadTool.listAllPropiedades();

    assertThat(result.get("success")).isEqualTo(false);
    assertThat((String) result.get("error")).contains("Error listing propiedades");
  }

  @Test
  void listPropiedadesByInmobiliaria_success() {
    when(propiedadService.findAllByInmobiliaria(10L)).thenReturn(List.of(sampleResponse));

    Map<String, Object> result = propiedadTool.listPropiedadesByInmobiliaria(10);

    assertThat(result.get("success")).isEqualTo(true);
    assertThat(result.get("count")).isEqualTo(1);
  }

  @Test
  void listPropiedadesByInmobiliaria_error() {
    when(propiedadService.findAllByInmobiliaria(10L)).thenThrow(new RuntimeException("Fail"));

    Map<String, Object> result = propiedadTool.listPropiedadesByInmobiliaria(10);

    assertThat(result.get("success")).isEqualTo(false);
  }

  @Test
  void getPropiedadById_success() {
    when(propiedadService.findById(1L)).thenReturn(sampleResponse);

    Map<String, Object> result = propiedadTool.getPropiedadById(1);

    assertThat(result.get("success")).isEqualTo(true);
    assertThat(result.get("propiedad")).isNotNull();
  }

  @Test
  void getPropiedadById_error() {
    when(propiedadService.findById(1L)).thenThrow(new RuntimeException("Missing"));

    Map<String, Object> result = propiedadTool.getPropiedadById(1);

    assertThat(result.get("success")).isEqualTo(false);
  }

  @Test
  void createPropiedad_success() {
    when(propiedadService.create(any(CreatePropiedadRequest.class))).thenReturn(sampleResponse);

    Map<String, Object> result =
        propiedadTool.createPropiedad(
            "Residencia Azul", "Casa", 10, "Av. Central 123", "Con alberca");

    assertThat(result.get("success")).isEqualTo(true);
    assertThat(result.get("propiedad")).isNotNull();
  }

  @Test
  void createPropiedad_invalidTipo_returnsError() {
    Map<String, Object> result =
        propiedadTool.createPropiedad("Residencia Azul", "INVALIDO", 10, null, null);

    assertThat(result.get("success")).isEqualTo(false);
    assertThat((String) result.get("error")).contains("Invalid tipo");
  }

  @Test
  void updatePropiedad_success() {
    when(propiedadService.update(eq(1L), any(UpdatePropiedadRequest.class)))
        .thenReturn(sampleResponse);

    Map<String, Object> result =
        propiedadTool.updatePropiedad(1, "Residencia Roja", "Departamento", 11, null, null);

    assertThat(result.get("success")).isEqualTo(true);
    assertThat(result.get("propiedad")).isNotNull();
  }

  @Test
  void updatePropiedad_invalidTipo_returnsError() {
    Map<String, Object> result = propiedadTool.updatePropiedad(1, null, "N/A", null, null, null);

    assertThat(result.get("success")).isEqualTo(false);
    assertThat((String) result.get("error")).contains("Invalid tipo");
  }

  @Test
  void deletePropiedad_success() {
    doNothing().when(propiedadService).delete(1L);

    Map<String, Object> result = propiedadTool.deletePropiedad(1);

    assertThat(result.get("success")).isEqualTo(true);
  }

  @Test
  void deletePropiedad_error() {
    doThrow(new RuntimeException("Cannot delete")).when(propiedadService).delete(1L);

    Map<String, Object> result = propiedadTool.deletePropiedad(1);

    assertThat(result.get("success")).isEqualTo(false);
  }
}
