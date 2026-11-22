package com.therionlabs.msformulario.repository;

import com.therionlabs.msformulario.model.Prospecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProspectoRepository extends JpaRepository<Prospecto, String> {

    Optional<Prospecto> findByRut(String rut);

    boolean existsByRut(String rut);

    @Query("""
            SELECT p FROM Prospecto p
            WHERE (:captadorId IS NULL OR p.captadorId = :captadorId)
              AND (:eventoId IS NULL OR p.eventoId = :eventoId)
              AND (:rut IS NULL OR p.rut = :rut)
              AND (:sedeInteres IS NULL OR LOWER(p.sedeInteres) = LOWER(:sedeInteres))
            ORDER BY p.timestampEpoch DESC
            """)
    List<Prospecto> findFiltered(
            @Param("captadorId") Integer captadorId,
            @Param("eventoId") String eventoId,
            @Param("rut") String rut,
            @Param("sedeInteres") String sedeInteres
    );

    @Query("""
            SELECT COUNT(p) FROM Prospecto p
            WHERE (:eventoId IS NULL OR p.eventoId = :eventoId)
            """)
    long countByOptionalEventoId(@Param("eventoId") String eventoId);

    @Query("""
            SELECT p.sedeInteres, COUNT(p) FROM Prospecto p
            WHERE (:eventoId IS NULL OR p.eventoId = :eventoId)
            GROUP BY p.sedeInteres
            """)
    List<Object[]> countGroupedBySede(@Param("eventoId") String eventoId);

    @Query("""
            SELECT p.nivelInteres, COUNT(p) FROM Prospecto p
            WHERE (:eventoId IS NULL OR p.eventoId = :eventoId)
            GROUP BY p.nivelInteres
            """)
    List<Object[]> countGroupedByNivelInteres(@Param("eventoId") String eventoId);

    @Query("""
            SELECT p.jornadaPreferencia, COUNT(p) FROM Prospecto p
            WHERE (:eventoId IS NULL OR p.eventoId = :eventoId)
            GROUP BY p.jornadaPreferencia
            """)
    List<Object[]> countGroupedByJornada(@Param("eventoId") String eventoId);
}
