package com.therionlabs.msformulario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa una preferencia de carrera ordenada por prioridad (1 a 3) dentro del cupón.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarreraPreferencia {

    @Column(nullable = false)
    private Integer prioridad;

    @Column(name = "nombre_carrera", nullable = false, length = 150)
    private String nombreCarrera;
}
