package br.com.aguiabranca.inovacao.exception;

/** Operação válida em si, mas incompatível com o estado atual do recurso (ex.: transição de status). */
public class OperacaoInvalidaException extends RuntimeException {

    public OperacaoInvalidaException(String message) {
        super(message);
    }
}
