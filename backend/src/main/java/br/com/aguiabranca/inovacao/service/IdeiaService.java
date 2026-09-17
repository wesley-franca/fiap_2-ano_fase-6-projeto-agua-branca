package br.com.aguiabranca.inovacao.service;

import br.com.aguiabranca.inovacao.domain.Ideia;
import br.com.aguiabranca.inovacao.domain.PrioridadeIdeia;
import br.com.aguiabranca.inovacao.domain.Role;
import br.com.aguiabranca.inovacao.domain.StatusIdeia;
import br.com.aguiabranca.inovacao.domain.Usuario;
import br.com.aguiabranca.inovacao.dto.ideia.IdeiaRequest;
import br.com.aguiabranca.inovacao.dto.ideia.IdeiaResponse;
import br.com.aguiabranca.inovacao.dto.ideia.PrioridadeRequest;
import br.com.aguiabranca.inovacao.dto.ideia.StatusIdeiaRequest;
import br.com.aguiabranca.inovacao.exception.OperacaoInvalidaException;
import br.com.aguiabranca.inovacao.exception.RecursoNaoEncontradoException;
import br.com.aguiabranca.inovacao.repository.IdeiaRepository;
import br.com.aguiabranca.inovacao.repository.UsuarioRepository;
import br.com.aguiabranca.inovacao.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IdeiaService {

    /** Fluxo permitido da ideia; PROJETO e REJEITADA são estados finais. */
    private static final Map<StatusIdeia, Set<StatusIdeia>> TRANSICOES = Map.of(
            StatusIdeia.ENVIADA, Set.of(StatusIdeia.TRIAGEM, StatusIdeia.ANALISE, StatusIdeia.REJEITADA),
            StatusIdeia.TRIAGEM, Set.of(StatusIdeia.ANALISE, StatusIdeia.REJEITADA),
            StatusIdeia.ANALISE, Set.of(StatusIdeia.DECISAO, StatusIdeia.REJEITADA),
            StatusIdeia.DECISAO, Set.of(StatusIdeia.PROJETO, StatusIdeia.REJEITADA),
            StatusIdeia.PROJETO, Set.of(),
            StatusIdeia.REJEITADA, Set.of());

    private final IdeiaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final OrientacaoService orientacaoService;

    /** Operador vê apenas as próprias ideias; gestor e liderança veem todas. Filtros são opcionais. */
    public List<IdeiaResponse> listar(UsuarioAutenticado usuario, StatusIdeia status, PrioridadeIdeia prioridade,
                                      String area, String orientacaoId) {
        List<Ideia> ideias = usuario.role() == Role.OPERADOR
                ? repository.findByOperadorIdOrderByCriadoEmDesc(usuario.id())
                : repository.findAllByOrderByCriadoEmDesc();

        return ideias.stream()
                .filter(i -> status == null || i.getStatus() == status)
                .filter(i -> prioridade == null || i.getPrioridade() == prioridade)
                .filter(i -> area == null || area.isBlank() || area.equalsIgnoreCase(i.getArea()))
                .filter(i -> orientacaoId == null || orientacaoId.isBlank() || orientacaoId.equals(i.getOrientacaoId()))
                .map(IdeiaResponse::from)
                .toList();
    }

    public IdeiaResponse buscar(String id, UsuarioAutenticado usuario) {
        return IdeiaResponse.from(buscarVisivel(id, usuario));
    }

    public IdeiaResponse criar(IdeiaRequest request, UsuarioAutenticado autor) {
        Usuario operador = usuarioRepository.findById(autor.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        orientacaoService.buscarAtiva(request.orientacaoId());

        Instant agora = Instant.now();
        Ideia ideia = Ideia.builder()
                .titulo(request.titulo())
                .categoria(request.categoria())
                .problemaObservado(request.problemaObservado())
                .suaProposta(request.suaProposta())
                .impacto(request.impactoOuMedio())
                .status(StatusIdeia.ENVIADA)
                .prioridade(PrioridadeIdeia.MEDIA)
                .operadorId(operador.getId())
                .nomeOperador(operador.getNome())
                .area(operador.getArea())
                .orientacaoId(request.orientacaoId())
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build();
        return IdeiaResponse.from(repository.save(ideia));
    }

    public IdeiaResponse atualizar(String id, IdeiaRequest request, UsuarioAutenticado autor) {
        Ideia ideia = buscar(id);
        exigirAutor(ideia, autor);
        exigirEditavel(ideia);
        orientacaoService.buscarAtiva(request.orientacaoId());

        ideia.setTitulo(request.titulo());
        ideia.setCategoria(request.categoria());
        ideia.setProblemaObservado(request.problemaObservado());
        ideia.setSuaProposta(request.suaProposta());
        ideia.setImpacto(request.impactoOuMedio());
        ideia.setOrientacaoId(request.orientacaoId());
        ideia.setAtualizadoEm(Instant.now());
        return IdeiaResponse.from(repository.save(ideia));
    }

    public void excluir(String id, UsuarioAutenticado autor) {
        Ideia ideia = buscar(id);
        exigirAutor(ideia, autor);
        exigirEditavel(ideia);
        repository.delete(ideia);
    }

    public IdeiaResponse priorizar(String id, PrioridadeRequest request, UsuarioAutenticado gestor) {
        Ideia ideia = buscar(id);
        if (ideia.getStatus() == StatusIdeia.REJEITADA || ideia.getStatus() == StatusIdeia.PROJETO) {
            throw new OperacaoInvalidaException("Ideia com status " + ideia.getStatus() + " não pode ser priorizada");
        }
        ideia.setPrioridade(request.prioridade());
        ideia.setAvaliadoPorId(gestor.id());
        ideia.setAtualizadoEm(Instant.now());
        return IdeiaResponse.from(repository.save(ideia));
    }

    public IdeiaResponse alterarStatus(String id, StatusIdeiaRequest request, UsuarioAutenticado gestor) {
        Ideia ideia = buscar(id);
        StatusIdeia atual = ideia.getStatus();
        StatusIdeia novo = request.status();

        if (!TRANSICOES.getOrDefault(atual, Set.of()).contains(novo)) {
            throw new OperacaoInvalidaException("Transição de status inválida: " + atual + " → " + novo);
        }
        if (novo == StatusIdeia.REJEITADA && (request.comentario() == null || request.comentario().isBlank())) {
            throw new OperacaoInvalidaException("Informe o motivo da rejeição no campo comentario");
        }

        ideia.setStatus(novo);
        ideia.setAvaliadoPorId(gestor.id());
        if (request.comentario() != null && !request.comentario().isBlank()) {
            ideia.setComentarioAvaliacao(request.comentario().strip());
        }
        ideia.setAtualizadoEm(Instant.now());
        return IdeiaResponse.from(repository.save(ideia));
    }

    public Ideia buscar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ideia não encontrada"));
    }

    private Ideia buscarVisivel(String id, UsuarioAutenticado usuario) {
        Ideia ideia = buscar(id);
        if (usuario.role() == Role.OPERADOR && !ideia.getOperadorId().equals(usuario.id())) {
            throw new AccessDeniedException("Ideia de outro operador");
        }
        return ideia;
    }

    private void exigirAutor(Ideia ideia, UsuarioAutenticado autor) {
        if (!ideia.getOperadorId().equals(autor.id())) {
            throw new AccessDeniedException("Apenas o autor pode alterar a ideia");
        }
    }

    private void exigirEditavel(Ideia ideia) {
        if (ideia.getStatus() != StatusIdeia.ENVIADA) {
            throw new OperacaoInvalidaException("A ideia já está em " + ideia.getStatus() + " e não pode mais ser alterada pelo operador");
        }
    }
}
