package com.inmobiliaria.gestion.persona.service;

import com.inmobiliaria.gestion.exception.ResourceNotFoundException;
import com.inmobiliaria.gestion.persona.domain.Persona;
import com.inmobiliaria.gestion.persona.domain.PersonaTipo;
import com.inmobiliaria.gestion.persona.dto.CreatePersonaRequest;
import com.inmobiliaria.gestion.persona.dto.PersonaResponse;
import com.inmobiliaria.gestion.persona.dto.UpdatePersonaRequest;
import com.inmobiliaria.gestion.persona.repository.PersonaRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PersonaService {

  private final PersonaRepository personaRepository;

  public PersonaService(PersonaRepository personaRepository) {
    this.personaRepository = personaRepository;
  }

  public List<PersonaResponse> findAll() {
    return personaRepository.findAll().stream().map(this::toResponse).toList();
  }

  public PersonaResponse findById(Long id) {
    return personaRepository.findById(id).map(this::toResponse).orElseThrow(() -> notFound(id));
  }

  @Transactional
  public PersonaResponse create(CreatePersonaRequest request) {
    Persona entity = new Persona();
    applyCreate(entity, request);
    return toResponse(personaRepository.save(entity));
  }

  @Transactional
  public PersonaResponse update(Long id, UpdatePersonaRequest request) {
    Persona entity = personaRepository.findById(id).orElseThrow(() -> notFound(id));
    applyUpdate(entity, request);
    return toResponse(personaRepository.save(entity));
  }

  @Transactional
  public void delete(Long id) {
    if (!personaRepository.existsById(id)) {
      throw notFound(id);
    }
    personaRepository.deleteById(id);
  }

  private void applyCreate(Persona entity, CreatePersonaRequest request) {
    entity.setTipoPersona(request.getTipoPersona());
    entity.setNombre(request.getNombre());
    entity.setApellidos(request.getApellidos());
    entity.setRazonSocial(request.getRazonSocial());
    entity.setRfc(request.getRfc());
    entity.setCurp(request.getCurp());
    entity.setEmail(request.getEmail());
    entity.setTelefono(request.getTelefono());
    entity.setFechaAlta(request.getFechaAlta());
    entity.setActivo(Boolean.TRUE.equals(request.getActivo()));
  }

  private void applyUpdate(Persona entity, UpdatePersonaRequest request) {
    PersonaTipo tipoPersona = request.getTipoPersona();
    if (tipoPersona != null) {
      entity.setTipoPersona(tipoPersona);
    }
    if (request.getNombre() != null) {
      entity.setNombre(request.getNombre());
    }
    if (request.getApellidos() != null) {
      entity.setApellidos(request.getApellidos());
    }
    if (request.getRazonSocial() != null) {
      entity.setRazonSocial(request.getRazonSocial());
    }
    if (request.getRfc() != null) {
      entity.setRfc(request.getRfc());
    }
    if (request.getCurp() != null) {
      entity.setCurp(request.getCurp());
    }
    if (request.getEmail() != null) {
      entity.setEmail(request.getEmail());
    }
    if (request.getTelefono() != null) {
      entity.setTelefono(request.getTelefono());
    }
    if (request.getFechaAlta() != null) {
      entity.setFechaAlta(request.getFechaAlta());
    }
    if (request.getActivo() != null) {
      entity.setActivo(request.getActivo());
    }
  }

  private PersonaResponse toResponse(Persona entity) {
    return new PersonaResponse(
        entity.getId(),
        entity.getTipoPersona(),
        entity.getNombre(),
        entity.getApellidos(),
        entity.getRazonSocial(),
        entity.getRfc(),
        entity.getCurp(),
        entity.getEmail(),
        entity.getTelefono(),
        entity.getFechaAlta(),
        entity.isActivo());
  }

  private ResourceNotFoundException notFound(Long id) {
    return new ResourceNotFoundException("Persona con id %d no encontrada".formatted(id));
  }
}
