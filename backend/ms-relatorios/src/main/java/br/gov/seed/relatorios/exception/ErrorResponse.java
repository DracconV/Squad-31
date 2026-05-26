package br.gov.seed.relatorios.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    LocalDateTime timestamp,
    Integer status,
    String error,
    String message,
    String path
) {}

