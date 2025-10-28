package com.inmobiliaria.gestion.propiedad.service;

import com.inmobiliaria.gestion.exception.ResourceNotFoundException;
import com.inmobiliaria.gestion.inmobiliaria.domain.Inmobiliaria;
import com.inmobiliaria.gestion.inmobiliaria.repository.InmobiliariaRepository;
import com.inmobiliaria.gestion.propiedad.domain.Propiedad;
import com.inmobiliaria.gestion.propiedad.domain.PropiedadTipo;
import com.inmobiliaria.gestion.propiedad.dto.CreatePropiedadRequest;
import com.inmobiliaria.gestion.propiedad.dto.PropiedadResponse;
import com.inmobiliaria.gestion.propiedad.dto.UpdatePropiedadRequest;
import com.inmobiliaria.gestion.propiedad.repository.PropiedadRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PropiedadService {

  private final PropiedadRepository propiedadRepository;
  private final InmobiliariaRepository inmobiliariaRepository;

  public PropiedadService(
      PropiedadRepository propiedadRepository, InmobiliariaRepository inmobiliariaRepository) {
    this.propiedadRepository = propiedadRepository;
    this.inmobiliariaRepository = inmobiliariaRepository;
  }

  public List<PropiedadResponse> findAll() {
    return propiedadRepository.findAll().stream().map(this::toResponse).toList();
  }

  public List<PropiedadResponse> findAllByInmobiliaria(Long inmobiliariaId) {
    resolveInmobiliaria(inmobiliariaId);
    return propiedadRepository.findByInmobiliariaId(inmobiliariaId).stream()
        .map(this::toResponse)
        .toList();
  }

  public PropiedadResponse findById(Long id) {
    return propiedadRepository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(
            () -> new ResourceNotFoundException("Propiedad con id %d no encontrada".formatted(id)));
  }

  @Transactional
  public PropiedadResponse create(CreatePropiedadRequest request) {
    Inmobiliaria inmobiliaria = resolveInmobiliaria(request.getInmobiliariaId());
    Propiedad entity = new Propiedad();
    entity.setInmobiliaria(inmobiliaria);
    applyChanges(
        entity,
        request.getNombre(),
        request.getTipo(),
        request.getDireccion(),
        request.getObservaciones());
    return toResponse(propiedadRepository.save(entity));
  }

  @Transactional
  public PropiedadResponse update(Long id, UpdatePropiedadRequest request) {
    Propiedad entity =
        propiedadRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Propiedad con id %d no encontrada".formatted(id)));

    if (request.getInmobiliariaId() != null) {
      Inmobiliaria inmobiliaria = resolveInmobiliaria(request.getInmobiliariaId());
      entity.setInmobiliaria(inmobiliaria);
    }

    applyChanges(
        entity,
        request.getNombre(),
        request.getTipo(),
        request.getDireccion(),
        request.getObservaciones());
    return toResponse(propiedadRepository.save(entity));
  }

  @Transactional
  public void delete(Long id) {
    if (!propiedadRepository.existsById(id)) {
      throw new ResourceNotFoundException("Propiedad con id %d no encontrada".formatted(id));
    }
    propiedadRepository.deleteById(id);
  }

  private void applyChanges(
      Propiedad entity, String nombre, PropiedadTipo tipo, String direccion, String observaciones) {
    if (nombre != null) {
      entity.setNombre(nombre);
    }
    if (tipo != null) {
      entity.setTipo(tipo);
    }
    if (direccion != null) {
      entity.setDireccion(direccion);
    }
    if (observaciones != null) {
      entity.setObservaciones(observaciones);
    }
  }

  private PropiedadResponse toResponse(Propiedad entity) {
    Inmobiliaria inmobiliaria = entity.getInmobiliaria();
    return new PropiedadResponse(
        entity.getId(),
        entity.getNombre(),
        entity.getTipo(),
        entity.getDireccion(),
        entity.getObservaciones(),
        inmobiliaria != null ? inmobiliaria.getId() : null,
        inmobiliaria != null ? inmobiliaria.getNombre() : null);
  }

  private Inmobiliaria resolveInmobiliaria(Long inmobiliariaId) {
    return inmobiliariaRepository
        .findById(inmobiliariaId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Inmobiliaria con id %d no encontrada".formatted(inmobiliariaId)));
  }
}
