package com.inmobiliaria.gestion.agent.tools;

import com.inmobiliaria.gestion.propiedad.domain.PropiedadTipo;
import com.inmobiliaria.gestion.propiedad.dto.CreatePropiedadRequest;
import com.inmobiliaria.gestion.propiedad.dto.PropiedadResponse;
import com.inmobiliaria.gestion.propiedad.dto.UpdatePropiedadRequest;
import com.inmobiliaria.gestion.propiedad.service.PropiedadService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ADK FunctionTool exposing CRUD operations for Propiedad entities. All methods delegate to {@link
 * PropiedadService} and return map-based responses suitable for conversational agents.
 */
@Component
public class PropiedadTool {

  private static final Logger log = LoggerFactory.getLogger(PropiedadTool.class);

  private final PropiedadService propiedadService;

  public PropiedadTool(PropiedadService propiedadService) {
    this.propiedadService = propiedadService;
  }

  /**
   * List all properties registered in the system.
   *
   * @return Map containing the result set
   */
  @Schema(description = "List all properties (propiedades) registered in the system")
  public Map<String, Object> listAllPropiedades() {
    try {
      List<PropiedadResponse> propiedades = propiedadService.findAll();
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("count", propiedades.size());
      result.put("propiedades", propiedades);
      return result;
    } catch (Exception e) {
      log.error("Error listing propiedades", e);
      return createErrorResponse("Error listing propiedades: " + e.getMessage());
    }
  }

  /**
   * List properties that belong to a specific inmobiliaria.
   *
   * @param inmobiliariaId Identifier of the owning inmobiliaria
   * @return Map containing the result set
   */
  @Schema(
      description =
          "List properties belonging to a specific real estate agency (inmobiliaria). Use this"
              + " when the user wants to see the portfolio of a given agency.")
  public Map<String, Object> listPropiedadesByInmobiliaria(
      @Schema(
              description = "Identifier of the inmobiliaria that owns the properties",
              example = "10",
              required = true)
          Integer inmobiliariaId) {
    try {
      List<PropiedadResponse> propiedades =
          propiedadService.findAllByInmobiliaria(inmobiliariaId.longValue());
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("count", propiedades.size());
      result.put("propiedades", propiedades);
      return result;
    } catch (Exception e) {
      log.error("Error listing propiedades for inmobiliaria {}", inmobiliariaId, e);
      return createErrorResponse("Error listing propiedades: " + e.getMessage());
    }
  }

  /**
   * Get the details of a specific property by id.
   *
   * @param id Identifier of the property
   * @return Map with the property response
   */
  @Schema(description = "Get details of a property by its ID")
  public Map<String, Object> getPropiedadById(
      @Schema(description = "ID of the property to retrieve", example = "25", required = true)
          Integer id) {
    try {
      PropiedadResponse response = propiedadService.findById(id.longValue());
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("propiedad", response);
      return result;
    } catch (Exception e) {
      log.error("Error retrieving propiedad {}", id, e);
      return createErrorResponse("Error retrieving propiedad: " + e.getMessage());
    }
  }

  /**
   * Create a new property.
   *
   * @param nombre Friendly name of the property (required)
   * @param tipo Catalog identifier (Casa, Departamento, Oficina, etc.)
   * @param inmobiliariaId Owning inmobiliaria identifier (required)
   * @param direccion Physical address
   * @param observaciones Internal notes
   * @return Map with creation result
   */
  @Schema(description = "Register a new property in the system")
  public Map<String, Object> createPropiedad(
      @Schema(
              description = "Display name of the property",
              example = "Residencia Azul",
              required = true)
          String nombre,
      @Schema(
              description = "Property type from the catalog (CASA, DEPARTAMENTO, OFICINA, etc.)",
              example = "CASA",
              required = true)
          String tipo,
      @Schema(description = "Identifier of the owning inmobiliaria", example = "7", required = true)
          Integer inmobiliariaId,
      @Schema(description = "Physical address or location reference", example = "Av. Central 123")
          String direccion,
      @Schema(
              description = "Internal notes about the property",
              example = "Contrato vigente hasta diciembre")
          String observaciones) {
    try {
      PropiedadTipo propiedadTipo = parseTipo(tipo, true);
      CreatePropiedadRequest request =
          new CreatePropiedadRequest(
              nombre, propiedadTipo, direccion, observaciones, inmobiliariaId.longValue());
      PropiedadResponse created = propiedadService.create(request);
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "Propiedad created successfully");
      result.put("propiedad", created);
      return result;
    } catch (Exception e) {
      log.error("Error creating propiedad", e);
      return createErrorResponse("Error creating propiedad: " + e.getMessage());
    }
  }

  /**
   * Update an existing property (supports partial updates).
   *
   * @param id Identifier of the property to update
   * @param nombre Updated name (optional)
   * @param tipo Updated property type (optional)
   * @param inmobiliariaId Identifier of a new owning inmobiliaria (optional)
   * @param direccion Updated address (optional)
   * @param observaciones Updated notes (optional)
   * @return Map with update result
   */
  @Schema(
      description =
          "Update an existing property. Supports partial updates: only provide the fields you want"
              + " to change. Any null field will keep its current value in the database.")
  public Map<String, Object> updatePropiedad(
      @Schema(description = "ID of the property to update", example = "25", required = true)
          Integer id,
      @Schema(description = "Updated name (optional)", example = "Residencia Actualizada")
          String nombre,
      @Schema(description = "Updated property type (optional)", example = "DEPARTAMENTO")
          String tipo,
      @Schema(description = "Updated owning inmobiliaria id (optional)", example = "9")
          Integer inmobiliariaId,
      @Schema(description = "Updated address (optional)", example = "Calle 5 #45") String direccion,
      @Schema(description = "Updated notes (optional)", example = "Disponible para visitas")
          String observaciones) {
    try {
      PropiedadTipo propiedadTipo = tipo != null ? parseTipo(tipo, false) : null;
      UpdatePropiedadRequest request =
          new UpdatePropiedadRequest(
              nombre,
              propiedadTipo,
              direccion,
              observaciones,
              inmobiliariaId != null ? inmobiliariaId.longValue() : null);
      PropiedadResponse updated = propiedadService.update(id.longValue(), request);
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "Propiedad updated successfully");
      result.put("propiedad", updated);
      return result;
    } catch (Exception e) {
      log.error("Error updating propiedad {}", id, e);
      return createErrorResponse("Error updating propiedad: " + e.getMessage());
    }
  }

  /**
   * Delete a property by id.
   *
   * @param id Identifier of the property
   * @return Map with deletion result
   */
  @Schema(description = "Delete a property by its ID")
  public Map<String, Object> deletePropiedad(
      @Schema(description = "ID of the property to delete", example = "25", required = true)
          Integer id) {
    try {
      propiedadService.delete(id.longValue());
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "Propiedad with ID " + id + " deleted successfully");
      return result;
    } catch (Exception e) {
      log.error("Error deleting propiedad {}", id, e);
      return createErrorResponse("Error deleting propiedad: " + e.getMessage());
    }
  }

  private PropiedadTipo parseTipo(String rawValue, boolean required) {
    if (rawValue == null) {
      if (required) {
        throw new IllegalArgumentException("tipo is required");
      }
      return null;
    }
    String normalized = rawValue.trim().replace(' ', '_').toUpperCase(Locale.ROOT);
    try {
      return PropiedadTipo.valueOf(normalized);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(
          "Invalid tipo '"
              + rawValue
              + "'. Expected values: CASA, DEPARTAMENTO, OFICINA, LOCAL, "
              + "ESTACIONAMIENTO, EDIFICIO, TERRENO, OTRO",
          ex);
    }
  }

  private Map<String, Object> createErrorResponse(String message) {
    Map<String, Object> error = new HashMap<>();
    error.put("success", false);
    error.put("error", message);
    return error;
  }
}
