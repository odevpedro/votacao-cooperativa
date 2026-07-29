package com.example.cooperativevoting.agenda.infrastructure;

import com.example.cooperativevoting.agenda.domain.Agenda;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendaRepository extends JpaRepository<Agenda, UUID> {
  List<Agenda> findAllByOrderByCreatedAtDesc();
}
