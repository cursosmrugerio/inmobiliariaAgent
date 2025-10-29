package com.inmobiliaria.gestion.auth.service;

import com.inmobiliaria.gestion.auth.domain.UserAccount;
import com.inmobiliaria.gestion.auth.repository.UserAccountRepository;
import com.inmobiliaria.gestion.exception.ResourceNotFoundException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserAccountService {

  private static final Logger log = LoggerFactory.getLogger(UserAccountService.class);
  private final UserAccountRepository repository;

  public UserAccountService(UserAccountRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public UserAccount create(UserAccount userAccount) {
    log.info("Creando nuevo usuario {}", userAccount.getEmail());
    return repository.save(userAccount);
  }

  public Optional<UserAccount> findByEmail(String email) {
    return repository.findByEmailIgnoreCase(email);
  }

  public boolean existsByEmail(String email) {
    return repository.existsByEmailIgnoreCase(email);
  }

  public UserAccount getById(Long id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
  }
}
