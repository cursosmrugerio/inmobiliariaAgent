package com.inmobiliaria.gestion.config;

import com.inmobiliaria.gestion.auth.domain.UserAccount;
import com.inmobiliaria.gestion.auth.domain.UserRole;
import com.inmobiliaria.gestion.auth.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Initializes default data on application startup. Creates a default admin user if no users exist
 * in the database.
 */
@Configuration
public class DataInitializer {

  private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

  /**
   * Creates default users on application startup if the database is empty.
   *
   * @param userAccountRepository Repository for user account operations
   * @param passwordEncoder Password encoder for hashing passwords
   * @return CommandLineRunner that executes on startup
   */
  @Bean
  public CommandLineRunner initializeDefaultUsers(
      UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      // Only create default users if no users exist
      if (userAccountRepository.count() == 0) {
        log.info("No users found in database. Creating default admin user...");

        UserAccount admin =
            new UserAccount(
                "admin@test.com",
                passwordEncoder.encode("admin123"),
                "Administrator",
                UserRole.ADMIN);

        userAccountRepository.save(admin);

        log.info("✓ Default admin user created successfully");
        log.info("  Email: admin@test.com");
        log.info("  Password: admin123");
        log.info("  Role: ADMIN");
        log.info("");
        log.info("⚠️  SECURITY WARNING: Change the default password in production!");
      } else {
        log.info("Users already exist in database. Skipping default user creation.");
      }
    };
  }
}
