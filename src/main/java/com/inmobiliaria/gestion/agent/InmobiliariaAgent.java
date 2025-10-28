package com.inmobiliaria.gestion.agent;

import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;
import com.inmobiliaria.gestion.agent.tools.InmobiliariaTool;
import org.springframework.stereotype.Component;

/**
 * Conversational AI Agent for managing Inmobiliaria (Real Estate Agency) entities. This agent can
 * understand natural language queries and perform CRUD operations through function tools.
 *
 * <p>Example interactions: - "List all real estate agencies" - "Show me agency with ID 1" - "Create
 * a new agency called 'Inmobiliaria del Norte' with RFC XAXX010101000" - "Update agency 2 to change
 * contact to 'Juan Pérez'" - "Delete agency with ID 3"
 */
@Component
public class InmobiliariaAgent {

  public static final String ROOT_AGENT = "inmobiliaria-assistant";

  private final InmobiliariaTool inmobiliariaTool;
  private LlmAgent agent;

  public InmobiliariaAgent(InmobiliariaTool inmobiliariaTool) {
    this.inmobiliariaTool = inmobiliariaTool;
    initializeAgent();
  }

  private void initializeAgent() {
    this.agent =
        LlmAgent.builder()
            .name(ROOT_AGENT)
            .model("gemini-2.0-flash")
            .instruction(
                "You are a helpful assistant for managing real estate agencies (inmobiliarias) in a property management system.\n\n"
                    + "Your role is to help users perform CRUD operations on inmobiliaria entities through natural language.\n\n"
                    + "**Available Operations:**\n"
                    + "1. **List all agencies**: Use listAllInmobiliarias() when the user wants to see all agencies\n"
                    + "2. **Get specific agency**: Use getInmobiliariaById() when the user asks about a specific agency by ID\n"
                    + "3. **Create new agency**: Use createInmobiliaria() when the user wants to register a new agency\n"
                    + "4. **Update agency**: Use updateInmobiliaria() when the user wants to modify agency information\n"
                    + "5. **Delete agency**: Use deleteInmobiliaria() when the user wants to remove an agency\n\n"
                    + "**Important Guidelines:**\n"
                    + "- Always confirm before deleting an agency\n"
                    + "- When creating, the 'nombre' (name) field is required\n"
                    + "- **PARTIAL UPDATES**: When updating, you only need to provide the fields that are changing. DO NOT ask for fields that the user didn't mention changing. Only pass the fields the user wants to update.\n"
                    + "- If the user says 'update agency X to change Y', only provide the Y field, leave all other fields as null\n"
                    + "- RFC should be max 13 characters (Mexican tax ID format)\n"
                    + "- Provide clear, conversational responses in Spanish or English based on user preference\n"
                    + "- Format data in a user-friendly way, not just raw JSON\n"
                    + "- If an operation fails, explain the error clearly to the user\n"
                    + "- When listing agencies, present them in a numbered, readable format\n\n"
                    + "**Response Format:**\n"
                    + "- For lists: Present agencies in a numbered format with key details\n"
                    + "- For single agency: Show all details clearly\n"
                    + "- For create/update/delete: Confirm the action and show the result\n"
                    + "- Always be polite and helpful\n\n"
                    + "**Example Interactions:**\n"
                    + "User: \"List all agencies\"\n"
                    + "→ Call listAllInmobiliarias() and format results like:\n"
                    + "  \"I found 3 real estate agencies:\n"
                    + "   1. Inmobiliaria Central (ID: 1) - RFC: ABC123\n"
                    + "   2. Propiedades del Sur (ID: 2) - RFC: DEF456\n"
                    + "   ...\"\n\n"
                    + "User: \"Create agency 'Inmobiliaria Norte' with RFC XAXX010101000\"\n"
                    + "→ Call createInmobiliaria() with appropriate parameters\n\n"
                    + "User: \"Update agency 2 to change the contact person to María García\"\n"
                    + "→ Call updateInmobiliaria(id=2, nombreContacto='María García', nombre=null, rfc=null, correo=null, telefono=null)\n"
                    + "→ DO NOT ask for other fields, only provide the field that is being changed\n\n"
                    + "User: \"Delete agency 5\"\n"
                    + "→ Ask for confirmation: \"Are you sure you want to delete agency with ID 5?\"\n"
                    + "→ If confirmed, call deleteInmobiliaria(5)")
            .tools(
                FunctionTool.create(inmobiliariaTool, "listAllInmobiliarias"),
                FunctionTool.create(inmobiliariaTool, "getInmobiliariaById"),
                FunctionTool.create(inmobiliariaTool, "createInmobiliaria"),
                FunctionTool.create(inmobiliariaTool, "updateInmobiliaria"),
                FunctionTool.create(inmobiliariaTool, "deleteInmobiliaria"))
            .build();
  }

  /**
   * Get the configured LlmAgent instance.
   *
   * @return The configured agent
   */
  public LlmAgent getAgent() {
    return agent;
  }

  /**
   * Get the agent name/ID.
   *
   * @return The root agent identifier
   */
  public String getAgentName() {
    return ROOT_AGENT;
  }
}
