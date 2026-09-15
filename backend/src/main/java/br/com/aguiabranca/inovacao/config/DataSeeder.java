package br.com.aguiabranca.inovacao.config;

import br.com.aguiabranca.inovacao.domain.AcaoHistorico;
import br.com.aguiabranca.inovacao.domain.HistoricoOrientacao;
import br.com.aguiabranca.inovacao.domain.Ideia;
import br.com.aguiabranca.inovacao.domain.Impacto;
import br.com.aguiabranca.inovacao.domain.Orientacao;
import br.com.aguiabranca.inovacao.domain.PrioridadeIdeia;
import br.com.aguiabranca.inovacao.domain.Projeto;
import br.com.aguiabranca.inovacao.domain.Role;
import br.com.aguiabranca.inovacao.domain.StatusIdeia;
import br.com.aguiabranca.inovacao.domain.StatusProjeto;
import br.com.aguiabranca.inovacao.domain.Usuario;
import br.com.aguiabranca.inovacao.repository.IdeiaRepository;
import br.com.aguiabranca.inovacao.repository.OrientacaoRepository;
import br.com.aguiabranca.inovacao.repository.ProjetoRepository;
import br.com.aguiabranca.inovacao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Popula o banco com os dados de demonstração da Sprint 1 (usuários de teste, orientações, ideias e projetos).
 * Idempotente: usuários são criados apenas se o e-mail não existir; demais coleções apenas se estiverem vazias.
 * Os projetos reproduzem os KPIs exibidos no dashboard da v1 (ROI 2,4x, lucro R$ 1,92M, 9/11 no prazo,
 * custo evitado R$ 420k, produtividade +9,4%).
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@ConditionalOnBooleanProperty(name = "app.seed.enabled", matchIfMissing = true)
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    static final String SENHA_PADRAO = "senha123";

    static final String OPERADOR = "operador@aguiabranca.com";
    static final String OPERADORA_ANA = "ana.lima@aguiabranca.com";
    static final String OPERADOR_ROBERTO = "roberto.mendes@aguiabranca.com";
    static final String GESTOR = "gestor@aguiabranca.com";
    static final String LIDERANCA = "lideranca@aguiabranca.com";

    private static final String PARADAS = "Reduzir paradas em 15%";
    private static final String OEE = "Aumentar OEE para 82%";
    private static final String ACIDENTES = "Zero acidentes";
    private static final String IDEIA_EMBALAGENS = "Reaproveitar embalagens da L2";

    private final UsuarioRepository usuarioRepository;
    private final OrientacaoRepository orientacaoRepository;
    private final IdeiaRepository ideiaRepository;
    private final ProjetoRepository projetoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Map<String, Usuario> usuarios = seedUsuarios();
        Map<String, Orientacao> orientacoes = seedOrientacoes(usuarios.get(LIDERANCA));
        Map<String, Ideia> ideias = seedIdeias(usuarios, orientacoes);
        seedProjetos(usuarios.get(GESTOR), orientacoes, ideias);
    }

    private Map<String, Usuario> seedUsuarios() {
        List<Usuario> usuarios = List.of(
                garantirUsuario(OPERADOR, "João Costa", Role.OPERADOR, "Manutenção"),
                garantirUsuario(OPERADORA_ANA, "Ana Lima", Role.OPERADOR, "Operação"),
                garantirUsuario(OPERADOR_ROBERTO, "Roberto Mendes", Role.OPERADOR, "Sustentabilidade"),
                garantirUsuario(GESTOR, "Maria Silva", Role.GESTOR, "Manutenção"),
                garantirUsuario(LIDERANCA, "Paulo Andrade", Role.LIDERANCA, "Geral"));
        return usuarios.stream().collect(Collectors.toMap(Usuario::getEmail, Function.identity()));
    }

    private Usuario garantirUsuario(String email, String nome, Role role, String area) {
        return usuarioRepository.findByEmail(email).orElseGet(() -> {
            log.info("Seed: criando usuário {} ({})", email, role);
            return usuarioRepository.save(Usuario.builder()
                    .nome(nome)
                    .email(email)
                    .senhaHash(passwordEncoder.encode(SENHA_PADRAO))
                    .role(role)
                    .area(area)
                    .criadoEm(Instant.now())
                    .build());
        });
    }

    private Map<String, Orientacao> seedOrientacoes(Usuario lider) {
        if (orientacaoRepository.count() == 0) {
            Instant criadoEm = diasAtras(60);
            orientacaoRepository.saveAll(List.of(
                    orientacao(PARADAS, "Foco do semestre: reduzir paradas não planejadas em 15%",
                            "Eficiência operacional", "Paradas Zero", "Manutenção", "2º semestre 2026",
                            List.of("OEE: 78%", "MTBF: 42h", "Paradas: -15%"), lider, criadoEm),
                    orientacao(OEE, "Meta anual: melhorar a eficiência global dos equipamentos (OEE)",
                            "Produtividade", "OEE 82", "Operação", "Anual 2026",
                            List.of("OEE atual: 78%", "Meta: 82%", "Investimento: R$ 200k"), lider, criadoEm),
                    orientacao(ACIDENTES, "Objetivo contínuo de segurança",
                            "Segurança", "Segurança em Primeiro Lugar", "EHS", "Contínuo",
                            List.of("Acidentes este ano: 0", "Treinamentos: 100%", "NR10 atualizado: Sim"), lider, criadoEm)));
            log.info("Seed: 3 orientações estratégicas criadas");
        }
        return orientacaoRepository.findAll().stream()
                .collect(Collectors.toMap(Orientacao::getTitulo, Function.identity(), (a, b) -> a));
    }

    private Orientacao orientacao(String titulo, String descricao, String categoria, String campanha, String area,
                                  String periodo, List<String> indicadores, Usuario lider, Instant criadoEm) {
        return Orientacao.builder()
                .titulo(titulo)
                .descricao(descricao)
                .categoria(categoria)
                .campanha(campanha)
                .area(area)
                .periodo(periodo)
                .indicadores(indicadores)
                .vigente(true)
                .criadoPorId(lider.getId())
                .criadoEm(criadoEm)
                .atualizadoEm(criadoEm)
                .historico(List.of(new HistoricoOrientacao(criadoEm, AcaoHistorico.CRIACAO, titulo, categoria, campanha,
                        lider.getId(), lider.getNome())))
                .build();
    }

    private Map<String, Ideia> seedIdeias(Map<String, Usuario> usuarios, Map<String, Orientacao> orientacoes) {
        if (ideiaRepository.count() == 0) {
            ideiaRepository.saveAll(List.of(
                    ideia("Sensor de vibração na linha 3", "Manutenção",
                            "Máquina apresenta vibrações anormais", "Instalar sensor para monitoramento em tempo real",
                            Impacto.ALTO, StatusIdeia.ANALISE, PrioridadeIdeia.ALTA,
                            usuarios.get(OPERADOR), orientacoes.get(PARADAS), 3),
                    ideia("Checklist digital de turno", "Operação",
                            "Checklist em papel gera atrasos", "App mobile para checklist com sincronização",
                            Impacto.MEDIO, StatusIdeia.ENVIADA, PrioridadeIdeia.MEDIA,
                            usuarios.get(OPERADORA_ANA), orientacoes.get(OEE), 5),
                    ideia(IDEIA_EMBALAGENS, "Sustentabilidade",
                            "Muita embalagem desperdiçada", "Programa de retorno de embalagens",
                            Impacto.MEDIO, StatusIdeia.PROJETO, PrioridadeIdeia.MEDIA,
                            usuarios.get(OPERADOR_ROBERTO), orientacoes.get(OEE), 14),
                    ideia("Guarda-corpo na plataforma da prensa 2", "Segurança",
                            "Plataforma sem proteção lateral durante a manutenção",
                            "Instalar guarda-corpo removível na plataforma",
                            Impacto.ALTO, StatusIdeia.ENVIADA, PrioridadeIdeia.ALTA,
                            usuarios.get(OPERADOR), orientacoes.get(ACIDENTES), 1),
                    rejeitada(ideia("Kit de troca rápida de ferramentas", "Manutenção",
                            "Troca de ferramentas demora mais de 40 minutos", "Montar kit dedicado por máquina",
                            Impacto.BAIXO, StatusIdeia.REJEITADA, PrioridadeIdeia.BAIXA,
                            usuarios.get(OPERADOR), orientacoes.get(PARADAS), 20),
                            usuarios.get(GESTOR), "Solução já coberta pelo projeto Padronização de setup")));
            log.info("Seed: 5 ideias criadas");
        }
        return ideiaRepository.findAll().stream()
                .collect(Collectors.toMap(Ideia::getTitulo, Function.identity(), (a, b) -> a));
    }

    private Ideia ideia(String titulo, String categoria, String problema, String proposta, Impacto impacto,
                        StatusIdeia status, PrioridadeIdeia prioridade, Usuario operador, Orientacao orientacao,
                        int diasAtras) {
        Instant criadoEm = diasAtras(diasAtras);
        return Ideia.builder()
                .titulo(titulo)
                .categoria(categoria)
                .problemaObservado(problema)
                .suaProposta(proposta)
                .impacto(impacto)
                .status(status)
                .prioridade(prioridade)
                .operadorId(operador.getId())
                .nomeOperador(operador.getNome())
                .area(operador.getArea())
                .orientacaoId(idOuNulo(orientacao))
                .criadoEm(criadoEm)
                .atualizadoEm(criadoEm)
                .build();
    }

    private Ideia rejeitada(Ideia ideia, Usuario gestor, String comentario) {
        ideia.setAvaliadoPorId(gestor.getId());
        ideia.setComentarioAvaliacao(comentario);
        return ideia;
    }

    private void seedProjetos(Usuario gestor, Map<String, Orientacao> orientacoes, Map<String, Ideia> ideias) {
        if (projetoRepository.count() > 0) {
            return;
        }
        Orientacao paradas = orientacoes.get(PARADAS);
        Orientacao oee = orientacoes.get(OEE);
        Orientacao acidentes = orientacoes.get(ACIDENTES);

        List<Projeto> projetos = List.of(
                projeto("Padronização de setup", oee, 2, 48, StatusProjeto.NO_PRAZO,
                        "2026-06-01", "2026-11-30", 84_000, 290_000, 60_000, "12.0"),
                projeto("Sensor de vibração L3", paradas, 1, 25, StatusProjeto.NO_PRAZO,
                        "2026-08-15", "2027-01-31", 110_000, 380_000, 90_000, "8.5"),
                projeto("Checklist digital de turno", oee, 3, 60, StatusProjeto.ATRASADO,
                        "2026-04-01", "2026-09-10", 22_000, 75_000, 15_000, "10.0"),
                projeto("Reaproveitamento de embalagens L2", oee, 2, 50, StatusProjeto.NO_PRAZO,
                        "2026-07-01", "2026-12-15", 35_000, 120_000, 40_000, "3.0"),
                projeto("Manutenção preditiva das prensas", paradas, 3, 70, StatusProjeto.NO_PRAZO,
                        "2026-03-01", "2026-10-31", 150_000, 520_000, 70_000, "11.0"),
                projeto("Treinamento NR10 digital", acidentes, 4, 90, StatusProjeto.NO_PRAZO,
                        "2026-02-01", "2026-09-30", 18_000, 60_000, 25_000, "4.0"),
                projeto("Automação da inspeção visual", oee, 2, 35, StatusProjeto.ATRASADO,
                        "2026-03-15", "2026-08-31", 140_000, 470_000, 30_000, "14.0"),
                projeto("EPIs com sensores inteligentes", acidentes, 1, 20, StatusProjeto.NO_PRAZO,
                        "2026-08-01", "2027-02-28", 60_000, 180_000, 20_000, "5.0"),
                projeto("Otimização de rotas internas", oee, 2, 55, StatusProjeto.NO_PRAZO,
                        "2026-06-15", "2026-12-20", 45_000, 150_000, 25_000, "9.0"),
                projeto("Lubrificação automática da L1", paradas, 3, 75, StatusProjeto.NO_PRAZO,
                        "2026-05-01", "2026-10-15", 70_000, 245_000, 30_000, "13.0"),
                projeto("Painel de OEE em tempo real", oee, 2, 45, StatusProjeto.NO_PRAZO,
                        "2026-07-01", "2026-12-01", 66_000, 230_000, 15_000, "13.9"));

        projetos.forEach(p -> {
            p.setResponsavelId(gestor.getId());
            p.setResponsavelNome(gestor.getNome());
        });
        Ideia embalagens = ideias.get(IDEIA_EMBALAGENS);
        projetos.get(3).setIdeiaOrigemId(embalagens == null ? null : embalagens.getId());

        projetoRepository.saveAll(projetos);
        log.info("Seed: {} projetos criados", projetos.size());
    }

    private Projeto projeto(String nome, Orientacao orientacao, int etapa, int progresso, StatusProjeto status,
                            String dataInicio, String prazo, long investimento, long retorno, long custoEvitado,
                            String produtividade) {
        Instant criadoEm = LocalDate.parse(dataInicio).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        return Projeto.builder()
                .nome(nome)
                .descricao("Iniciativa vinculada à orientação \"" + (orientacao == null ? "-" : orientacao.getTitulo()) + "\"")
                .orientacaoId(idOuNulo(orientacao))
                .etapa(etapa)
                .totalEtapas(4)
                .progresso(progresso)
                .status(status)
                .dataInicio(LocalDate.parse(dataInicio))
                .prazo(LocalDate.parse(prazo))
                .investimento(BigDecimal.valueOf(investimento))
                .retornoFinanceiro(BigDecimal.valueOf(retorno))
                .custoEvitado(BigDecimal.valueOf(custoEvitado))
                .aumentoProdutividade(new BigDecimal(produtividade))
                .criadoEm(criadoEm)
                .atualizadoEm(criadoEm)
                .build();
    }

    private static String idOuNulo(Orientacao orientacao) {
        return orientacao == null ? null : orientacao.getId();
    }

    private static Instant diasAtras(int dias) {
        return Instant.now().minus(Duration.ofDays(dias));
    }
}
