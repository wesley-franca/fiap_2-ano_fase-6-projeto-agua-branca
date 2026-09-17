package br.com.aguiabranca.inovacao.service;

import br.com.aguiabranca.inovacao.domain.AcaoHistorico;
import br.com.aguiabranca.inovacao.domain.HistoricoOrientacao;
import br.com.aguiabranca.inovacao.domain.Orientacao;
import br.com.aguiabranca.inovacao.dto.orientacao.HistoricoResponse;
import br.com.aguiabranca.inovacao.dto.orientacao.OrientacaoRequest;
import br.com.aguiabranca.inovacao.dto.orientacao.OrientacaoResponse;
import br.com.aguiabranca.inovacao.exception.RecursoNaoEncontradoException;
import br.com.aguiabranca.inovacao.repository.OrientacaoRepository;
import br.com.aguiabranca.inovacao.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrientacaoService {

    private final OrientacaoRepository repository;

    /** Lista as orientações ativas; os filtros são opcionais e o volume é pequeno (diretrizes da empresa). */
    public List<OrientacaoResponse> listar(Boolean vigente, String area) {
        return repository.findByAtivoTrueOrderByCriadoEmDesc().stream()
                .filter(o -> vigente == null || o.isVigente() == vigente)
                .filter(o -> area == null || area.isBlank() || area.equalsIgnoreCase(o.getArea()))
                .map(OrientacaoResponse::from)
                .toList();
    }

    public OrientacaoResponse buscar(String id) {
        return OrientacaoResponse.from(buscarAtiva(id));
    }

    public List<HistoricoResponse> historico(String id) {
        return buscarAtiva(id).getHistorico().stream()
                .sorted(Comparator.comparing(HistoricoOrientacao::data).reversed())
                .map(HistoricoResponse::from)
                .toList();
    }

    public OrientacaoResponse criar(OrientacaoRequest request, UsuarioAutenticado autor) {
        Instant agora = Instant.now();
        Orientacao orientacao = Orientacao.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .categoria(request.categoria())
                .campanha(request.campanha())
                .area(request.area())
                .periodo(request.periodo())
                .indicadores(request.indicadoresOuVazio())
                .vigente(request.vigenteOuFalso())
                .criadoPorId(autor.id())
                .criadoEm(agora)
                .atualizadoEm(agora)
                .historico(new ArrayList<>())
                .build();
        registrar(orientacao, AcaoHistorico.CRIACAO, autor, agora);
        return OrientacaoResponse.from(repository.save(orientacao));
    }

    public OrientacaoResponse atualizar(String id, OrientacaoRequest request, UsuarioAutenticado autor) {
        Orientacao orientacao = buscarAtiva(id);
        orientacao.setTitulo(request.titulo());
        orientacao.setDescricao(request.descricao());
        orientacao.setCategoria(request.categoria());
        orientacao.setCampanha(request.campanha());
        orientacao.setArea(request.area());
        orientacao.setPeriodo(request.periodo());
        orientacao.setIndicadores(request.indicadoresOuVazio());
        orientacao.setVigente(request.vigenteOuFalso());

        Instant agora = Instant.now();
        orientacao.setAtualizadoEm(agora);
        registrar(orientacao, AcaoHistorico.ATUALIZACAO, autor, agora);
        return OrientacaoResponse.from(repository.save(orientacao));
    }

    /** Exclusão lógica: preserva o histórico e os vínculos de ideias e projetos já criados. */
    public void excluir(String id, UsuarioAutenticado autor) {
        Orientacao orientacao = buscarAtiva(id);
        Instant agora = Instant.now();
        orientacao.setAtivo(false);
        orientacao.setVigente(false);
        orientacao.setAtualizadoEm(agora);
        registrar(orientacao, AcaoHistorico.EXCLUSAO, autor, agora);
        repository.save(orientacao);
    }

    public Orientacao buscarAtiva(String id) {
        return repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orientação não encontrada"));
    }

    private void registrar(Orientacao orientacao, AcaoHistorico acao, UsuarioAutenticado autor, Instant data) {
        List<HistoricoOrientacao> historico = new ArrayList<>(orientacao.getHistorico());
        historico.add(new HistoricoOrientacao(data, acao, orientacao.getTitulo(), orientacao.getCategoria(),
                orientacao.getCampanha(), autor.id(), autor.nome()));
        orientacao.setHistorico(historico);
    }
}
