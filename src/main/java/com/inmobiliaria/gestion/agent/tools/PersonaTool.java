package com.inmobiliaria.gestion.agent.tools;

import com.inmobiliaria.gestion.persona.domain.PersonaTipo;
import com.inmobiliaria.gestion.persona.dto.CreatePersonaRequest;
import com.inmobiliaria.gestion.persona.dto.PersonaResponse;
import com.inmobiliaria.gestion.persona.dto.UpdatePersonaRequest;
import com.inmobiliaria.gestion.persona.service.PersonaService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ADK FunctionTool exposing CRUD operations for Persona entities. All business logic stays in the
 * service layer; this class adapts the contract for conversational agents.
 */
@Component
public class PersonaTool {

  private static final Logger log = LoggerFactory.getLogger(PersonaTool.class);
  private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  private final PersonaService personaService;

  public PersonaTool(PersonaService personaService) {
    this.personaService = personaService;
  }

  /**
   * List all personas registered in the system.
   *
   * @return Map containing the result set
   */
  @Schema(description = "Listar todas las personas registradas en el sistema")
  public Map<String, Object> listAllPersonas() {
    try {
      List<PersonaResponse> personas = personaService.findAll();
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("count", personas.size());
      result.put("personas", personas);
      return result;
    } catch (Exception e) {
      log.error("Error listing personas", e);
      return createErrorResponse("Error listing personas: " + e.getMessage());
    }
  }

  /**
   * Retrieve the details for a specific persona.
   *
   * @param id Identifier of the persona
   * @return Map with persona information or error details
   */
  @Schema(description = "Obtener el detalle de una persona específica por su identificador")
  public Map<String, Object> getPersonaById(
      @Schema(description = "Identificador de la persona a consultar", example = "15") Integer id) {
    try {
      PersonaResponse persona = personaService.findById(id.longValue());
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("persona", persona);
      return result;
    } catch (Exception e) {
      log.error("Error retrieving persona {}", id, e);
      return createErrorResponse("Error retrieving persona: " + e.getMessage());
    }
  }

  /**
   * Register a new persona in the catalog.
   *
   * @param tipoPersona Tipo de persona (FISICA o MORAL)
   * @param nombre Nombre (para personas físicas)
   * @param apellidos Apellidos (para personas físicas)
   * @param razonSocial Razón social (para personas morales)
   * @param rfc RFC con homoclave
   * @param curp CURP (solo personas físicas)
   * @param email Correo de contacto
   * @param telefono Teléfono de contacto
   * @param fechaAlta Fecha de alta en formato ISO (yyyy-MM-dd'T'HH:mm:ss)
   * @param activo Estatus activo/inactivo
   * @return Map con la persona creada o el error correspondiente
   */
  @Schema(
      description =
          "Registrar una nueva persona física o moral. Utilice este método cuando se requiera dar"
              + " de alta un nuevo cliente o contacto.")
  public Map<String, Object> createPersona(
      @Schema(description = "Tipo de persona (FISICA o MORAL)", example = "FISICA", required = true)
          String tipoPersona,
      @Schema(description = "Nombre(s) de la persona física", example = "María Fernanda")
          String nombre,
      @Schema(description = "Apellidos de la persona física", example = "Gómez Ramírez")
          String apellidos,
      @Schema(description = "Razón social de la persona moral", example = "Servicios Delta S.A.")
          String razonSocial,
      @Schema(description = "RFC con homoclave", example = "GORA8501011H0") String rfc,
      @Schema(description = "CURP de la persona física", example = "GORM850101HDFRNL01")
          String curp,
      @Schema(description = "Correo electrónico de contacto", example = "contacto@delta.com")
          String email,
      @Schema(description = "Teléfono de contacto", example = "+52-55-5555-5555") String telefono,
      @Schema(
              description = "Fecha de alta en formato ISO (yyyy-MM-ddTHH:mm:ss)",
              example = "2024-02-10T09:30:00",
              required = true)
          String fechaAlta,
      @Schema(description = "Estatus activo/inactivo", example = "true", required = true)
          Boolean activo) {
    try {
      PersonaTipo personaTipo = parseTipo(tipoPersona, true);
      LocalDateTime alta = parseFecha(fechaAlta, true);
      if (activo == null) {
        throw new IllegalArgumentException("El parámetro 'activo' es obligatorio");
      }
      CreatePersonaRequest request =
          new CreatePersonaRequest(
              personaTipo,
              nombre,
              apellidos,
              razonSocial,
              rfc,
              curp,
              email,
              telefono,
              alta,
              activo);
      PersonaResponse created = personaService.create(request);
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "Persona created successfully");
      result.put("persona", created);
      return result;
    } catch (Exception e) {
      log.error("Error creating persona", e);
      return createErrorResponse("Error creating persona: " + e.getMessage());
    }
  }

  /**
   * Update an existing persona. Supports partial updates - only provide the fields to change.
   *
   * @param id Identifier of the persona
   * @param tipoPersona Nuevo tipo (FISICA/MORAL) opcional
   * @param nombre Nombre opcional
   * @param apellidos Apellidos opcional
   * @param razonSocial Razón social opcional
   * @param rfc RFC opcional
   * @param curp CURP opcional
   * @param email Correo opcional
   * @param telefono Teléfono opcional
   * @param fechaAlta Nueva fecha de alta opcional en formato ISO
   * @param activo Estatus activo opcional
   * @return Map con la persona actualizada o error
   */
  @Schema(
      description =
          "Actualizar una persona existente. Este método admite actualizaciones parciales: solo"
              + " proporcione los campos que desea cambiar.")
  public Map<String, Object> updatePersona(
      @Schema(description = "Identificador de la persona", example = "12", required = true)
          Integer id,
      @Schema(description = "Nuevo tipo de persona", example = "MORAL") String tipoPersona,
      @Schema(description = "Nombre(s) actualizado(s)", example = "Servicios") String nombre,
      @Schema(description = "Apellidos actualizados", example = "Actualizados") String apellidos,
      @Schema(description = "Nueva razón social", example = "Servicios Renovados S.A.")
          String razonSocial,
      @Schema(description = "Nuevo RFC", example = "REN123456789") String rfc,
      @Schema(description = "Nueva CURP", example = "RENX850101HDFABC01") String curp,
      @Schema(description = "Nuevo correo", example = "nuevo@servicios.com") String email,
      @Schema(description = "Nuevo teléfono", example = "+52-55-6666-7777") String telefono,
      @Schema(description = "Nueva fecha de alta en formato ISO", example = "2024-03-01T10:00:00")
          String fechaAlta,
      @Schema(description = "Nuevo estatus activo/inactivo", example = "false") Boolean activo) {
    try {
      PersonaTipo personaTipo = tipoPersona != null ? parseTipo(tipoPersona, false) : null;
      LocalDateTime alta = fechaAlta != null ? parseFecha(fechaAlta, false) : null;
      UpdatePersonaRequest request =
          new UpdatePersonaRequest(
              personaTipo,
              nombre,
              apellidos,
              razonSocial,
              rfc,
              curp,
              email,
              telefono,
              alta,
              activo);
      PersonaResponse updated = personaService.update(id.longValue(), request);
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "Persona updated successfully");
      result.put("persona", updated);
      return result;
    } catch (Exception e) {
      log.error("Error updating persona {}", id, e);
      return createErrorResponse("Error updating persona: " + e.getMessage());
    }
  }

  /**
   * Delete a persona by its identifier.
   *
   * @param id Identifier of the persona to delete
   * @return Map with the operation outcome
   */
  @Schema(description = "Eliminar una persona del sistema")
  public Map<String, Object> deletePersona(
      @Schema(description = "Identificador de la persona a eliminar", example = "20") Integer id) {
    try {
      personaService.delete(id.longValue());
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "Persona with ID " + id + " deleted successfully");
      return result;
    } catch (Exception e) {
      log.error("Error deleting persona {}", id, e);
      return createErrorResponse("Error deleting persona: " + e.getMessage());
    }
  }

  private PersonaTipo parseTipo(String raw, boolean required) {
    if (raw == null || raw.isBlank()) {
      if (required) {
        throw new IllegalArgumentException("El tipo de persona es obligatorio (FISICA o MORAL)");
      }
      return null;
    }
    try {
      return PersonaTipo.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(
          "Tipo de persona inválido. Utiliza FISICA o MORAL (valor recibido: " + raw + ")");
    }
  }

  private LocalDateTime parseFecha(String raw, boolean required) {
    if (raw == null || raw.isBlank()) {
      if (required) {
        throw new IllegalArgumentException(
            "La fecha de alta es obligatoria y debe tener formato yyyy-MM-ddTHH:mm:ss");
      }
      return null;
    }
    try {
      return LocalDateTime.parse(raw.trim(), ISO_FORMATTER);
    } catch (DateTimeParseException ex) {
      throw new IllegalArgumentException(
          "Formato de fecha inválido. Utiliza yyyy-MM-ddTHH:mm:ss (valor recibido: " + raw + ")");
    }
  }

  private Map<String, Object> createErrorResponse(String message) {
    Map<String, Object> error = new HashMap<>();
    error.put("success", false);
    error.put("error", message);
    return error;
  }
}
