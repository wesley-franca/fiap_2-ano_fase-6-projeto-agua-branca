package br.com.aguiabranca.inovacao.service;

import br.com.aguiabranca.inovacao.domain.Ideia;
import br.com.aguiabranca.inovacao.domain.Orientacao;
import br.com.aguiabranca.inovacao.domain.Projeto;
import br.com.aguiabranca.inovacao.domain.StatusIdeia;
import br.com.aguiabranca.inovacao.domain.StatusProjeto;
import br.com.aguiabranca.inovacao.dto.dashboard.IndicadoresResponse;
import br.com.aguiabranca.inovacao.dto.dashboard.PainelGestorResponse;
import br.com.aguiabranca.inovacao.dto.dashboard.ResumoLiderancaResponse;
import br.com.aguiabranca.inovacao.dto.dashboard.ResumoOrientacaoResponse;
import br.com.aguiabranca.inovacao.dto.dashboard.ResumoProjetoResponse;
import br.com.aguiabranca.inovacao.exception.RecursoNaoEncontradoException;
import br.com.aguiabranca.inovacao.repository.IdeiaRepository;
import br.com.aguiabranca.inovacao.repository.OrientacaoRepository;
import br.com.aguiabranca.inovacao.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Consolida os números exibidos nos painéis. O volume de dados é pequeno (projetos e ideias de uma
 * planta industrial), então os cálculos são feitos em memória com BigDecimal, mantendo a precisão
 * dos valores financeiros.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int ESCALA = 2;

    private final ProjetoRepository projetoRepository;
    private final IdeiaRepository ideiaRepository;
    private final OrientacaoRepository orientacaoRepository;

    public PainelGestorResponse painelDoGestor() {
        List<Ideia> ideias = ideiaRepository.findAll();
        List<Projeto> projetos = projetoRepository.findAll();

        return new PainelGestorResponse(
                contarIdeias(ideias, StatusIdeia.ENVIADA),
                ideias.stream().filter(i -> i.getStatus() == StatusIdeia.TRIAGEM
                        || i.getStatus() == StatusIdeia.ANALISE
                        || i.getStatus() == StatusIdeia.DECISAO).count(),
                contarIdeias(ideias, StatusIdeia.PROJETO),
                contarIdeias(ideias, StatusIdeia.REJEITADA),
                projetos.stream().filter(DashboardService::ativo).count(),
                projetos.stream().filter(p -> p.getStatus() == StatusProjeto.ATRASADO).count());
    }

    public ResumoLiderancaResponse resumoDaLideranca() {
        List<Projeto> projetos = projetoRepository.findAll();
        List<Ideia> ideias = ideiaRepository.findAll();

        Map<String, Long> projetosPorStatus = new LinkedHashMap<>();
        for (StatusProjeto status : StatusProjeto.values()) {
            projetosPorStatus.put(status.name(), projetos.stream().filter(p -> p.getStatus() == status).count());
        }
        Map<String, Long> ideiasPorStatus = new LinkedHashMap<>();
        for (StatusIdeia status : StatusIdeia.values()) {
            ideiasPorStatus.put(status.name(), contarIdeias(ideias, status));
        }

        return new ResumoLiderancaResponse(
                indicadores(projetos),
                projetosPorStatus,
                ideiasPorStatus,
                porOrientacao(projetos, ideias));
    }

    public List<ResumoOrientacaoResponse> porOrientacao() {
        return porOrientacao(projetoRepository.findAll(), ideiaRepository.findAll());
    }

    public ResumoOrientacaoResponse porOrientacao(String orientacaoId) {
        Orientacao orientacao = orientacaoRepository.findById(orientacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orientação não encontrada"));
        return resumir(orientacao, projetoRepository.findAll(), ideiaRepository.findAll());
    }

    public ResumoProjetoResponse porProjeto(String projetoId) {
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado"));
        Map<String, Orientacao> orientacoes = orientacoesPorId();
        return resumir(projeto, orientacoes);
    }

    private List<ResumoOrientacaoResponse> porOrientacao(List<Projeto> projetos, List<Ideia> ideias) {
        return orientacaoRepository.findByAtivoTrueOrderByCriadoEmDesc().stream()
                .map(orientacao -> resumir(orientacao, projetos, ideias))
                .sorted(Comparator.comparing((ResumoOrientacaoResponse r) -> r.indicadores().lucro()).reversed())
                .toList();
    }

    private ResumoOrientacaoResponse resumir(Orientacao orientacao, List<Projeto> projetos, List<Ideia> ideias) {
        List<Projeto> daOrientacao = projetos.stream()
                .filter(p -> orientacao.getId().equals(p.getOrientacaoId()))
                .toList();
        long ideiasVinculadas = ideias.stream()
                .filter(i -> orientacao.getId().equals(i.getOrientacaoId()))
                .count();

        return new ResumoOrientacaoResponse(
                orientacao.getId(),
                orientacao.getTitulo(),
                orientacao.getCampanha(),
                orientacao.getArea(),
                ideiasVinculadas,
                indicadores(daOrientacao));
    }

    private ResumoProjetoResponse resumir(Projeto projeto, Map<String, Orientacao> orientacoes) {
        BigDecimal investimento = valor(projeto.getInvestimento());
        BigDecimal retorno = valor(projeto.getRetornoFinanceiro());
        Orientacao orientacao = orientacoes.get(projeto.getOrientacaoId());

        return new ResumoProjetoResponse(
                projeto.getId(),
                projeto.getNome(),
                projeto.getStatus(),
                projeto.getEtapa(),
                projeto.getTotalEtapas(),
                projeto.getProgresso(),
                projeto.getPrazo(),
                projeto.getOrientacaoId(),
                orientacao == null ? null : orientacao.getTitulo(),
                investimento,
                retorno,
                retorno.subtract(investimento),
                roi(investimento, retorno),
                valor(projeto.getCustoEvitado()),
                valor(projeto.getAumentoProdutividade()));
    }

    private IndicadoresResponse indicadores(List<Projeto> projetos) {
        BigDecimal investimento = somar(projetos, Projeto::getInvestimento);
        BigDecimal retorno = somar(projetos, Projeto::getRetornoFinanceiro);
        long ativos = projetos.stream().filter(DashboardService::ativo).count();
        long noPrazo = projetos.stream().filter(p -> p.getStatus() == StatusProjeto.NO_PRAZO).count();
        long concluidos = projetos.stream().filter(p -> p.getStatus() == StatusProjeto.CONCLUIDO).count();

        return new IndicadoresResponse(
                projetos.size(),
                ativos,
                noPrazo,
                concluidos,
                percentual(noPrazo, projetos.size()),
                investimento,
                retorno,
                retorno.subtract(investimento),
                roi(investimento, retorno),
                somar(projetos, Projeto::getCustoEvitado),
                media(projetos, Projeto::getAumentoProdutividade));
    }

    private Map<String, Orientacao> orientacoesPorId() {
        return orientacaoRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Orientacao::getId, Function.identity(), (a, b) -> a));
    }

    private static boolean ativo(Projeto projeto) {
        return projeto.getStatus() == StatusProjeto.NO_PRAZO || projeto.getStatus() == StatusProjeto.ATRASADO;
    }

    private static long contarIdeias(List<Ideia> ideias, StatusIdeia status) {
        return ideias.stream().filter(i -> i.getStatus() == status).count();
    }

    private static BigDecimal somar(List<Projeto> projetos, Function<Projeto, BigDecimal> campo) {
        return projetos.stream()
                .map(campo)
                .map(DashboardService::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(ESCALA, RoundingMode.HALF_UP);
    }

    private static BigDecimal media(List<Projeto> projetos, Function<Projeto, BigDecimal> campo) {
        if (projetos.isEmpty()) {
            return BigDecimal.ZERO.setScale(ESCALA, RoundingMode.HALF_UP);
        }
        return somar(projetos, campo).divide(BigDecimal.valueOf(projetos.size()), ESCALA, RoundingMode.HALF_UP);
    }

    /** ROI = (retorno − investimento) / investimento. Sem investimento registrado, não há ROI. */
    private static BigDecimal roi(BigDecimal investimento, BigDecimal retorno) {
        if (investimento.signum() == 0) {
            return null;
        }
        return retorno.subtract(investimento).divide(investimento, ESCALA, RoundingMode.HALF_UP);
    }

    private static BigDecimal percentual(long parte, long total) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(ESCALA, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(parte * 100L).divide(BigDecimal.valueOf(total), ESCALA, RoundingMode.HALF_UP);
    }

    private static BigDecimal valor(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
