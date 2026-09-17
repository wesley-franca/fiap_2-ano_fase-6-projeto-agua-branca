package br.com.aguiabranca.inovacao.dto.dashboard;

/** KPIs do painel do gestor (ideias aguardando avaliação e projetos em andamento). */
public record PainelGestorResponse(
        long ideiasNovas,
        long ideiasEmAnalise,
        long ideiasAprovadas,
        long ideiasRejeitadas,
        long projetosAtivos,
        long projetosAtrasados
) {
}
