package br.com.aguiabranca.inovacao.config;

import br.com.aguiabranca.inovacao.TestcontainersConfiguration;
import br.com.aguiabranca.inovacao.domain.Projeto;
import br.com.aguiabranca.inovacao.domain.StatusProjeto;
import br.com.aguiabranca.inovacao.domain.Usuario;
import br.com.aguiabranca.inovacao.repository.IdeiaRepository;
import br.com.aguiabranca.inovacao.repository.OrientacaoRepository;
import br.com.aguiabranca.inovacao.repository.ProjetoRepository;
import br.com.aguiabranca.inovacao.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DataSeederTests {

    @Autowired
    private DataSeeder dataSeeder;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private OrientacaoRepository orientacaoRepository;
    @Autowired
    private IdeiaRepository ideiaRepository;
    @Autowired
    private ProjetoRepository projetoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void seedEIdempotente() {
        long usuarios = usuarioRepository.count();
        long orientacoes = orientacaoRepository.count();
        long ideias = ideiaRepository.count();
        long projetos = projetoRepository.count();

        dataSeeder.run(null);

        assertThat(usuarioRepository.count()).isEqualTo(usuarios).isEqualTo(5);
        assertThat(orientacaoRepository.count()).isEqualTo(orientacoes).isEqualTo(3);
        assertThat(ideiaRepository.count()).isEqualTo(ideias).isEqualTo(5);
        assertThat(projetoRepository.count()).isEqualTo(projetos).isEqualTo(11);
    }

    @Test
    void senhasSaoArmazenadasComBcrypt() {
        Usuario operador = usuarioRepository.findByEmail(DataSeeder.OPERADOR).orElseThrow();

        assertThat(operador.getSenhaHash()).isNotEqualTo(DataSeeder.SENHA_PADRAO).startsWith("$2");
        assertThat(passwordEncoder.matches(DataSeeder.SENHA_PADRAO, operador.getSenhaHash())).isTrue();
    }

    @Test
    void emailDeUsuarioEUnico() {
        Usuario duplicado = Usuario.builder()
                .nome("Duplicado")
                .email(DataSeeder.GESTOR)
                .senhaHash("x")
                .criadoEm(Instant.now())
                .build();

        assertThatThrownBy(() -> usuarioRepository.save(duplicado)).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void ideiasEProjetosEstaoVinculadosAOrientacoes() {
        assertThat(ideiaRepository.findAll()).allSatisfy(ideia -> assertThat(ideia.getOrientacaoId()).isNotNull());
        assertThat(projetoRepository.findAll()).allSatisfy(projeto -> {
            assertThat(projeto.getOrientacaoId()).isNotNull();
            assertThat(projeto.getResponsavelId()).isNotNull();
        });
    }

    @Test
    void projetosReproduzemKpisDoDashboardDaV1() {
        List<Projeto> projetos = projetoRepository.findAll();

        BigDecimal investimento = soma(projetos, Projeto::getInvestimento);
        BigDecimal retorno = soma(projetos, Projeto::getRetornoFinanceiro);
        BigDecimal lucro = retorno.subtract(investimento);
        BigDecimal roi = lucro.divide(investimento, 1, RoundingMode.HALF_UP);
        BigDecimal produtividadeMedia = soma(projetos, Projeto::getAumentoProdutividade)
                .divide(BigDecimal.valueOf(projetos.size()), 1, RoundingMode.HALF_UP);

        assertThat(roi).isEqualByComparingTo("2.4");
        assertThat(lucro).isEqualByComparingTo("1920000");
        assertThat(soma(projetos, Projeto::getCustoEvitado)).isEqualByComparingTo("420000");
        assertThat(produtividadeMedia).isEqualByComparingTo("9.4");
        assertThat(projetos).filteredOn(p -> p.getStatus() == StatusProjeto.NO_PRAZO).hasSize(9);
    }

    private static BigDecimal soma(List<Projeto> projetos, java.util.function.Function<Projeto, BigDecimal> campo) {
        return projetos.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
