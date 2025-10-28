package com.inmobiliaria.gestion.agent;

import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;
import com.inmobiliaria.gestion.agent.tools.PropiedadTool;
import org.springframework.stereotype.Component;

/**
 * Conversational AI agent specialized in managing Propiedad entities. Provides natural language
 * access to CRUD operations via the PropiedadTool.
 */
@Component
public class PropiedadAgent {

  public static final String ROOT_AGENT = "propiedad-assistant";

  private final PropiedadTool propiedadTool;
  private LlmAgent agent;

  public PropiedadAgent(PropiedadTool propiedadTool) {
    this.propiedadTool = propiedadTool;
    initializeAgent();
  }

  private void initializeAgent() {
    this.agent =
        LlmAgent.builder()
            .name(ROOT_AGENT)
            .model("gemini-2.0-flash")
            .instruction(
                "You are a helpful assistant that manages properties (propiedades) for a real estate "
                    + "management system. Use the available tools to fulfil the user's tasks.\n\n"
                    + "**Available tools:**\n"
                    + "1. listAllPropiedades() - When the user wants to see the complete property catalog.\n"
                    + "2. listPropiedadesByInmobiliaria(inmobiliariaId) - When the user wants "
                    + "properties belonging to a specific inmobiliaria.\n"
                    + "3. getPropiedadById(id) - When the user needs details of a particular property.\n"
                    + "4. createPropiedad(nombre, tipo, inmobiliariaId, direccion, observaciones) - "
                    + "When the user wants to register a new property. 'tipo' must come from the catalog "
                    + "(CASA, DEPARTAMENTO, OFICINA, LOCAL, ESTACIONAMIENTO, EDIFICIO, TERRENO, OTRO).\n"
                    + "5. updatePropiedad(id, ...) - When the user wants to modify an existing property. "
                    + "Only send the fields that change. Leave other parameters null for partial updates. "
                    + "If the user wants to move the property to another inmobiliaria, set inmobiliariaId.\n"
                    + "6. deletePropiedad(id) - When the user wants to remove a property. Confirm intention first.\n\n"
                    + "**Guidelines:**\n"
                    + "- Understand questions in Spanish or English.\n"
                    + "- Always prefer partial updates. Never ask for data the user did not mention.\n"
                    + "- Validate the property type against the catalog values listed above.\n"
                    + "- When a user mentions an inmobiliaria name but not the ID, politely ask for the ID.\n"
                    + "- Provide friendly, formatted responses summarizing the action taken.\n"
                    + "- When listing properties, include key details like ID, name, type, and owning inmobiliaria.\n"
                    + "- When deleting, confirm the action and mention that the property was deleted.\n"
                    + "- Explain errors in plain language if an operation fails.\n")
            .tools(
                FunctionTool.create(propiedadTool, "listAllPropiedades"),
                FunctionTool.create(propiedadTool, "listPropiedadesByInmobiliaria"),
                FunctionTool.create(propiedadTool, "getPropiedadById"),
                FunctionTool.create(propiedadTool, "createPropiedad"),
                FunctionTool.create(propiedadTool, "updatePropiedad"),
                FunctionTool.create(propiedadTool, "deletePropiedad"))
            .build();
  }

  public LlmAgent getAgent() {
    return agent;
  }

  public String getAgentName() {
    return ROOT_AGENT;
  }
}
