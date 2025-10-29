package com.inmobiliaria.gestion.persona.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "personas")
public class Persona {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_persona")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_persona", nullable = false, length = 10)
  private PersonaTipo tipoPersona;

  @Column(length = 150)
  private String nombre;

  @Column(length = 150)
  private String apellidos;

  @Column(name = "razon_social", length = 200)
  private String razonSocial;

  @Column(length = 13)
  private String rfc;

  @Column(length = 18)
  private String curp;

  private String email;

  private String telefono;

  @Column(name = "fecha_alta", nullable = false)
  private LocalDateTime fechaAlta;

  @Column(nullable = false)
  private boolean activo;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public PersonaTipo getTipoPersona() {
    return tipoPersona;
  }

  public void setTipoPersona(PersonaTipo tipoPersona) {
    this.tipoPersona = tipoPersona;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getApellidos() {
    return apellidos;
  }

  public void setApellidos(String apellidos) {
    this.apellidos = apellidos;
  }

  public String getRazonSocial() {
    return razonSocial;
  }

  public void setRazonSocial(String razonSocial) {
    this.razonSocial = razonSocial;
  }

  public String getRfc() {
    return rfc;
  }

  public void setRfc(String rfc) {
    this.rfc = rfc;
  }

  public String getCurp() {
    return curp;
  }

  public void setCurp(String curp) {
    this.curp = curp;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public LocalDateTime getFechaAlta() {
    return fechaAlta;
  }

  public void setFechaAlta(LocalDateTime fechaAlta) {
    this.fechaAlta = fechaAlta;
  }

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }
}
