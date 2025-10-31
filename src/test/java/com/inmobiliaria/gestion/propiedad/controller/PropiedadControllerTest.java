package com.inmobiliaria.gestion.propiedad.controller;

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
import com.inmobiliaria.gestion.inmobiliaria.repository.InmobiliariaRepository;
import com.inmobiliaria.gestion.propiedad.domain.Propiedad;
import com.inmobiliaria.gestion.propiedad.domain.PropiedadTipo;
import com.inmobiliaria.gestion.propiedad.dto.CreatePropiedadRequest;
import com.inmobiliaria.gestion.propiedad.dto.UpdatePropiedadRequest;
import com.inmobiliaria.gestion.propiedad.repository.PropiedadRepository;
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
class PropiedadControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private PropiedadRepository propiedadRepository;
  @Autowired private InmobiliariaRepository inmobiliariaRepository;

  @Test
  @DisplayName("Debe crear una propiedad")
  void shouldCreatePropiedad() throws Exception {
    Inmobiliaria inmobiliaria = createInmobiliaria("Inmo Norte");
    CreatePropiedadRequest request =
        new CreatePropiedadRequest(
            "Residencia Las Palmas",
            PropiedadTipo.CASA,
            "Av. Central 123",
            "Cuenta con alberca",
            inmobiliaria.getId());

    mockMvc
        .perform(
            post("/api/propiedades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.nombre").value("Residencia Las Palmas"))
        .andExpect(jsonPath("$.tipo").value("CASA"))
        .andExpect(jsonPath("$.direccion").value("Av. Central 123"))
        .andExpect(jsonPath("$.observaciones").value("Cuenta con alberca"))
        .andExpect(jsonPath("$.inmobiliariaId").value(inmobiliaria.getId()));
  }

  @Test
  @DisplayName("Debe listar las propiedades registradas")
  void shouldListPropiedades() throws Exception {
    Inmobiliaria inmobiliaria = createInmobiliaria("Inmo Centro");
    Propiedad primera = buildPropiedad("Casa Azul", PropiedadTipo.CASA, inmobiliaria);
    Propiedad segunda = buildPropiedad("Local Norte", PropiedadTipo.LOCAL, inmobiliaria);
    propiedadRepository.save(primera);
    propiedadRepository.save(segunda);

    mockMvc
        .perform(get("/api/propiedades"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  @DisplayName("Debe filtrar las propiedades por inmobiliaria")
  void shouldFilterPropiedadesByInmobiliaria() throws Exception {
    Inmobiliaria inmobiliariaUno = createInmobiliaria("Inmo Uno");
    Inmobiliaria inmobiliariaDos = createInmobiliaria("Inmo Dos");

    propiedadRepository.save(buildPropiedad("Casa Uno", PropiedadTipo.CASA, inmobiliariaUno));
    propiedadRepository.save(buildPropiedad("Local Dos", PropiedadTipo.LOCAL, inmobiliariaDos));

    mockMvc
        .perform(
            get("/api/propiedades").param("inmobiliariaId", inmobiliariaUno.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].inmobiliariaId").value(inmobiliariaUno.getId()));
  }

  @Test
  @DisplayName("Debe devolver 404 al filtrar por una inmobiliaria inexistente")
  void shouldReturnNotFoundWhenFilteringWithUnknownInmobiliaria() throws Exception {
    mockMvc
        .perform(get("/api/propiedades").param("inmobiliariaId", "999"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Debe obtener una propiedad por su id")
  void shouldGetPropiedadById() throws Exception {
    Inmobiliaria inmobiliaria = createInmobiliaria("Inmo Centro");
    Propiedad propiedad =
        buildPropiedad("Departamento Central", PropiedadTipo.DEPARTAMENTO, inmobiliaria);
    propiedad = propiedadRepository.save(propiedad);

    mockMvc
        .perform(get("/api/propiedades/{id}", propiedad.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(propiedad.getId()))
        .andExpect(jsonPath("$.nombre").value("Departamento Central"));
  }

  @Test
  @DisplayName("Debe actualizar una propiedad existente")
  void shouldUpdatePropiedad() throws Exception {
    Inmobiliaria inmobiliaria = createInmobiliaria("Inmo Sur");
    Propiedad propiedad = buildPropiedad("Casa Antigua", PropiedadTipo.CASA, inmobiliaria);
    propiedad = propiedadRepository.save(propiedad);

    UpdatePropiedadRequest request =
        new UpdatePropiedadRequest(
            "Casa Moderna", PropiedadTipo.DEPARTAMENTO, "Nueva 100", "Remodelada", null);

    mockMvc
        .perform(
            put("/api/propiedades/{id}", propiedad.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Casa Moderna"))
        .andExpect(jsonPath("$.tipo").value("DEPARTAMENTO"))
        .andExpect(jsonPath("$.direccion").value("Nueva 100"))
        .andExpect(jsonPath("$.observaciones").value("Remodelada"));
  }

  @Test
  @DisplayName("Debe eliminar una propiedad")
  void shouldDeletePropiedad() throws Exception {
    Inmobiliaria inmobiliaria = createInmobiliaria("Inmo Este");
    Propiedad propiedad = buildPropiedad("Local Plaza", PropiedadTipo.LOCAL, inmobiliaria);
    propiedad = propiedadRepository.save(propiedad);

    mockMvc
        .perform(delete("/api/propiedades/{id}", propiedad.getId()))
        .andExpect(status().isNoContent());

    assertThat(propiedadRepository.existsById(propiedad.getId())).isFalse();
  }

  private Inmobiliaria createInmobiliaria(String nombre) {
    Inmobiliaria inmobiliaria = new Inmobiliaria();
    inmobiliaria.setNombre(nombre);
    return inmobiliariaRepository.save(inmobiliaria);
  }

  private Propiedad buildPropiedad(String nombre, PropiedadTipo tipo, Inmobiliaria inmobiliaria) {
    Propiedad propiedad = new Propiedad();
    propiedad.setNombre(nombre);
    propiedad.setTipo(tipo);
    propiedad.setInmobiliaria(inmobiliaria);
    return propiedad;
  }
}
