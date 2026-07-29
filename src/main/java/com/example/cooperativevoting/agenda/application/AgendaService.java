package com.example.cooperativevoting.agenda.application;

import com.example.cooperativevoting.agenda.domain.Agenda;
import com.example.cooperativevoting.agenda.infrastructure.AgendaRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendaService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AgendaService.class);
  private final AgendaRepository repository;
  private final Clock clock;

  public AgendaService(AgendaRepository repository, Clock clock) {
    this.repository = repository;
    this.clock = clock;
  }

  @Transactional
  public Agenda create(String title, String description) {
    var agenda = new Agenda(UUID.randomUUID(), title.trim(), normalize(description), Instant.now(clock));
    var created = repository.save(agenda);
    LOGGER.info("event=agenda.created agendaId={}", created.getId());
    return created;
  }

  @Transactional(readOnly = true)
  public List<Agenda> list() {
    return repository.findAllByOrderByCreatedAtDesc();
  }

  @Transactional(readOnly = true)
  public Agenda get(UUID id) {
    return repository.findById(id).orElseThrow(() -> new AgendaNotFoundException(id));
  }

  private String normalize(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
