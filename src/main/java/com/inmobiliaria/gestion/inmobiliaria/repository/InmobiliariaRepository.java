package com.inmobiliaria.gestion.inmobiliaria.repository;

import com.inmobiliaria.gestion.inmobiliaria.domain.Inmobiliaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InmobiliariaRepository extends JpaRepository<Inmobiliaria, Long> {}
