package br.com.aguiabranca.inovacao.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<CampoInvalido> fields
) {

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return of(status, message, path, List.of());
    }

    public static ErrorResponse of(HttpStatus status, String message, String path, List<CampoInvalido> fields) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path, fields);
    }

    public record CampoInvalido(String field, String message) {
    }
}
