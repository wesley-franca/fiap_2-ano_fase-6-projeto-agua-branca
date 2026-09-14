# Planejamento — Challenge Grupo Águia Branca · Sprint 2

> Fonte: https://on.fiap.com.br/mod/assign/view.php?id=658207
> Período: 09/09/2026 → **21/09/2026 23h59** (sem prazo extra de atraso) · Grupo 42

## 1. Objetivo

Substituir os mocks do app da Sprint 1 (semestre anterior) por um **backend real**: APIs REST
completas, autenticação com controle de acesso por perfil e integração com o app nativo.

> **Fora do escopo por enquanto:** Diferencial IA (Plus).

## 2. Decisões tomadas

| Tema | Decisão |
|---|---|
| Backend | **Java 21 + Spring Boot 3**, Spring Security + JWT |
| Banco | **MongoDB** (Spring Data MongoDB) |
| App | App Android da Sprint 1 (Kotlin + Jetpack Compose) — base: `app_de_Inovação2.zip` |
| Repositório | **Monorepo**: `backend/` + `app/` |
| IA | adiada |

> O enunciado cita JPA/Hibernate, mas com MongoDB o equivalente é Spring Data MongoDB — justificar na apresentação.

## 3. Estrutura do monorepo

```
fiap_2-ano_fase-3/
├── backend/        API Spring Boot (Maven) + docker-compose (API + MongoDB)
├── app/            App Android (Kotlin/Compose) — MockRepository → Retrofit
├── docs/           diagrama de arquitetura, endpoints, Postman, slides
└── PLANEJAMENTO.md
```

## 4. Ponto de partida: app da Sprint 1

- Pacote `com.aguiabranca.inovacao`, MVVM + `MockRepository` (dados em memória), Navigation Compose.
- `app_de_Inovação2.zip` = mesma base do `app_de_inovação.zip` com dependências atualizadas (SDK 35, Compose BOM).
- Telas: Login · Operador (home + orientações, minhas ideias, nova ideia) · Gestor (painel KPIs, fila de ideias, aprovar/rejeitar, projetos) · Liderança (dashboard 6 KPIs, orientações, andamento de projetos).
- Modelos atuais (`Models.kt`): `User(role: OPERADOR|GESTOR|LIDERANCA)`, `Orientacao`, `Idea(status: ENVIADA|TRIAGEM|ANALISE|DECISAO|PROJETO|REJEITADA, prioridade: BAIXA|MEDIA|ALTA)`, `Projeto`, `DashboardMetricas`.
- Lacunas vs. enunciado: CRUD de orientações pelo líder, edição/exclusão de ideias pelo operador, CRUD de projetos pelo gestor, vínculo ideia/projeto ↔ estratégia, histórico de estratégias, valores monetários como `String`.
- Atenção: o zip contém uma pasta espúria `app/src/main/java/com/aguiabranca/inovacao/{data/` — limpar na importação.

## 5. Perfis e permissões

| Recurso | Operador | Gestor | Liderança |
|---|---|---|---|
| Orientações estratégicas | consulta | consulta | **CRUD** |
| Ideias de inovação | **CRUD das próprias** | consulta, prioriza, aprova | consulta |
| Projetos / iniciativas | — | **CRUD** + progresso/resultados | consulta andamento |
| Dashboard / relatórios | — | — | **consulta** |

## 6. Modelo de domínio (alinhado ao app)

Manter nomes/enums do app sempre que possível para reduzir retrabalho na integração.

- **Usuario** — id, nome, email, senhaHash (BCrypt), role (`OPERADOR | GESTOR | LIDERANCA`), area
- **Orientacao** (estratégia) — id, titulo, descricao, categoria, campanha, area, periodo, indicadores[], vigente, criadoEm, **historico[]** (data, categoria, campanha, alteradoPor)
- **Ideia** — id, titulo, categoria, problemaObservado, suaProposta, impacto, status (enum do app), prioridade (`BAIXA | MEDIA | ALTA`), operadorId, nomeOperador, orientacaoId, criadoEm, atualizadoEm
- **Projeto** — id, nome, descricao, responsavelId, ideiaOrigemId, orientacaoId, etapa, totalEtapas, progresso, status, prazo, investimento (BigDecimal), retornoFinanceiro, lucro, custoEvitado, aumentoProdutividade, atualizacoes[]
- **Dashboard** (calculado) — roi, lucro, projetosAtivos, noPrazo, custoEvitado, produtividade; por orientação e por projeto

## 7. Endpoints previstos (v1)

```
POST   /api/auth/login                      público → JWT
GET    /api/auth/me                         autenticado

GET    /api/orientacoes                     todos
GET    /api/orientacoes/{id}                todos
GET    /api/orientacoes/{id}/historico      todos
POST   /api/orientacoes                     LIDERANCA
PUT    /api/orientacoes/{id}                LIDERANCA
DELETE /api/orientacoes/{id}                LIDERANCA

GET    /api/ideias                          OPERADOR (próprias) · GESTOR/LIDERANCA (todas, filtros status/prioridade/orientação)
GET    /api/ideias/{id}                     autor · GESTOR · LIDERANCA
POST   /api/ideias                          OPERADOR
PUT    /api/ideias/{id}                     OPERADOR (autor, enquanto ENVIADA)
DELETE /api/ideias/{id}                     OPERADOR (autor, enquanto ENVIADA)
PATCH  /api/ideias/{id}/prioridade          GESTOR
PATCH  /api/ideias/{id}/status              GESTOR (aprovar/rejeitar/avançar)

GET    /api/projetos                        GESTOR · LIDERANCA
GET    /api/projetos/{id}                   GESTOR · LIDERANCA
POST   /api/projetos                        GESTOR
PUT    /api/projetos/{id}                   GESTOR
PATCH  /api/projetos/{id}/progresso         GESTOR (etapa, status, resultados)
DELETE /api/projetos/{id}                   GESTOR

GET    /api/dashboard/resumo                LIDERANCA
GET    /api/dashboard/orientacoes/{id}      LIDERANCA
GET    /api/dashboard/projetos/{id}         LIDERANCA
GET    /api/dashboard/gestor                GESTOR (KPIs do painel do app)
```

## 8. Arquitetura do backend

- Java 21, Spring Boot 3, Maven, Spring Web, Security + JWT (jjwt), Data MongoDB, Validation,
  springdoc-openapi (Swagger), Lombok, JUnit 5 + Testcontainers/Mongo, Docker Compose.
- Pacotes: `controller → service → repository`, `domain`, `dto` + mappers, `security`
  (filtro JWT, `@PreAuthorize` por role), `config`, `exception` (handler global, erros padronizados).
- Seed: 3 usuários do app (`operador|gestor|lideranca@aguiabranca.com` / `senha123`) + dados de exemplo.

## 9. Integração do app

- Adicionar Retrofit + OkHttp + kotlinx/Gson; `ApiRepository` substituindo `MockRepository`.
- Interceptor com o JWT; armazenamento do token (DataStore); base URL por `BuildConfig` (`10.0.2.2:8080` no emulador).
- Novas telas/ações para cobrir as lacunas (CRUD orientações, editar/excluir ideia, CRUD projetos, vínculo com orientação).
- Gerar APK final.

## 10. Roadmap (até 21/09)

| # | Etapa | Resultado |
|---|---|---|
| 0 | Importar app para `app/` e mapear tela → endpoint | contrato fechado |
| 1 | Setup backend: Spring Boot, Docker Compose, Mongo, Swagger | API sobe |
| 2 | Autenticação JWT + roles + seed | login dos 3 perfis |
| 3 | Orientações (CRUD + histórico) | |
| 4 | Ideias (CRUD, priorizar, aprovar, vínculo) | |
| 5 | Projetos (CRUD, progresso, resultados) | |
| 6 | Dashboard / relatórios agregados | |
| 7 | Integrar app (Retrofit, JWT, novas telas) + APK | |
| 8 | Testes, README, Postman, diagrama, slides, zips | entrega |

## 11. Entregáveis

- [ ] `.zip` do backend (camadas claras + README com instruções de execução)
- [ ] `.zip` do app integrado + **APK**
- [ ] Apresentação PDF/PPT: nomes e RMs, diagrama de arquitetura, especificação dos endpoints (rota, método, payload, resposta)

## 12. Critérios de avaliação

Implementação técnica 50% · Integração app↔backend 15% · Apresentação/documentação 15% ·
Qualidade de código 10% · Criatividade/inovação 10%

## 13. Pendências

- [ ] Confirmar integrantes/RMs do Grupo 42
- [ ] Reavaliar Diferencial IA se sobrar tempo
