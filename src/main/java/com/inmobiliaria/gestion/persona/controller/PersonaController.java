package com.inmobiliaria.gestion.persona.controller;

import com.inmobiliaria.gestion.persona.dto.CreatePersonaRequest;
import com.inmobiliaria.gestion.persona.dto.PersonaResponse;
import com.inmobiliaria.gestion.persona.dto.UpdatePersonaRequest;
import com.inmobiliaria.gestion.persona.service.PersonaService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/personas")
@Tag(name = "Personas", description = "Gestión del catálogo de personas")
public class PersonaController {

  private final PersonaService personaService;

  public PersonaController(PersonaService personaService) {
    this.personaService = personaService;
  }

  @Operation(
      summary = "Listar personas",
      description = "Obtiene el catálogo completo de personas registradas.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Listado obtenido exitosamente",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PersonaResponse.class)))
      })
  @GetMapping
  public ResponseEntity<List<PersonaResponse>> findAll() {
    return ResponseEntity.ok(personaService.findAll());
  }

  @Operation(
      summary = "Consultar persona por id",
      description = "Obtiene el detalle de una persona específica.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Persona encontrada",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PersonaResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Persona no encontrada",
            content = @Content)
      })
  @GetMapping("/{id}")
  public ResponseEntity<PersonaResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(personaService.findById(id));
  }

  @Operation(
      summary = "Crear persona",
      description = "Registra una nueva persona física o moral.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Persona creada",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PersonaResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content)
      })
  @PostMapping
  public ResponseEntity<PersonaResponse> create(@Valid @RequestBody CreatePersonaRequest request) {
    PersonaResponse response = personaService.create(request);
    return ResponseEntity.created(URI.create("/api/personas/" + response.getId())).body(response);
  }

  @Operation(
      summary = "Actualizar persona",
      description = "Actualiza los datos de una persona existente.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Persona actualizada",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PersonaResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "Persona no encontrada",
            content = @Content)
      })
  @PutMapping("/{id}")
  public ResponseEntity<PersonaResponse> update(
      @PathVariable Long id, @Valid @RequestBody UpdatePersonaRequest request) {
    return ResponseEntity.ok(personaService.update(id, request));
  }

  @Operation(
      summary = "Eliminar persona",
      description = "Elimina una persona del sistema.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Persona eliminada", content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "Persona no encontrada",
            content = @Content)
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    personaService.delete(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
