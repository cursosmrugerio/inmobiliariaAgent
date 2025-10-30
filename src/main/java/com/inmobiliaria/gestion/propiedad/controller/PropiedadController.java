package com.inmobiliaria.gestion.propiedad.controller;

import com.inmobiliaria.gestion.propiedad.dto.CreatePropiedadRequest;
import com.inmobiliaria.gestion.propiedad.dto.PropiedadResponse;
import com.inmobiliaria.gestion.propiedad.dto.UpdatePropiedadRequest;
import com.inmobiliaria.gestion.propiedad.service.PropiedadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/propiedades")
@Tag(name = "Propiedades", description = "Gestión del catálogo de propiedades")
public class PropiedadController {

  private final PropiedadService propiedadService;

  public PropiedadController(PropiedadService propiedadService) {
    this.propiedadService = propiedadService;
  }

  @Operation(
      summary = "Listar propiedades",
      description =
          "Obtiene todas las propiedades registradas o filtra por inmobiliaria si se proporciona el parámetro.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Listado obtenido exitosamente",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PropiedadResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Inmobiliaria no encontrada",
            content = @Content)
      })
  @GetMapping
  public ResponseEntity<List<PropiedadResponse>> findAll(
      @Parameter(description = "Identificador de la inmobiliaria para filtrar el resultado")
          @RequestParam(name = "inmobiliariaId", required = false)
          Long inmobiliariaId) {
    if (inmobiliariaId != null) {
      return ResponseEntity.ok(propiedadService.findAllByInmobiliaria(inmobiliariaId));
    }
    return ResponseEntity.ok(propiedadService.findAll());
  }

  @Operation(
      summary = "Consultar propiedad por id",
      description = "Obtiene los detalles de una propiedad específica.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Propiedad encontrada",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PropiedadResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Propiedad no encontrada",
            content = @Content)
      })
  @GetMapping("/{id}")
  public ResponseEntity<PropiedadResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(propiedadService.findById(id));
  }

  @Operation(
      summary = "Registrar propiedad",
      description = "Crea una nueva propiedad asociada a una inmobiliaria existente.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Propiedad creada",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PropiedadResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "Inmobiliaria no encontrada",
            content = @Content)
      })
  @PostMapping
  public ResponseEntity<PropiedadResponse> create(
      @Valid @RequestBody CreatePropiedadRequest request) {
    PropiedadResponse response = propiedadService.create(request);
    return ResponseEntity.created(URI.create("/api/propiedades/" + response.getId()))
        .body(response);
  }

  @Operation(
      summary = "Actualizar propiedad",
      description = "Actualiza los datos de una propiedad existente.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Propiedad actualizada",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PropiedadResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "Propiedad o inmobiliaria no encontrada",
            content = @Content)
      })
  @PutMapping("/{id}")
  public ResponseEntity<PropiedadResponse> update(
      @PathVariable Long id, @Valid @RequestBody UpdatePropiedadRequest request) {
    return ResponseEntity.ok(propiedadService.update(id, request));
  }

  @Operation(
      summary = "Eliminar propiedad",
      description = "Elimina una propiedad del sistema.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Propiedad eliminada", content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "Propiedad no encontrada",
            content = @Content)
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    propiedadService.delete(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
