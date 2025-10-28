package com.inmobiliaria.gestion;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health Check", description = "Endpoint to check the application status")
public class HealthCheckController {

  @Operation(
      summary = "Check application health",
      description = "Returns the operational status of the application.")
  @ApiResponse(responseCode = "200", description = "Application is running")
  @GetMapping("/health")
  public Map<String, String> healthCheck() {
    return Map.of("status", "UP");
  }
}
