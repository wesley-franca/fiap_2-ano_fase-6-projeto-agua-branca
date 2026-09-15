package br.com.aguiabranca.inovacao.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document("projetos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Projeto {

    @Id
    private String id;

    private String nome;

    private String descricao;

    @Indexed
    private String responsavelId;

    private String responsavelNome;

    private String ideiaOrigemId;

    @Indexed
    private String orientacaoId;

    @Builder.Default
    private int etapa = 1;

    @Builder.Default
    private int totalEtapas = 4;

    private int progresso;

    @Indexed
    @Builder.Default
    private StatusProjeto status = StatusProjeto.NO_PRAZO;

    private LocalDate dataInicio;

    private LocalDate prazo;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal investimento;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal retornoFinanceiro;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal custoEvitado;

    /** Aumento de produtividade em pontos percentuais (ex.: 9.4 = +9,4%). */
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal aumentoProdutividade;

    @Builder.Default
    private List<AtualizacaoProjeto> atualizacoes = new ArrayList<>();

    private Instant criadoEm;

    private Instant atualizadoEm;
}
