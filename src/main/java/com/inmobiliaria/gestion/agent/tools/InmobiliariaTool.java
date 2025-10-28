package com.inmobiliaria.gestion.agent.tools;

import com.inmobiliaria.gestion.inmobiliaria.dto.CreateInmobiliariaRequest;
import com.inmobiliaria.gestion.inmobiliaria.dto.InmobiliariaResponse;
import com.inmobiliaria.gestion.inmobiliaria.dto.UpdateInmobiliariaRequest;
import com.inmobiliaria.gestion.inmobiliaria.service.InmobiliariaService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * ADK FunctionTool for managing Inmobiliaria (Real Estate Agency) entities. This class provides
 * conversational CRUD operations that can be called by AI agents.
 */
@Component
public class InmobiliariaTool {

  private final InmobiliariaService inmobiliariaService;

  public InmobiliariaTool(InmobiliariaService inmobiliariaService) {
    this.inmobiliariaService = inmobiliariaService;
  }

  /**
   * List all real estate agencies in the system.
   *
   * @return Map containing list of all agencies
   */
  @Schema(description = "List all real estate agencies (inmobiliarias) in the system")
  public Map<String, Object> listAllInmobiliarias() {
    try {
      List<InmobiliariaResponse> inmobiliarias = inmobiliariaService.findAll();
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("count", inmobiliarias.size());
      result.put("inmobiliarias", inmobiliarias);
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error listing inmobiliarias: " + e.getMessage());
    }
  }

  /**
   * Get a specific real estate agency by its ID.
   *
   * @param id The unique identifier of the inmobiliaria
   * @return Map containing the inmobiliaria details or error
   */
  @Schema(
      description =
          "Get details of a specific real estate agency by its ID. Use this when the user asks"
              + " about a specific agency.")
  public Map<String, Object> getInmobiliariaById(
      @Schema(description = "The ID of the inmobiliaria to retrieve", example = "1") Integer id) {
    try {
      InmobiliariaResponse inmobiliaria = inmobiliariaService.findById(id.longValue());
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("inmobiliaria", inmobiliaria);
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error retrieving inmobiliaria: " + e.getMessage());
    }
  }

  /**
   * Create a new real estate agency.
   *
   * @param nombre Commercial name of the agency (required)
   * @param rfc Mexican tax ID (RFC), max 13 characters
   * @param nombreContacto Contact person name
   * @param correo Email address
   * @param telefono Phone number
   * @return Map containing the created inmobiliaria or error
   */
  @Schema(
      description =
          "Create a new real estate agency (inmobiliaria). Use this when the user wants to"
              + " register a new agency.")
  public Map<String, Object> createInmobiliaria(
      @Schema(
              description = "Commercial name of the real estate agency",
              example = "Inmobiliaria del Norte",
              required = true)
          String nombre,
      @Schema(description = "Mexican tax ID (RFC), max 13 characters", example = "XAXX010101000")
          String rfc,
      @Schema(description = "Name of the contact person", example = "Juan Pérez")
          String nombreContacto,
      @Schema(description = "Contact email address", example = "contacto@ejemplo.com")
          String correo,
      @Schema(description = "Contact phone number", example = "+52-55-1234-5678") String telefono) {
    try {
      CreateInmobiliariaRequest request =
          new CreateInmobiliariaRequest(nombre, rfc, nombreContacto, correo, telefono);
      InmobiliariaResponse created = inmobiliariaService.create(request);
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "Inmobiliaria created successfully");
      result.put("inmobiliaria", created);
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error creating inmobiliaria: " + e.getMessage());
    }
  }

  /**
   * Update an existing real estate agency.
   *
   * @param id The ID of the inmobiliaria to update
   * @param nombre Updated commercial name
   * @param rfc Updated RFC
   * @param nombreContacto Updated contact person name
   * @param correo Updated email
   * @param telefono Updated phone number
   * @return Map containing the updated inmobiliaria or error
   */
  @Schema(
      description =
          "Update an existing real estate agency. Use this when the user wants to modify agency"
              + " information.")
  public Map<String, Object> updateInmobiliaria(
      @Schema(description = "The ID of the inmobiliaria to update", example = "1", required = true)
          Integer id,
      @Schema(description = "Updated commercial name", example = "Inmobiliaria del Sur")
          String nombre,
      @Schema(description = "Updated RFC", example = "XAXX010101000") String rfc,
      @Schema(description = "Updated contact person name", example = "María García")
          String nombreContacto,
      @Schema(description = "Updated email", example = "nuevo@ejemplo.com") String correo,
      @Schema(description = "Updated phone number", example = "+52-55-9876-5432") String telefono) {
    try {
      UpdateInmobiliariaRequest request =
          new UpdateInmobiliariaRequest(nombre, rfc, nombreContacto, correo, telefono);
      InmobiliariaResponse updated = inmobiliariaService.update(id.longValue(), request);
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "Inmobiliaria updated successfully");
      result.put("inmobiliaria", updated);
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error updating inmobiliaria: " + e.getMessage());
    }
  }

  /**
   * Delete a real estate agency by its ID.
   *
   * @param id The ID of the inmobiliaria to delete
   * @return Map containing success status or error
   */
  @Schema(
      description =
          "Delete a real estate agency. Use this when the user wants to remove an agency from the"
              + " system.")
  public Map<String, Object> deleteInmobiliaria(
      @Schema(description = "The ID of the inmobiliaria to delete", example = "1", required = true)
          Integer id) {
    try {
      inmobiliariaService.delete(id.longValue());
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "Inmobiliaria with ID " + id + " deleted successfully");
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error deleting inmobiliaria: " + e.getMessage());
    }
  }

  private Map<String, Object> createErrorResponse(String message) {
    Map<String, Object> error = new HashMap<>();
    error.put("success", false);
    error.put("error", message);
    return error;
  }
}
