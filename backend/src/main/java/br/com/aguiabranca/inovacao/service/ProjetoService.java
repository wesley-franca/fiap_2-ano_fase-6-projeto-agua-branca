package br.com.aguiabranca.inovacao.service;

import br.com.aguiabranca.inovacao.domain.AtualizacaoProjeto;
import br.com.aguiabranca.inovacao.domain.Ideia;
import br.com.aguiabranca.inovacao.domain.Projeto;
import br.com.aguiabranca.inovacao.domain.StatusIdeia;
import br.com.aguiabranca.inovacao.domain.StatusProjeto;
import br.com.aguiabranca.inovacao.dto.projeto.ProgressoRequest;
import br.com.aguiabranca.inovacao.dto.projeto.ProjetoRequest;
import br.com.aguiabranca.inovacao.dto.projeto.ProjetoResponse;
import br.com.aguiabranca.inovacao.dto.projeto.ResultadosRequest;
import br.com.aguiabranca.inovacao.exception.OperacaoInvalidaException;
import br.com.aguiabranca.inovacao.exception.RecursoNaoEncontradoException;
import br.com.aguiabranca.inovacao.repository.IdeiaRepository;
import br.com.aguiabranca.inovacao.repository.ProjetoRepository;
import br.com.aguiabranca.inovacao.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoRepository repository;
    private final IdeiaRepository ideiaRepository;
    private final OrientacaoService orientacaoService;

    /** Gestor e liderança enxergam todos os projetos; os filtros são opcionais. */
    public List<ProjetoResponse> listar(StatusProjeto status, String orientacaoId) {
        return repository.findAllByOrderByCriadoEmDesc().stream()
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> orientacaoId == null || orientacaoId.isBlank() || orientacaoId.equals(p.getOrientacaoId()))
                .map(ProjetoResponse::from)
                .toList();
    }

    public ProjetoResponse buscar(String id) {
        return ProjetoResponse.from(buscarProjeto(id));
    }

    public ProjetoResponse criar(ProjetoRequest request, UsuarioAutenticado gestor) {
        orientacaoService.buscarAtiva(request.orientacaoId());
        String ideiaOrigemId = vincularIdeia(request.ideiaOrigemId(), gestor);

        Instant agora = Instant.now();
        Projeto projeto = Projeto.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .orientacaoId(request.orientacaoId())
                .ideiaOrigemId(ideiaOrigemId)
                .responsavelId(gestor.id())
                .responsavelNome(gestor.nome())
                .etapa(1)
                .totalEtapas(request.totalEtapasOuPadrao())
                .progresso(0)
                .status(StatusProjeto.NO_PRAZO)
                .dataInicio(request.dataInicio())
                .prazo(request.prazo())
                .investimento(request.investimento())
                .atualizacoes(new ArrayList<>())
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build();
        return ProjetoResponse.from(repository.save(projeto));
    }

    public ProjetoResponse atualizar(String id, ProjetoRequest request, UsuarioAutenticado gestor) {
        Projeto projeto = buscarProjeto(id);
        orientacaoService.buscarAtiva(request.orientacaoId());

        if (request.totalEtapasOuPadrao() < projeto.getEtapa()) {
            throw new OperacaoInvalidaException("O total de etapas não pode ser menor que a etapa atual ("
                    + projeto.getEtapa() + ")");
        }

        projeto.setNome(request.nome());
        projeto.setDescricao(request.descricao());
        projeto.setOrientacaoId(request.orientacaoId());
        projeto.setTotalEtapas(request.totalEtapasOuPadrao());
        projeto.setDataInicio(request.dataInicio());
        projeto.setPrazo(request.prazo());
        projeto.setInvestimento(request.investimento());
        projeto.setAtualizadoEm(Instant.now());
        return ProjetoResponse.from(repository.save(projeto));
    }

    public ProjetoResponse registrarProgresso(String id, ProgressoRequest request, UsuarioAutenticado gestor) {
        Projeto projeto = buscarProjeto(id);
        if (request.etapa() > projeto.getTotalEtapas()) {
            throw new OperacaoInvalidaException("Etapa " + request.etapa() + " excede o total de "
                    + projeto.getTotalEtapas() + " etapas");
        }

        Instant agora = Instant.now();
        projeto.setEtapa(request.etapa());
        projeto.setProgresso(request.progresso());
        if (request.status() != null) {
            projeto.setStatus(request.status());
        }
        List<AtualizacaoProjeto> atualizacoes = new ArrayList<>(projeto.getAtualizacoes());
        atualizacoes.add(new AtualizacaoProjeto(agora, projeto.getEtapa(), projeto.getProgresso(),
                projeto.getStatus(), request.observacao(), gestor.id(), gestor.nome()));
        projeto.setAtualizacoes(atualizacoes);
        projeto.setAtualizadoEm(agora);
        return ProjetoResponse.from(repository.save(projeto));
    }

    public ProjetoResponse registrarResultados(String id, ResultadosRequest request, UsuarioAutenticado gestor) {
        Projeto projeto = buscarProjeto(id);
        projeto.setRetornoFinanceiro(request.retornoFinanceiro());
        projeto.setCustoEvitado(request.custoEvitado());
        projeto.setAumentoProdutividade(request.aumentoProdutividade());
        projeto.setAtualizadoEm(Instant.now());
        return ProjetoResponse.from(repository.save(projeto));
    }

    public void excluir(String id) {
        repository.delete(buscarProjeto(id));
    }

    private Projeto buscarProjeto(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado"));
    }

    /** Ao originar um projeto, a ideia sai de DECISAO para PROJETO e não pode originar outro. */
    private String vincularIdeia(String ideiaOrigemId, UsuarioAutenticado gestor) {
        if (ideiaOrigemId == null || ideiaOrigemId.isBlank()) {
            return null;
        }
        Ideia ideia = ideiaRepository.findById(ideiaOrigemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ideia de origem não encontrada"));

        if (repository.existsByIdeiaOrigemId(ideiaOrigemId)) {
            throw new OperacaoInvalidaException("Esta ideia já originou um projeto");
        }
        if (ideia.getStatus() != StatusIdeia.DECISAO && ideia.getStatus() != StatusIdeia.PROJETO) {
            throw new OperacaoInvalidaException("A ideia precisa estar em DECISAO para virar projeto (status atual: "
                    + ideia.getStatus() + ")");
        }
        if (ideia.getStatus() == StatusIdeia.DECISAO) {
            ideia.setStatus(StatusIdeia.PROJETO);
            ideia.setAvaliadoPorId(gestor.id());
            ideia.setAtualizadoEm(Instant.now());
            ideiaRepository.save(ideia);
        }
        return ideia.getId();
    }
}
