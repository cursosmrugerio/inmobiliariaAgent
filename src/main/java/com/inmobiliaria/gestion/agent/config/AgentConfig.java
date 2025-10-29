package com.inmobiliaria.gestion.agent.config;

import com.google.adk.runner.InMemoryRunner;
import com.inmobiliaria.gestion.agent.InmobiliariaAgent;
import com.inmobiliaria.gestion.agent.PersonaAgent;
import com.inmobiliaria.gestion.agent.PropiedadAgent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for ADK (Agent Development Kit) components. This configuration sets up the
 * agent runner and manages the lifecycle of conversational AI agents.
 */
@Configuration
public class AgentConfig {

  /**
   * Creates an InMemoryRunner bean for executing the Inmobiliaria agent. The runner manages
   * sessions and executes agent interactions.
   *
   * @param inmobiliariaAgent The configured Inmobiliaria agent
   * @return InMemoryRunner configured with the agent
   */
  @Bean(name = "inmobiliariaAgentRunner")
  public InMemoryRunner inmobiliariaAgentRunner(InmobiliariaAgent inmobiliariaAgent) {
    return new InMemoryRunner(inmobiliariaAgent.getAgent());
  }

  @Bean(name = "propiedadAgentRunner")
  public InMemoryRunner propiedadAgentRunner(PropiedadAgent propiedadAgent) {
    return new InMemoryRunner(propiedadAgent.getAgent());
  }

  @Bean(name = "personaAgentRunner")
  public InMemoryRunner personaAgentRunner(PersonaAgent personaAgent) {
    return new InMemoryRunner(personaAgent.getAgent());
  }
}
