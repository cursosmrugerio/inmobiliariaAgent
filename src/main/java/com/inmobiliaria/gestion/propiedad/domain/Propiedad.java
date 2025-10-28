package com.inmobiliaria.gestion.propiedad.domain;

import com.inmobiliaria.gestion.inmobiliaria.domain.Inmobiliaria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "propiedades")
public class Propiedad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String nombre;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private PropiedadTipo tipo;

  private String direccion;

  @Column(columnDefinition = "TEXT")
  private String observaciones;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "inmobiliaria_id", nullable = false)
  private Inmobiliaria inmobiliaria;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public PropiedadTipo getTipo() {
    return tipo;
  }

  public void setTipo(PropiedadTipo tipo) {
    this.tipo = tipo;
  }

  public String getDireccion() {
    return direccion;
  }

  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

  public Inmobiliaria getInmobiliaria() {
    return inmobiliaria;
  }

  public void setInmobiliaria(Inmobiliaria inmobiliaria) {
    this.inmobiliaria = inmobiliaria;
  }
}
