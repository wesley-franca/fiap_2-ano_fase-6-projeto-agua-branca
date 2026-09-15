package br.com.aguiabranca.inovacao.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document("orientacoes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orientacao {

    @Id
    private String id;

    private String titulo;

    private String descricao;

    private String categoria;

    private String campanha;

    private String area;

    private String periodo;

    @Builder.Default
    private List<String> indicadores = new ArrayList<>();

    private boolean vigente;

    @Builder.Default
    private boolean ativo = true;

    private String criadoPorId;

    private Instant criadoEm;

    private Instant atualizadoEm;

    @Builder.Default
    private List<HistoricoOrientacao> historico = new ArrayList<>();
}
