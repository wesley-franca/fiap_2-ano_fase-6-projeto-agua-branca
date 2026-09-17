# Roteiro da apresentação — Sprint 2

Conteúdo pronto para virar slides (PDF ou PPT). O que está marcado com **[preencher]** depende de você.

---

## Slide 1 — Capa

**Plataforma de Inovação · Grupo Águia Branca**
Challenge FIAP — 2º ano, Fase 6 · Sprint 2 · Grupo 42

**[preencher]** Nome completo e RM de cada integrante.

---

## Slide 2 — O problema

A Sprint 1 entregou um protótipo com dados fixos em memória: as telas funcionavam, mas nada era guardado,
não havia usuários reais e cada aparelho vivia isolado.

Na Sprint 2 o protótipo virou plataforma: backend próprio, autenticação por perfil, dados persistidos e o
app consumindo tudo por APIs REST.

---

## Slide 3 — Como funciona

Incluir o diagrama de arquitetura de [ARQUITETURA.md](ARQUITETURA.md) (primeiro diagrama).

- **App Android:** Kotlin, Jetpack Compose, MVVM, Retrofit.
- **API:** Java 21, Spring Boot 4, Spring Security com JWT, camadas `controller → service → repository`.
- **Banco:** MongoDB, com valores financeiros em decimal e e-mail único por usuário.
- **Execução:** dois containers que sobem com um comando (`docker compose up`).

---

## Slide 4 — Segurança e perfis

Incluir o diagrama de autenticação de [ARQUITETURA.md](ARQUITETURA.md).

- Senhas guardadas com BCrypt; token JWT assinado com validade de 8 horas.
- O perfil viaja dentro do token e cada rota declara quem pode acessá-la.
- Quem não tem permissão recebe 403 com mensagem clara, e o app trata isso na tela.

| Recurso | Operador | Gestor | Liderança |
|---|---|---|---|
| Orientações | consulta | consulta | cria, edita, exclui, vê histórico |
| Ideias | cria e gerencia as próprias | prioriza, avança etapa, rejeita | consulta |
| Projetos | — | cria, edita, acompanha, registra resultados | consulta |
| Dashboards | — | painel tático | visão executiva completa |

---

## Slide 5 — O que cada perfil vê no app

**[preencher]** Prints do app: home do operador, fila do gestor e dashboard da liderança.

- **Operador:** orientação vigente, suas ideias, cadastro com vínculo à estratégia.
- **Gestor:** KPIs, fila com filtros, análise da ideia, projetos com progresso e resultados.
- **Liderança:** 6 indicadores, três gráficos, orientações com histórico e retorno por projeto.

---

## Slide 6 — Da ideia ao resultado

Incluir o diagrama de ciclo de vida da ideia de [ARQUITETURA.md](ARQUITETURA.md).

O operador registra o problema que vê na operação. O gestor prioriza e conduz a ideia pelas etapas.
Ao ser aprovada, ela vira projeto, e o que o projeto devolve em dinheiro e produtividade aparece
no dashboard da liderança. É o caminho completo: chão de fábrica → estratégia → resultado medido.

---

## Slide 7 — Endpoints

27 endpoints, documentados em [ENDPOINTS.md](ENDPOINTS.md) e testáveis no Swagger.

Exemplo para mostrar ao vivo:

```
POST /api/auth/login          → { "token": "...", "usuario": { "role": "GESTOR" } }
GET  /api/dashboard/resumo    → indicadores + séries dos gráficos   (só LIDERANCA)
PATCH /api/ideias/{id}/status → avança ou rejeita a ideia           (só GESTOR)
```

Erros seguem um formato único, com `status`, `message` e `path`.

---

## Slide 8 — Qualidade

- **69 testes de integração** com MongoDB em container, cobrindo permissões, regras de dono,
  transições de status e o cálculo dos indicadores.
- **CI no GitHub Actions**: a cada push roda os testes do backend e compila o APK.
- Camadas separadas, DTOs distintos dos documentos, validação de entrada e erros padronizados.

---

## Slide 9 — Os números da demonstração

O banco já sobe populado, reproduzindo os indicadores do protótipo da Sprint 1:

| Indicador | Valor |
|---|---|
| ROI consolidado | 2,4x |
| Lucro | R$ 1,92 milhão |
| Projetos no prazo | 9 de 11 |
| Custo evitado | R$ 420 mil |
| Ganho de produtividade | +9,4% |

Nada disso está fixo no código: tudo é calculado pela API a partir dos projetos cadastrados.

---

## Slide 10 — Como rodar

```bash
cd backend && docker compose up --build     # sobe API + MongoDB
# app/ no Android Studio → Run
```

Usuários de teste (senha `senha123`): `operador@`, `gestor@` e `lideranca@aguiabranca.com`.

Passo a passo completo, roteiro de avaliação e solução de problemas no README do repositório.

---

## Slide 11 — Próximos passos

- Diferencial de IA: pontuar e priorizar ideias automaticamente, e gerar análises do dashboard.
- Seletor de data nos formulários e notificações para o operador quando a ideia muda de etapa.
- Publicação da API para acesso fora da rede local.

---

## Checklist antes de gravar/apresentar

- [ ] Nomes e RMs preenchidos
- [ ] Prints atualizados das telas
- [ ] API rodando para a demonstração ao vivo
- [ ] Ensaio do fluxo: operador cria ideia → gestor aprova → vira projeto → liderança vê o retorno
