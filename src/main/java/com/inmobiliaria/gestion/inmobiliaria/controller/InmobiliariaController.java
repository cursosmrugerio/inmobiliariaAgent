package com.inmobiliaria.gestion.inmobiliaria.controller;

import com.inmobiliaria.gestion.inmobiliaria.dto.CreateInmobiliariaRequest;
import com.inmobiliaria.gestion.inmobiliaria.dto.InmobiliariaResponse;
import com.inmobiliaria.gestion.inmobiliaria.dto.UpdateInmobiliariaRequest;
import com.inmobiliaria.gestion.inmobiliaria.service.InmobiliariaService;
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
@RequestMapping("/api/inmobiliarias")
@Tag(name = "Inmobiliarias", description = "Gestión de inmobiliarias")
public class InmobiliariaController {

  private final InmobiliariaService inmobiliariaService;

  public InmobiliariaController(InmobiliariaService inmobiliariaService) {
    this.inmobiliariaService = inmobiliariaService;
  }

  @Operation(
      summary = "Listar inmobiliarias",
      description = "Obtiene el catálogo completo de inmobiliarias registradas.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Listado obtenido exitosamente",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = InmobiliariaResponse.class)))
      })
  @GetMapping
  public ResponseEntity<List<InmobiliariaResponse>> findAll() {
    return ResponseEntity.ok(inmobiliariaService.findAll());
  }

  @Operation(
      summary = "Consultar inmobiliaria por id",
      description = "Obtiene los detalles de una inmobiliaria específica.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Inmobiliaria encontrada",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = InmobiliariaResponse.class))),
        @ApiResponse(responseCode = "404", description = "Inmobiliaria no encontrada")
      })
  @GetMapping("/{id}")
  public ResponseEntity<InmobiliariaResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(inmobiliariaService.findById(id));
  }

  @Operation(
      summary = "Crear inmobiliaria",
      description = "Registra una nueva inmobiliaria en el sistema.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Inmobiliaria creada",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = InmobiliariaResponse.class)))
      })
  @PostMapping
  public ResponseEntity<InmobiliariaResponse> create(
      @Valid @RequestBody CreateInmobiliariaRequest request) {
    InmobiliariaResponse response = inmobiliariaService.create(request);
    return ResponseEntity.created(URI.create("/api/inmobiliarias/" + response.getId()))
        .body(response);
  }

  @Operation(
      summary = "Actualizar inmobiliaria",
      description = "Actualiza los datos de una inmobiliaria existente.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Inmobiliaria actualizada",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = InmobiliariaResponse.class))),
        @ApiResponse(responseCode = "404", description = "Inmobiliaria no encontrada")
      })
  @PutMapping("/{id}")
  public ResponseEntity<InmobiliariaResponse> update(
      @PathVariable Long id, @Valid @RequestBody UpdateInmobiliariaRequest request) {
    return ResponseEntity.ok(inmobiliariaService.update(id, request));
  }

  @Operation(
      summary = "Eliminar inmobiliaria",
      description = "Elimina una inmobiliaria del sistema.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Inmobiliaria eliminada"),
        @ApiResponse(responseCode = "404", description = "Inmobiliaria no encontrada")
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    inmobiliariaService.delete(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
