package com.inmobiliaria.gestion.inmobiliaria.service;

import com.inmobiliaria.gestion.exception.ResourceNotFoundException;
import com.inmobiliaria.gestion.inmobiliaria.domain.Inmobiliaria;
import com.inmobiliaria.gestion.inmobiliaria.dto.CreateInmobiliariaRequest;
import com.inmobiliaria.gestion.inmobiliaria.dto.InmobiliariaResponse;
import com.inmobiliaria.gestion.inmobiliaria.dto.UpdateInmobiliariaRequest;
import com.inmobiliaria.gestion.inmobiliaria.repository.InmobiliariaRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class InmobiliariaService {

  private final InmobiliariaRepository inmobiliariaRepository;

  public InmobiliariaService(InmobiliariaRepository inmobiliariaRepository) {
    this.inmobiliariaRepository = inmobiliariaRepository;
  }

  public List<InmobiliariaResponse> findAll() {
    return inmobiliariaRepository.findAll().stream().map(this::toResponse).toList();
  }

  public InmobiliariaResponse findById(Long id) {
    return inmobiliariaRepository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Inmobiliaria con id %d no encontrada".formatted(id)));
  }

  @Transactional
  public InmobiliariaResponse create(CreateInmobiliariaRequest request) {
    Inmobiliaria entity = new Inmobiliaria();
    applyRequest(
        entity,
        request.getNombre(),
        request.getRfc(),
        request.getNombreContacto(),
        request.getCorreo(),
        request.getTelefono());
    return toResponse(inmobiliariaRepository.save(entity));
  }

  @Transactional
  public InmobiliariaResponse update(Long id, UpdateInmobiliariaRequest request) {
    Inmobiliaria entity =
        inmobiliariaRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Inmobiliaria con id %d no encontrada".formatted(id)));
    applyRequest(
        entity,
        request.getNombre(),
        request.getRfc(),
        request.getNombreContacto(),
        request.getCorreo(),
        request.getTelefono());
    return toResponse(inmobiliariaRepository.save(entity));
  }

  @Transactional
  public void delete(Long id) {
    if (!inmobiliariaRepository.existsById(id)) {
      throw new ResourceNotFoundException("Inmobiliaria con id %d no encontrada".formatted(id));
    }
    inmobiliariaRepository.deleteById(id);
  }

  private void applyRequest(
      Inmobiliaria entity,
      String nombre,
      String rfc,
      String nombreContacto,
      String correo,
      String telefono) {
    if (nombre != null) {
      entity.setNombre(nombre);
    }
    if (rfc != null) {
      entity.setRfc(rfc);
    }
    if (nombreContacto != null) {
      entity.setNombreContacto(nombreContacto);
    }
    if (correo != null) {
      entity.setCorreo(correo);
    }
    if (telefono != null) {
      entity.setTelefono(telefono);
    }
  }

  private InmobiliariaResponse toResponse(Inmobiliaria entity) {
    return new InmobiliariaResponse(
        entity.getId(),
        entity.getNombre(),
        entity.getRfc(),
        entity.getNombreContacto(),
        entity.getCorreo(),
        entity.getTelefono());
  }
}
