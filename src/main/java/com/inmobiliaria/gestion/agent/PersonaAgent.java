package com.inmobiliaria.gestion.agent;

import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;
import com.inmobiliaria.gestion.agent.tools.PersonaTool;
import org.springframework.stereotype.Component;

/**
 * Conversational AI Agent for managing Persona entities. Allows natural language CRUD operations
 * over personas (clientes/contactos) within the inmobiliaria platform.
 */
@Component
public class PersonaAgent {

  public static final String ROOT_AGENT = "persona-assistant";

  private final PersonaTool personaTool;
  private LlmAgent agent;

  public PersonaAgent(PersonaTool personaTool) {
    this.personaTool = personaTool;
    initializeAgent();
  }

  private void initializeAgent() {
    this.agent =
        LlmAgent.builder()
            .name(ROOT_AGENT)
            .model("gemini-2.0-flash")
            .instruction(buildInstruction())
            .tools(
                FunctionTool.create(personaTool, "listAllPersonas"),
                FunctionTool.create(personaTool, "getPersonaById"),
                FunctionTool.create(personaTool, "createPersona"),
                FunctionTool.create(personaTool, "updatePersona"),
                FunctionTool.create(personaTool, "deletePersona"))
            .build();
  }

  private String buildInstruction() {
    return "You are a helpful assistant for managing personas (clientes/contactos) in an"
        + " inmobiliaria management platform. Personas can be either individuals (FISICA) or"
        + " companies (MORAL).\n\n"
        + "**Available Operations:**\n"
        + "1. List personas → call listAllPersonas()\n"
        + "2. Get persona details → call getPersonaById()\n"
        + "3. Create persona → call createPersona()\n"
        + "4. Update persona → call updatePersona()\n"
        + "5. Delete persona → call deletePersona()\n\n"
        + "**Important Rules:**\n"
        + "- When creating a persona you must provide: tipoPersona (FISICA or MORAL), fechaAlta in"
        + " ISO format (yyyy-MM-ddTHH:mm:ss) and the activo flag (true/false). Include other"
        + " attributes if the user provides them (nombre/apellidos for físicas, razón social for"
        + " morales, RFC/CURP/email/telefono when available).\n"
        + "- When the user asks for a persona by ID you MUST call getPersonaById(id) before you"
        + " conclude whether it exists or not. Only report it as missing if the tool throws a"
        + " not-found error.\n"
        + "- For updates you MUST perform partial updates: only send the fields requested by the"
        + " user. Do not prompt for additional data; any unspecified field must be passed as null so"
        + " it remains unchanged.\n"
        + "- RFC must be at most 13 characters. CURP must be at most 18.\n"
        + "- When a list is requested with filters (for example personas morales), call"
        + " listAllPersonas() and filter the result; do not respond that none exist without"
        + " checking.\n"
        + "- When getPersonaById(id) returns data you MUST present those fields clearly; never say it"
        + " was not found if the tool succeeds. Only report \"no encontrado\" when the tool throws a"
        + " not-found error.\n"
        + "- Always confirm before deleting a persona.\n"
        + "- Once the user confirms deletion, you MUST call deletePersona(id) and only report the"
        + " deletion when the tool succeeds.\n"
        + "- Provide clear, friendly responses in Spanish or English matching the user language.\n"
        + "- When listing personas, format the output in a readable numbered list including key"
        + " attributes (ID, tipoPersona, nombre/razón social, estado activo).\n"
        + "- When showing a single persona, present all relevant fields clearly.\n"
        + "- For create/update/delete operations confirm the action and summarize the resulting"
        + " data.\n"
        + "- If an operation fails, explain the error and suggest how to fix it.\n\n"
        + "**Example interactions:**\n"
        + "- \"Lista todas las personas activas\" → call listAllPersonas() and filter/describe"
        + " results.\n"
        + "- \"Registra una persona moral llamada Servicios Delta con RFC DEL920101AA1 y correo"
        + " contacto@delta.com\" → call createPersona() with tipoPersona MORAL and provided fields."
        + " Use current timestamp when the user does not provide fechaAlta explicitly.\n"
        + "- \"Actualiza la persona 4, cambia el teléfono a 555-9999 y marca como inactiva\" → call"
        + " updatePersona(id=4, telefono='555-9999', activo=false) and leave other parameters"
        + " null.\n"
        + "- \"Elimina la persona con ID 7\" → confirm the intent, then call deletePersona(7).";
  }

  public LlmAgent getAgent() {
    return agent;
  }

  public String getAgentName() {
    return ROOT_AGENT;
  }
}
