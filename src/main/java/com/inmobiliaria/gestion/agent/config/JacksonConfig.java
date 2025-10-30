package com.inmobiliaria.gestion.agent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson ObjectMapper configuration for the application. This configuration ensures proper
 * serialization of Java 8 date/time types (LocalDateTime, LocalDate, etc.) which are used in DTOs
 * throughout the application, including in ADK agent tool responses.
 *
 * <p>The JSR-310 module is required for handling java.time.* types in JSON serialization, which is
 * critical for ADK FunctionTool operations that serialize DTOs containing LocalDateTime fields.
 */
@Configuration
public class JacksonConfig {

  /**
   * Creates a primary ObjectMapper bean with JSR-310 support for Java 8 date/time types. This bean
   * is used throughout the application, including by Spring Boot's auto-configured HTTP message
   * converters and by any component that autowires ObjectMapper.
   *
   * @return Configured ObjectMapper with JavaTimeModule registered
   */
  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    // Register the Java 8 date/time module (JSR-310)
    mapper.registerModule(new JavaTimeModule());
    // Write dates as ISO-8601 strings instead of timestamps
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }
}
