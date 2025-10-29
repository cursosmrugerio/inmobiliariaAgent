package com.inmobiliaria.gestion.persona.repository;

import com.inmobiliaria.gestion.persona.domain.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {}
