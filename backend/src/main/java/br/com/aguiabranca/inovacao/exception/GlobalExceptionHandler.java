package br.com.aguiabranca.inovacao.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErrorResponse.CampoInvalido(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return responder(ErrorResponse.of(HttpStatus.BAD_REQUEST, "Dados inválidos", request.getRequestURI(), campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> corpoInvalido(HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou malformado", request);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    ResponseEntity<ErrorResponse> credenciais(CredenciaisInvalidasException ex, HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> acessoNegado(HttpServletRequest request) {
        return responder(HttpStatus.FORBIDDEN, "Seu perfil não tem permissão para esta operação", request);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<ErrorResponse> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponse> rotaInexistente(HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "Recurso não encontrado", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "Método " + ex.getMethod() + " não suportado nesta rota", request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> inesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", request);
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus status, String message, HttpServletRequest request) {
        return responder(ErrorResponse.of(status, message, request.getRequestURI()));
    }

    private ResponseEntity<ErrorResponse> responder(ErrorResponse body) {
        return ResponseEntity.status(body.status()).body(body);
    }
}
