package br.com.aguiabranca.inovacao.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("ideias")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ideia {

    @Id
    private String id;

    private String titulo;

    private String categoria;

    private String problemaObservado;

    private String suaProposta;

    @Builder.Default
    private Impacto impacto = Impacto.MEDIO;

    @Indexed
    @Builder.Default
    private StatusIdeia status = StatusIdeia.ENVIADA;

    @Builder.Default
    private PrioridadeIdeia prioridade = PrioridadeIdeia.MEDIA;

    @Indexed
    private String operadorId;

    private String nomeOperador;

    private String area;

    @Indexed
    private String orientacaoId;

    private String avaliadoPorId;

    private String comentarioAvaliacao;

    private Instant criadoEm;

    private Instant atualizadoEm;
}
