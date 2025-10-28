package com.inmobiliaria.gestion.propiedad.repository;

import com.inmobiliaria.gestion.propiedad.domain.Propiedad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropiedadRepository extends JpaRepository<Propiedad, Long> {
  List<Propiedad> findByInmobiliariaId(Long inmobiliariaId);
}
