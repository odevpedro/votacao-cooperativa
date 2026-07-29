package com.example.cooperativevoting.shared.error;

import com.example.cooperativevoting.agenda.application.AgendaNotFoundException;
import com.example.cooperativevoting.eligibility.application.EligibilityServiceUnavailableException;
import com.example.cooperativevoting.eligibility.application.InvalidCpfException;
import com.example.cooperativevoting.eligibility.application.VoterNotEligibleException;
import com.example.cooperativevoting.shared.observability.CorrelationIdFilter;
import com.example.cooperativevoting.vote.application.DuplicateVoteException;
import com.example.cooperativevoting.votingsession.application.SessionAlreadyExistsException;
import com.example.cooperativevoting.votingsession.application.SessionNotFoundException;
import com.example.cooperativevoting.votingsession.application.SessionNotOpenException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ApiExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);
  private static final String PROBLEM_BASE = "https://cooperative-voting.example/problems/";
  private final Clock clock;

  public ApiExceptionHandler(Clock clock) {
    this.clock = clock;
  }

  @ExceptionHandler(AgendaNotFoundException.class)
  ProblemDetail agendaNotFound(AgendaNotFoundException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.NOT_FOUND,
        "Pauta não encontrada",
        exception.getMessage(),
        "AGENDA_NOT_FOUND",
        "agenda-not-found",
        request);
  }

  @ExceptionHandler(SessionNotFoundException.class)
  ProblemDetail sessionNotFound(SessionNotFoundException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.CONFLICT,
        "Sessão inexistente",
        exception.getMessage(),
        "SESSION_NOT_FOUND",
        "session-not-found",
        request);
  }

  @ExceptionHandler(SessionAlreadyExistsException.class)
  ProblemDetail sessionExists(SessionAlreadyExistsException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.CONFLICT,
        "Sessão já existente",
        exception.getMessage(),
        "SESSION_ALREADY_EXISTS",
        "session-already-exists",
        request);
  }

  @ExceptionHandler(SessionNotOpenException.class)
  ProblemDetail sessionNotOpen(SessionNotOpenException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.CONFLICT,
        "Sessão indisponível para votação",
        exception.getMessage(),
        "SESSION_NOT_OPEN",
        "session-not-open",
        request);
  }

  @ExceptionHandler(DuplicateVoteException.class)
  ProblemDetail duplicateVote(DuplicateVoteException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.CONFLICT,
        "Voto já registrado",
        exception.getMessage(),
        "DUPLICATE_VOTE",
        "duplicate-vote",
        request);
  }

  @ExceptionHandler({VoterNotEligibleException.class, InvalidCpfException.class})
  ProblemDetail unprocessable(RuntimeException exception, HttpServletRequest request) {
    String invalid =
        exception instanceof InvalidCpfException ? "INVALID_CPF" : "VOTER_NOT_ELIGIBLE";
    String slug = exception instanceof InvalidCpfException ? "invalid-cpf" : "voter-not-eligible";
    return problem(
        HttpStatus.UNPROCESSABLE_ENTITY,
        "Associado não habilitado",
        exception.getMessage(),
        invalid,
        slug,
        request);
  }

  @ExceptionHandler(EligibilityServiceUnavailableException.class)
  ProblemDetail eligibilityUnavailable(
      EligibilityServiceUnavailableException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.SERVICE_UNAVAILABLE,
        "Serviço de elegibilidade indisponível",
        exception.getMessage(),
        "ELIGIBILITY_UNAVAILABLE",
        "eligibility-unavailable",
        request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
    Map<String, String> errors =
        exception.getBindingResult().getFieldErrors().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    FieldError::getField,
                    error -> String.valueOf(error.getDefaultMessage()),
                    (first, ignored) -> first));
    var detail =
        problem(
            HttpStatus.BAD_REQUEST,
            "Requisição inválida",
            "Um ou mais campos são inválidos.",
            "VALIDATION_ERROR",
            "validation-error",
            request);
    detail.setProperty("errors", errors);
    return detail;
  }

  @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
  ProblemDetail badRequest(Exception exception, HttpServletRequest request) {
    return problem(
        HttpStatus.BAD_REQUEST,
        "Requisição inválida",
        "O corpo da requisição está ausente ou contém valores inválidos.",
        "INVALID_REQUEST",
        "invalid-request",
        request);
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail unexpected(Exception exception, HttpServletRequest request) {
    LOGGER.error("event=request.unexpected_error path={}", request.getRequestURI(), exception);
    return problem(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Erro interno",
        "Ocorreu um erro inesperado.",
        "INTERNAL_ERROR",
        "internal-error",
        request);
  }

  private ProblemDetail problem(
      HttpStatus status,
      String title,
      String detail,
      String code,
      String slug,
      HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    problem.setType(URI.create(PROBLEM_BASE + slug));
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("code", code);
    problem.setProperty("timestamp", Instant.now(clock));
    problem.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
    return problem;
  }
}
