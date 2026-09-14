# Planejamento — Challenge Grupo Águia Branca · Sprint 2

> Fonte: https://on.fiap.com.br/mod/assign/view.php?id=658207
> Período: 09/09/2026 → **21/09/2026 23h59** (sem prazo extra de atraso) · Grupo 42

## 1. Objetivo

Substituir os mocks do app da Sprint 1 (semestre anterior) por um **backend real**: APIs REST
completas, autenticação com controle de acesso por perfil, integração com o app nativo,
consumo de serviços externos e (plus) integração com IA.

## 2. Requisitos obrigatórios

| Tema | Requisito |
|---|---|
| Backend | **Java** (Spring Boot, Spring Security, JPA/Hibernate) **ou** **C#** (.NET 8 Web API, Identity/JWT, EF Core) |
| Banco | MongoDB (ou outro NoSQL) |
| Integração | App da Sprint 1 consumindo as APIs reais, sem mocks |
| Plus | IA no filtro/análise de iniciativas ou resultados (APIs gratuitas: Gemini, OpenRouter, GitHub Models…) |

## 3. Perfis e permissões

| Recurso | Operador | Gestor | Líder |
|---|---|---|---|
| Orientações estratégicas | consulta | consulta | **CRUD** |
| Ideias de inovação | **CRUD das próprias** | consulta, prioriza, aprova | consulta |
| Projetos / iniciativas | — | **CRUD** + progresso/resultados | consulta andamento |
| Dashboard / relatórios | — | — | **consulta** |

## 4. Modelo de domínio (rascunho)

- **Usuario** — id, nome, email, senhaHash (BCrypt), perfil (`OPERADOR | GESTOR | LIDER`)
- **Estrategia** — id, titulo, descricao, categoria, campanha, dataCriacao, vigente, **historico[]** (versões: data, categoria, campanha, autor)
- **Ideia** — id, titulo, descricao, problema, autorId, estrategiaId, status (`PENDENTE | PRIORIZADA | APROVADA | REJEITADA`), prioridade, scoreIA, justificativaIA, datas
- **Projeto** — id, nome, descricao, estrategiaId, ideiaOrigemId, gestorId, etapa, status, investimento, prazo, dataInicio/Fim, retornoFinanceiro, lucro, aumentoProdutividade, atualizacoes[]

## 5. Endpoints previstos (v1)

```
POST   /api/auth/login                     público → JWT
POST   /api/auth/register                  (seed ou LIDER)
GET    /api/auth/me

GET    /api/estrategias                    todos
GET    /api/estrategias/{id}               todos
GET    /api/estrategias/{id}/historico     todos
POST   /api/estrategias                    LIDER
PUT    /api/estrategias/{id}               LIDER
DELETE /api/estrategias/{id}               LIDER

GET    /api/ideias                         OPERADOR (próprias) · GESTOR/LIDER (todas, filtros)
POST   /api/ideias                         OPERADOR
PUT    /api/ideias/{id}                    OPERADOR (autor)
DELETE /api/ideias/{id}                    OPERADOR (autor)
PATCH  /api/ideias/{id}/prioridade         GESTOR
PATCH  /api/ideias/{id}/status             GESTOR (aprovar/rejeitar)

GET    /api/projetos                       GESTOR · LIDER
GET    /api/projetos/{id}                  GESTOR · LIDER
POST   /api/projetos                       GESTOR
PUT    /api/projetos/{id}                  GESTOR
PATCH  /api/projetos/{id}/progresso        GESTOR
DELETE /api/projetos/{id}                  GESTOR

GET    /api/dashboard/resumo               LIDER  (ROI, lucro, investimento, prazo, produtividade)
GET    /api/dashboard/estrategias/{id}     LIDER
GET    /api/dashboard/projetos/{id}        LIDER

POST   /api/ia/ideias/{id}/avaliar         GESTOR   (score + justificativa)
POST   /api/ia/chat                        GESTOR   (assistente)
GET    /api/ia/dashboard/insights          LIDER
```

## 6. Arquitetura proposta

- **Stack (a confirmar):** Java 21 + Spring Boot 3, Spring Security + JWT, Spring Data MongoDB,
  Bean Validation, springdoc-openapi (Swagger), Docker Compose (API + MongoDB).
  - Obs.: o enunciado cita JPA/Hibernate, mas com MongoDB o equivalente é Spring Data MongoDB — justificar na apresentação.
- **Camadas:** `controller → service → repository`, `domain` (entidades), `dto` + mappers,
  `security` (filtro JWT, config de roles), `integration/ai` (cliente IA), `exception` (handler global).
- **IA:** interface `AiProvider` com implementação Gemini (ou OpenRouter) — chave via variável de ambiente.
- **Seed:** 1 usuário de cada perfil + dados de exemplo para demo e dashboard.

## 7. Roadmap (até 21/09)

| # | Etapa | Entrega |
|---|---|---|
| 0 | Receber e analisar app v1 (Sprint 1): telas, mocks, contratos esperados | mapa tela → endpoint |
| 1 | Setup: projeto, Docker Compose, Mongo, Swagger, CI básico | API sobe vazia |
| 2 | Autenticação JWT + roles + seed | login dos 3 perfis |
| 3 | Estratégias (CRUD + histórico) | |
| 4 | Ideias (CRUD, priorizar, aprovar, vínculo estratégia) | |
| 5 | Projetos (CRUD, progresso, resultados) | |
| 6 | Dashboard / relatórios agregados | |
| 7 | IA (score de ideias → insights → chat, conforme tempo) | plus |
| 8 | Integrar app: trocar mocks por chamadas reais, gerar APK/IPA | |
| 9 | Testes, README, Postman, diagrama, slides, zips | entrega |

## 8. Entregáveis

- [ ] `.zip` do backend (camadas claras + README com instruções de execução)
- [ ] `.zip` do app integrado + **APK** (ou IPA)
- [ ] Apresentação PDF/PPT: nomes e RMs, diagrama de arquitetura, especificação dos endpoints (rota, método, payload, resposta), explicação da IA

## 9. Critérios de avaliação

Implementação técnica 50% · Integração app↔backend 15% · Apresentação/documentação 15% ·
Qualidade de código 10% · Criatividade/inovação 10%

## 10. Pendências / decisões

- [ ] Confirmar stack: Java/Spring (recomendado) ou .NET 8
- [ ] Trazer o app v1 (há `app_de_inovação.zip` e `app_de_Inovação2.zip` em `../fiap-ano-2/` — confirmar se é esse)
- [ ] Definir provedor de IA e obter chave gratuita
- [ ] Monorepo (`backend/` + `app/`) ou repositórios separados
- [ ] Confirmar integrantes/RMs do Grupo 42
