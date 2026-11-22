package com.therionlabs.msformulario.model;

import com.therionlabs.msformulario.model.enums.Genero;
import com.therionlabs.msformulario.model.enums.Jornada;
import com.therionlabs.msformulario.model.enums.NivelInteres;
import com.therionlabs.msformulario.model.enums.PerfilMotivacional;
import com.therionlabs.msformulario.model.enums.SituacionAcademica;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA espejo de Prospecto.kt (cupón de admisión Duoc UC).
 * Utiliza clientUuid como identificador único compartido con la app móvil Offline-First.
 */
@Entity
@Table(
        name = "prospectos",
        indexes = {
                @Index(name = "idx_prospectos_rut", columnList = "rut", unique = true),
                @Index(name = "idx_prospectos_captador", columnList = "captador_id"),
                @Index(name = "idx_prospectos_evento", columnList = "evento_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Prospecto {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "client_uuid", length = 36)
    private String clientUuid;

    @Column(name = "captador_id", nullable = false)
    private Integer captadorId;

    @Column(name = "evento_id", length = 80)
    private String eventoId;

    @Column(name = "timestamp_epoch", nullable = false)
    private Long timestampEpoch;

    @Builder.Default
    @Column(name = "consentimiento_datos", nullable = false)
    private boolean consentimientoDatos = true;

    @Column(nullable = false, unique = true, length = 15)
    private String rut;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genero genero = Genero.NO_INDICA;

    @Column(name = "fecha_nacimiento", length = 20)
    private String fechaNacimiento;

    @Column(name = "comuna_residencia", nullable = false, length = 100)
    private String comunaResidencia;

    @Column(nullable = false, length = 20)
    private String celular;

    @Column(nullable = false, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacion_academica", nullable = false, length = 30)
    private SituacionAcademica situacionAcademica;

    @Column(name = "situacion_academica_otro", length = 150)
    private String situacionAcademicaOtro;

    @Column(name = "establecimiento_educacional", nullable = false, length = 150)
    private String establecimientoEducacional;

    @Column(name = "comuna_establecimiento", nullable = false, length = 100)
    private String comunaEstablecimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "jornada_preferencia", nullable = false, length = 20)
    private Jornada jornadaPreferencia;

    @Column(name = "sede_interes", nullable = false, length = 100)
    private String sedeInteres;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_interes", nullable = false, length = 20)
    private NivelInteres nivelInteres;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_motivacional", nullable = false, length = 30)
    private PerfilMotivacional perfilMotivacional;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "prospecto_carreras", joinColumns = @JoinColumn(name = "prospecto_uuid"))
    @OrderBy("prioridad ASC")
    private List<CarreraPreferencia> carreras = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void setCarreras(List<CarreraPreferencia> carreras) {
        if (this.carreras == null) {
            this.carreras = new ArrayList<>();
        }
        this.carreras.clear();
        if (carreras != null) {
            this.carreras.addAll(carreras);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.clientUuid == null || this.clientUuid.isBlank()) {
            this.clientUuid = UUID.randomUUID().toString();
        }
        if (this.timestampEpoch == null) {
            this.timestampEpoch = System.currentTimeMillis();
        }
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
