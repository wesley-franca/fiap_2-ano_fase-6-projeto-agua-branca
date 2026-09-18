# Planejamento — Challenge Grupo Águia Branca · Sprint 2

> Fonte: https://on.fiap.com.br/mod/assign/view.php?id=658207 (Fase 6 – DevOps)
> Período: 09/09/2026 → **21/09/2026 23h59** (sem prazo extra de atraso) · Grupo 42
> Diferencial IA (Plus): **fora do escopo por enquanto**.

---

## 1. Objetivo

Transformar o protótipo da Sprint 1 (app Android com `MockRepository`) em uma plataforma real:
**backend Java/Spring Boot + MongoDB**, autenticação JWT com 3 perfis, APIs para todos os recursos
e **app consumindo as APIs sem nenhum mock**.

## 2. Decisões

| Tema | Decisão |
|---|---|
| Backend | Java 21 · Spring Boot 4 · Spring Security + JWT · Spring Data MongoDB · Maven |
| Banco | MongoDB (local via Docker Compose) |
| App | App da Sprint 1 (Kotlin · Jetpack Compose · Material 3 · MVVM) |
| Repositório | Monorepo: `backend/` · `app/` · `docs/` |
| Entrega | **Execução local com Docker** — confirmado com o tutor: a equipe de correção roda tudo na máquina dela,
  desde que o README da raiz explique o passo a passo. Sem deploy em nuvem |
| IA | adiada |

> O enunciado cita JPA/Hibernate; com MongoDB o equivalente é **Spring Data MongoDB** — justificar na apresentação.

---

## 3. Diagnóstico da versão 1 (entregue na Sprint 1)

**Material recebido** (`App Águia Branca.zip`): código-fonte (`apkinov-master.zip`), `app-debug.apk` (v1.0.0),
PDF de apresentação e vídeo de demonstração.

**Stack:** Kotlin 1.9.10, AGP 8.1.2, compileSdk 35, Compose BOM 2024.10, Navigation Compose, StateFlow.
~1.900 linhas, 6 rotas: `login`, `operador_home`, `nova_ideia`, `gestor_painel`, `fila_ideias`, `lideranca_dashboard`.
`INTERNET` já declarada no manifest.

### 3.1 O que existe hoje

| Perfil | Tela | Comportamento v1 |
|---|---|---|
| Todos | Login | credenciais fixas no `MockRepository`; botão **"Continuar com SSO" sem ação** |
| Operador | Home | 1ª orientação + lista "Minhas ideias" (**mostra ideias de todos**) |
| Operador | Nova ideia | título, categoria (texto livre), problema, proposta; autor fixo `"Você"`, data `"agora"` |
| Gestor | Painel | KPIs (novas, em análise, projetos) + ideias para priorizar + "Meus projetos" |
| Gestor | Fila de ideias | aprovar (→ `PROJETO`) / rejeitar; chips **"Todas" e "Por área" sem ação** |
| Liderança | Dashboard | 6 KPIs **fixos** (ROI, Lucro YTD, ativos, no prazo, custo evitado, produtividade), orientações e andamento dos projetos |

### 3.2 Lacunas frente ao enunciado da Sprint 2

| # | Requisito | Situação v1 | O que fazer |
|---|---|---|---|
| L1 | Login real, JWT, criptografia | mock | backend JWT + BCrypt; app guarda token |
| L2 | Operação conforme role | só navegação | `@PreAuthorize` no backend + UI por perfil |
| L3 | Liderança: **CRUD** de orientações | só leitura | telas lista/form/editar/excluir |
| L4 | Histórico de estratégias (id, data, categoria, campanha) | não existe | modelo + endpoint + tela de histórico |
| L5 | Operador: **CRUD** das próprias ideias | só criar | detalhe, editar, excluir |
| L6 | Gestor: consultar, **priorizar** e aprovar | aprovar/rejeitar | ação de prioridade, detalhe, filtros |
| L7 | Ideia ↔ estratégia vigente | não existe | seleção de orientação no formulário |
| L8 | Gestor: **CRUD** de projetos + progresso + resultados | só leitura | telas lista/form/progresso/resultados |
| L9 | Projeto ↔ estratégia vigente | não existe | seleção de orientação no formulário |
| L10 | Liderança: andamento (etapa, status, investimento, prazo, **retorno**) | sem retorno | exibir retorno financeiro |
| L11 | Dashboard por **estratégia**, por **projeto** e geral, **com gráficos** | KPIs fixos, sem gráficos | endpoints de relatório + gráficos no app |
| L12 | Sem mocks | tudo mock | remover `MockRepository` |
| L13 | APK integrado | APK mock | novo APK com URL da API configurável |

### 3.3 Dívidas técnicas a corrigir no app

- Valores monetários/datas como `String` ("R$ 84k", "12 jun", "há 3 dias") → números/ISO no contrato, formatação no app.
- `MockRepository` é `object` singleton chamado direto nos ViewModels → interface `Repository` + implementação HTTP.
- Sem estados de erro/carregamento reais nas telas.
- Botão SSO sem função → remover (ou ocultar).
- README do app promete Firebase → atualizar.
- Zip interno `App_de_Inovação.zip` e pastas `{data`, `{values,drawable,mipmap}` são lixo → não importar.

---

## 4. Regras de negócio e permissões

| Recurso | OPERADOR | GESTOR | LIDERANCA |
|---|---|---|---|
| Orientações | listar/ver | listar/ver | **criar/editar/excluir** + histórico |
| Ideias | **CRUD das próprias** (editar/excluir só em `ENVIADA`) | listar todas, **priorizar**, mudar status (triagem→análise→decisão→aprovar/rejeitar) | listar/ver |
| Projetos | — | **CRUD**, atualizar etapa/progresso/status, registrar resultados | listar/ver andamento |
| Dashboard | — | KPIs do painel do gestor | **resumo geral, por orientação, por projeto** |

- Ideia aprovada pode gerar projeto (`ideiaOrigemId`), mudando status para `PROJETO`.
- Toda alteração de orientação grava um item no histórico (data, categoria, campanha, autor, ação).
- Somente uma orientação pode ser "vigente" por área/período (ou flag `vigente`) — usada como padrão nos vínculos.
- Soft delete em orientações para preservar histórico e vínculos.

## 5. Modelo de dados (MongoDB)

```
usuarios      { _id, nome, email (único), senhaHash, role, area, ativo, criadoEm }
orientacoes   { _id, titulo, descricao, categoria, campanha, area, periodo, indicadores[],
                vigente, ativo, criadoPor, criadoEm, atualizadoEm,
                historico[ { data, acao, categoria, campanha, alteradoPor, snapshot } ] }
ideias        { _id, titulo, categoria, problemaObservado, suaProposta, impacto,
                status, prioridade, operadorId, nomeOperador, area, orientacaoId,
                avaliadoPor, comentarioAvaliacao, criadoEm, atualizadoEm }
projetos      { _id, nome, descricao, responsavelId, responsavelNome, ideiaOrigemId, orientacaoId,
                etapa, totalEtapas, progresso, status (NO_PRAZO|ATRASADO|CONCLUIDO|CANCELADO),
                dataInicio, prazo, investimento, retornoFinanceiro, lucro, custoEvitado,
                aumentoProdutividade, atualizacoes[ { data, etapa, progresso, observacao, autor } ] }
```

**Métricas calculadas** (agregações Mongo): ROI = (retorno − investimento) / investimento; lucro total;
projetos ativos; % no prazo; custo evitado; produtividade média; ideias por status/área.

## 6. Contrato da API (v1)

Base `/api` · JSON · erros padronizados `{ timestamp, status, error, message, path, fields[] }` · paginação `?page&size`.

| Método | Rota | Perfil | Uso no app |
|---|---|---|---|
| POST | `/auth/login` | público | Login → `{ token, expiresIn, usuario }` |
| GET | `/auth/me` | autenticado | restaurar sessão |
| GET | `/orientacoes` `?vigente&area` | todos | Home operador, dashboard, selects |
| GET | `/orientacoes/{id}` | todos | detalhe |
| GET | `/orientacoes/{id}/historico` | todos | histórico |
| POST | `/orientacoes` | LIDERANCA | form |
| PUT | `/orientacoes/{id}` | LIDERANCA | form |
| DELETE | `/orientacoes/{id}` | LIDERANCA | excluir |
| GET | `/ideias` `?status&prioridade&area&orientacaoId` | OPERADOR (próprias) · GESTOR · LIDERANCA | minhas ideias, fila |
| GET | `/ideias/{id}` | autor · GESTOR · LIDERANCA | detalhe |
| POST | `/ideias` | OPERADOR | nova ideia |
| PUT | `/ideias/{id}` | OPERADOR autor | editar |
| DELETE | `/ideias/{id}` | OPERADOR autor | excluir |
| PATCH | `/ideias/{id}/prioridade` | GESTOR | priorizar |
| PATCH | `/ideias/{id}/status` | GESTOR | aprovar/rejeitar/avançar |
| GET | `/projetos` `?status&orientacaoId` | GESTOR · LIDERANCA | meus projetos, andamento |
| GET | `/projetos/{id}` | GESTOR · LIDERANCA | detalhe |
| POST | `/projetos` | GESTOR | novo projeto (opcional a partir de ideia) |
| PUT | `/projetos/{id}` | GESTOR | editar |
| PATCH | `/projetos/{id}/progresso` | GESTOR | etapa/progresso/status |
| PATCH | `/projetos/{id}/resultados` | GESTOR | retorno, lucro, custo evitado, produtividade |
| DELETE | `/projetos/{id}` | GESTOR | excluir |
| GET | `/dashboard/gestor` | GESTOR | KPIs do painel |
| GET | `/dashboard/resumo` | LIDERANCA | 6 KPIs + séries para gráficos |
| GET | `/dashboard/orientacoes` | LIDERANCA | retorno por orientação (gráfico barras) |
| GET | `/dashboard/orientacoes/{id}` | LIDERANCA | detalhe por orientação |
| GET | `/dashboard/projetos/{id}` | LIDERANCA | detalhe por projeto |

Swagger em `/swagger-ui.html` · health em `/actuator/health`.

---

## 7. Backend — estrutura e tarefas

```
backend/
├── src/main/java/br/com/aguiabranca/inovacao/
│   ├── config/        (OpenAPI, CORS, Mongo auditing, seed)
│   ├── security/      (JwtConfig, TokenService, entry point 401 e handler 403)
│   ├── controller/    (Auth, Orientacao, Ideia, Projeto, Dashboard)
│   ├── service/       (regras de negócio e permissões por dono)
│   ├── repository/    (MongoRepository + agregações)
│   ├── domain/        (documentos + enums)
│   ├── dto/           (request/response records + mappers)
│   └── exception/     (GlobalExceptionHandler, exceções de negócio)
├── src/test/java/...  (unitários de service + integração com Testcontainers)
├── Dockerfile · docker-compose.yml · .env.example · README.md
```

- [x] B1 Setup Spring Initializr (Boot 4.1.1, Java 21: webmvc, security, data-mongodb, validation, actuator, lombok, springdoc 3.1.1, testcontainers), Dockerfile multi-stage, Docker Compose (API + Mongo 8.2) — JWT entra no B2 (OAuth2 Resource Server)
- [x] B2 Auth: login com BCrypt (tempo constante p/ e-mail inexistente), JWT HS256 com expiração e claim `role`, validação via OAuth2 Resource Server, 401/403 em JSON padronizado, `/auth/me`, `@EnableMethodSecurity`
- [x] B3 Seed idempotente: 5 usuários (3 contas da v1 + 2 operadores), 3 orientações, 5 ideias, 11 projetos que reproduzem os KPIs da v1; índices `@Indexed` criados na inicialização
- [x] B4 Orientações: CRUD (escrita só para LIDERANCA), exclusão lógica, histórico automático (criação/atualização/exclusão), filtros `vigente` e `area`
- [x] B5 Ideias: CRUD do operador (só o autor e só enquanto `ENVIADA`), vínculo obrigatório com orientação ativa,
  filtros (status/prioridade/área/orientação), priorização e fluxo de status pelo gestor
  (`ENVIADA → TRIAGEM/ANALISE → DECISAO → PROJETO`, rejeição com motivo obrigatório; transição inválida = 409)
- [x] B6 Projetos: CRUD do gestor (liderança só consulta, operador sem acesso), vínculo obrigatório com orientação,
  origem opcional em ideia que esteja em `DECISAO` (marca a ideia como `PROJETO`, uma ideia por projeto),
  progresso com histórico de atualizações e registro de resultados (retorno, custo evitado, produtividade)
- [x] B7 Dashboard: painel do gestor (ideias por etapa e projetos ativos/atrasados) e visão executiva da liderança
  (ROI, lucro, investimento, retorno, custo evitado, produtividade, % no prazo), séries para gráficos
  (projetos e ideias por status, resultado por orientação ordenado por lucro) e detalhe por orientação e por projeto
- **Convenção de DTOs:** campos booleanos/numéricos opcionais usam wrapper (`Boolean`, `Integer`), nunca primitivo —
  no Jackson 3 um primitivo ausente ou nulo faz a requisição falhar com 400 antes da validação.

- [x] B8 Validação (Bean Validation), handler global de erros e OpenAPI com esquema Bearer — CORS não se aplica:
  o cliente é app nativo, não navegador
- [x] B9 Testes: 69 testes de integração com Testcontainers (autenticação, permissões, regras de dono,
  transições de status e cálculo dos indicadores)
- [x] B10 Containerização: Dockerfile multi-stage + docker-compose (API + Mongo 8.2) com healthcheck, tudo
  configurável por variáveis (`JWT_SECRET`, `JWT_EXPIRATION`, `MONGODB_URI`, `SEED_ENABLED`, `API_PORT`)
- [x] B11 CI no GitHub Actions: testes e empacotamento do backend + compilação do APK, publicado como artefato
- [x] B12 Collection do Postman em `docs/postman_collection.json`: 27 requisições, login que salva o token
  automaticamente e variáveis para trocar de perfil

## 8. App — tarefas de integração

- [x] A1 Importar código v1 para `app/` (sem lixo) — commit baseline + wrapper Gradle recriado (o jar da v1 estava vazio e faltava `gradlew`); `./gradlew assembleDebug` OK
- [x] A2 Retrofit + OkHttp (logging) + Gson, desugaring para `java.time`; `BuildConfig.API_BASE_URL` (padrão `http://10.0.2.2:8080/`,
  sobrescrevível com `-PapiBaseUrl=`), `networkSecurityConfig` liberando HTTP local — DataStore fica para o A5
- [x] A3 Camada de dados: `ApiService` (Retrofit), DTOs, interceptor com Bearer, erros da API traduzidos para mensagem legível,
  `InovacaoRepository` único e **`MockRepository` removido**; ViewModels com carregamento e erro
- [x] A4 Formatação de moeda, datas e percentuais no app (`Formatadores`), com enums alinhados à API
- [x] A5 Login real: token e usuário persistidos em DataStore, sessão restaurada e validada com `/auth/me` na abertura,
  401 durante o uso derruba a sessão e volta ao login com aviso; SSO e credenciais de teste removidos da tela
- [x] A6 Estados de UI: carregando, erro com "Tentar de novo" e lista vazia nas telas dos três perfis — pull-to-refresh fica para o A15
- [x] **Operador**
  - [x] A7 Minhas ideias (só as próprias, vindas da API) + tela de detalhe com status, prioridade e retorno do gestor
  - [x] A8 Formulário único de cadastro e edição com seletor de orientação estratégica; exclusão com confirmação.
    Edição e exclusão só aparecem enquanto a ideia está `ENVIADA`, como a API exige
- [x] **Gestor**
  - [x] A9 Painel com KPIs reais vindos de `/api/dashboard/gestor`, com atalhos para a fila e para os projetos
  - [x] A10 Fila com filtros funcionais (etapa, prioridade e área), tela de análise da ideia com priorização,
    avanço de etapa e rejeição exigindo motivo digitado pelo gestor
  - [x] A11 Projetos: lista, cadastro, edição, exclusão com confirmação, atualização de progresso (etapa, %, status,
    observação) e registro de resultados (retorno, custo evitado, produtividade)
- [x] **Liderança**
  - [x] A12 Dashboard com KPIs reais + gráficos desenhados em Canvas (sem biblioteca externa): lucro por orientação,
    projetos por situação e ideias por etapa
  - [x] A13 CRUD de orientações pela liderança (criar, editar, excluir com confirmação) + tela de histórico de alterações
  - [x] A14 Andamento com retorno financeiro no card e tela de retorno do projeto (investimento, retorno, lucro,
    ROI, custo evitado e produtividade), além do consolidado por orientação no dashboard
- [x] A15 Navegação: um grafo por perfil (operador, gestor, liderança). O ViewModel é criado ao entrar no grafo e
  compartilhado entre as telas daquele perfil, então entrar como operador não dispara as chamadas dos outros perfis
  (que voltariam 403). Trocar de usuário leva para a home do novo perfil
- [x] A16 APK da entrega gerado e verificado (`com.aguiabranca.inovacao` 2.0.0, minSdk 24, target 35,
  apontando para a API local) e **fluxo dos 3 perfis percorrido no emulador** com a API no ar

## 9. Documentação e entrega

- [x] D1 `README.md` raiz: pré-requisitos, passo a passo (Docker + Android Studio), usuários de teste,
  roteiro de avaliação por perfil, diagrama de arquitetura, endpoints, regras de negócio, testes,
  configuração e solução de problemas — validado executando o ambiente do zero
- [x] D2 `backend/README.md` com execução via Docker, variáveis, usuários de teste e como rodar os testes
- [x] D3 `docs/ARQUITETURA.md`: diagramas de arquitetura, fluxo de autenticação, domínio e ciclo de vida da ideia,
  além das decisões técnicas e seus porquês
- [x] D4 `docs/ENDPOINTS.md`: 27 endpoints com método, parâmetros, corpo e respostas, gerado a partir do OpenAPI,
  com a tabela de códigos de erro comuns
- [x] D5 Apresentação em `docs/APRESENTACAO.pptx` e `.pdf` (11 slides) com nomes e RMs na capa e os prints
  dos três perfis no slide 5
- [ ] D6 Vídeo de demonstração dos 3 perfis (recomendado, a v1 teve)
- [x] D7 `./empacotar-entrega.sh` gera `entrega/` com backend.zip, app.zip (com o APK dentro e solto) e
  documentacao.zip — testado: nenhum arquivo de build entrou nos pacotes
- [x] D8 Checklist verificado (18/09): compose sobe do zero, 69 testes passando, APK instala, os 3 perfis logam,
  nenhum mock restou e `./empacotar-entrega.sh` gera a entrega final.
  Ajustes feitos na verificação: MongoDB 8.0 → 8.2 (a 8.0 não sobe no kernel do Docker Desktop atual) e
  logout que exibia "sessão expirada" por recarregar o painel sem token

## 10. Cronograma (hoje 14/09 → entrega 21/09)

| Dia | Backend | App | Docs |
|---|---|---|---|
| **Seg 14/09** | B1 | A1 | plano ✅ |
| **Ter 15/09** | B2 · B3 · B4 | A2 · A3 | |
| **Qua 16/09** | B5 · B6 | A4 · A5 · A6 · A7 · A8 | |
| **Qui 17/09** | B7 · B8 | A9 · A10 · A11 | |
| **Sex 18/09** | B10 · B11 | A12 · A13 · A14 · A15 | D3 · D4 |
| **Sáb 19/09** | B9 · B12 | A16 · testes ponta a ponta | D1 · D2 |
| **Dom 20/09** | correções | correções | D5 · D6 · D7 |
| **Seg 21/09** | — | — | D8 · **entregar até 23h59** (buffer) |

## 11. Critérios de avaliação × onde pontuamos

| Critério | Peso | Cobertura |
|---|---|---|
| Implementação técnica funcional | 50% | B2–B7, A3–A15 |
| Integração app ↔ backend | 15% | A3, A16, B10 |
| Apresentação e documentação | 15% | D1–D6, Swagger |
| Qualidade de código e boas práticas | 10% | camadas, DTOs, validação, testes B9, CI B11 |
| Criatividade e inovação | 10% | gráficos, histórico, ideia → projeto (IA fica como extra futuro) |

## 12. Riscos

| Risco | Mitigação |
|---|---|
| Prazo curto, grupo com 1 integrante | priorizar obrigatórios; cortar extras (vídeo, CI) antes de funcionalidades |
| Correção não conseguir rodar o ambiente | README da raiz com passo a passo, pré-requisitos e solução de problemas; APK já aponta para a API local |
| Toolchain antiga do app (AGP 8.1.2 + SDK 35) | validar build logo no A1; atualizar AGP/Kotlin só se quebrar |
| HTTP bloqueado no Android | `networkSecurityConfig` liberando cleartext para o host de desenvolvimento |

## 13. Pendências

- [x] Integrantes e RMs do Grupo 42 (18/09): Mateus Bhering Beltrão Santos RM564760, Ariel Alves Amaral RM564563,
  Wesley Santos de França RM563666, Ronaldo dos Santos Silva RM561414, Rodrigo Kumamoto Rêgo RM566049
- [x] **Forma de entrega definida (17/09):** correção roda localmente com Docker; nenhuma hospedagem necessária
- [ ] Reavaliar Diferencial IA se sobrar tempo
