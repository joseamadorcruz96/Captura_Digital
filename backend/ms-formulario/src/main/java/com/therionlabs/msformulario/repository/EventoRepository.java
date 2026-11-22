package com.therionlabs.msformulario.repository;

import com.therionlabs.msformulario.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, String> {

    List<Evento> findByActivoTrueOrderByCreatedAtDesc();
}
