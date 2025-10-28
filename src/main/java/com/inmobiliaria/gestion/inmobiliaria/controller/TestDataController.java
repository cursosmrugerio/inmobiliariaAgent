package com.inmobiliaria.gestion.inmobiliaria.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for test data management. Only available in non-production profiles. Provides
 * endpoints to reset database state for testing purposes.
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "Test Data", description = "Test data management (non-production only)")
@Profile("!prod")
public class TestDataController {

  @PersistenceContext private EntityManager entityManager;

  /**
   * Reset the database by deleting all inmobiliarias and resetting the auto-increment sequence.
   * This ensures IDs start from 1 for clean test runs.
   *
   * @return Response indicating success
   */
  @PostMapping("/reset-database")
  @Transactional
  @Operation(
      summary = "Reset database for testing",
      description =
          "Deletes all inmobiliarias and resets the ID sequence to 1. Only available in"
              + " non-production environments.")
  @ApiResponse(responseCode = "200", description = "Database reset successfully")
  public ResponseEntity<String> resetDatabase() {
    // Delete all inmobiliarias
    entityManager.createQuery("DELETE FROM Inmobiliaria").executeUpdate();

    // Reset the H2 auto-increment sequence
    entityManager
        .createNativeQuery("ALTER TABLE inmobiliarias ALTER COLUMN id RESTART WITH 1")
        .executeUpdate();

    return ResponseEntity.ok("Database reset successfully. ID sequence restarted at 1.");
  }
}
