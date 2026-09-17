# Backend — API Plataforma de Inovação

Java 21 · Spring Boot 4 · Spring Security · Spring Data MongoDB · springdoc-openapi · Docker Compose.

## Executar com Docker (recomendado)

Pré-requisito: [Docker](https://docs.docker.com/get-docker/) com Docker Compose. Não é preciso Java, Maven nem MongoDB instalados.

```bash
cd backend
docker compose up --build
```

A primeira execução baixa imagens e dependências e pode levar alguns minutos.

| Recurso | URL |
|---|---|
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Health | http://localhost:8080/actuator/health |

Parar: `docker compose down` · Parar e apagar os dados do banco: `docker compose down -v`

### Configuração

Todas as variáveis têm valor padrão. Para alterar, copie `.env.example` para `.env`:

| Variável | Padrão | Descrição |
|---|---|---|
| `API_PORT` | `8080` | porta da API na sua máquina |
| `MONGODB_URI` | `mongodb://mongo:27017/inovacao` | conexão da API com o MongoDB |
| `JWT_SECRET` | segredo de desenvolvimento | chave HS256 dos tokens (mín. 32 caracteres) — **troque fora do ambiente local** |
| `JWT_EXPIRATION` | `8h` | validade do token |
| `SEED_ENABLED` | `true` | cria usuários e dados de demonstração na inicialização |

O MongoDB do compose não expõe porta na sua máquina (só a API o acessa), evitando conflito com um MongoDB já instalado.

## Usuários de teste

Criados automaticamente pelo seed (senha de todos: `senha123`):

| E-mail | Perfil | Nome |
|---|---|---|
| `operador@aguiabranca.com` | OPERADOR | João Costa |
| `ana.lima@aguiabranca.com` | OPERADOR | Ana Lima |
| `roberto.mendes@aguiabranca.com` | OPERADOR | Roberto Mendes |
| `gestor@aguiabranca.com` | GESTOR | Maria Silva |
| `lideranca@aguiabranca.com` | LIDERANCA | Paulo Andrade |

O seed também cria 3 orientações estratégicas, 5 ideias e 11 projetos (valores que reproduzem os KPIs do
dashboard da v1). Ele só insere o que estiver faltando, então reiniciar a API não duplica dados.

## Autenticação

```bash
# 1. Login → token JWT
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"gestor@aguiabranca.com","senha":"senha123"}'

# 2. Usar o token nas demais rotas
curl -s http://localhost:8080/api/auth/me -H "Authorization: Bearer <token>"
```

No Swagger, clique em **Authorize** e cole o token. Erros seguem o formato:

```json
{ "timestamp": "...", "status": 401, "error": "Unauthorized", "message": "E-mail ou senha inválidos", "path": "/api/auth/login" }
```

## Endpoints

| Método | Rota | Perfis |
|---|---|---|
| POST | `/api/auth/login` | público |
| GET | `/api/auth/me` | autenticado |
| GET | `/api/orientacoes?vigente=&area=` | todos |
| GET | `/api/orientacoes/{id}` | todos |
| GET | `/api/orientacoes/{id}/historico` | todos |
| POST | `/api/orientacoes` | LIDERANCA |
| PUT | `/api/orientacoes/{id}` | LIDERANCA |
| DELETE | `/api/orientacoes/{id}` | LIDERANCA |
| GET | `/api/ideias?status=&prioridade=&area=&orientacaoId=` | todos (operador vê só as próprias) |
| GET | `/api/ideias/{id}` | autor, GESTOR, LIDERANCA |
| POST | `/api/ideias` | OPERADOR |
| PUT | `/api/ideias/{id}` | OPERADOR (autor, status `ENVIADA`) |
| DELETE | `/api/ideias/{id}` | OPERADOR (autor, status `ENVIADA`) |
| PATCH | `/api/ideias/{id}/prioridade` | GESTOR |
| PATCH | `/api/ideias/{id}/status` | GESTOR |
| GET | `/api/projetos?status=&orientacaoId=` | GESTOR, LIDERANCA |
| GET | `/api/projetos/{id}` | GESTOR, LIDERANCA |
| POST | `/api/projetos` | GESTOR |
| PUT | `/api/projetos/{id}` | GESTOR |
| PATCH | `/api/projetos/{id}/progresso` | GESTOR |
| PATCH | `/api/projetos/{id}/resultados` | GESTOR |
| DELETE | `/api/projetos/{id}` | GESTOR |
| GET | `/api/dashboard/gestor` | GESTOR |
| GET | `/api/dashboard/resumo` | LIDERANCA |
| GET | `/api/dashboard/orientacoes` | LIDERANCA |
| GET | `/api/dashboard/orientacoes/{id}` | LIDERANCA |
| GET | `/api/dashboard/projetos/{id}` | LIDERANCA |

**Ideias.** Toda ideia nasce como `ENVIADA`, com prioridade `MEDIA`, autoria vinda do token e vínculo
obrigatório a uma orientação ativa. O operador edita e exclui apenas as próprias e somente enquanto
ninguém tiver mexido nelas. O gestor prioriza e conduz o fluxo:

```
ENVIADA → TRIAGEM → ANALISE → DECISAO → PROJETO
   └────────┴─────────┴──────────┴──────→ REJEITADA (exige comentário)
```

Transição fora desse fluxo retorna 409. `PROJETO` e `REJEITADA` são finais.

**Projetos.** O gestor cadastra e mantém; a liderança acompanha; o operador não tem acesso. Todo projeto é
vinculado a uma orientação e pode nascer de uma ideia que esteja em `DECISAO` — nesse caso a ideia passa a
`PROJETO` e não pode originar outro. Cada atualização de progresso (etapa, percentual, status e observação)
entra no histórico do projeto, e os resultados (retorno financeiro, custo evitado e aumento de produtividade)
alimentam os indicadores da liderança.

**Dashboards.** O gestor vê quantas ideias estão em cada etapa e quantos projetos estão ativos ou atrasados.
A liderança vê os indicadores consolidados — investimento, retorno, lucro, ROI, custo evitado, produtividade
média e percentual no prazo — junto com séries prontas para gráficos: projetos e ideias por status e resultado
por orientação, ordenado por lucro. Também é possível abrir o detalhe de uma orientação ou de um projeto.

`ROI = (retorno − investimento) / investimento`, e o lucro é sempre calculado, nunca armazenado. Projeto sem
investimento registrado vem com `roi: null`, em vez de um número inventado.

A exclusão de orientação é lógica: ela some das consultas, mas o histórico e os vínculos com ideias e
projetos são preservados. Cada criação, alteração e exclusão gera um registro no histórico (data, ação,
título, categoria, campanha e autor).

## Executar sem Docker (desenvolvimento)

Pré-requisitos: JDK 21 e um MongoDB acessível em `localhost:27017`
(ex.: `docker run -d --name inovacao-mongo-dev -p 27017:27017 mongo:8.0`).

```bash
./mvnw spring-boot:run          # usa mongodb://localhost:27017/inovacao
./mvnw test                     # testes (usa Testcontainers, requer Docker)
```

Sem JDK 21 instalado, rode os testes em um container (Linux):

```bash
docker run --rm --network host -v "$PWD":/workspace -w /workspace \
  -v /var/run/docker.sock:/var/run/docker.sock -v inovacao-m2:/root/.m2 \
  eclipse-temurin:21-jdk ./mvnw -B test
```

## Estrutura

```
src/main/java/br/com/aguiabranca/inovacao/
├── config/       segurança, OpenAPI, índices do Mongo e seed
├── controller/   endpoints REST
├── domain/       documentos MongoDB e enums
├── dto/          contratos de entrada/saída
├── exception/    tratamento global de erros
├── repository/   Spring Data MongoDB
├── security/     JWT (emissão/validação) e respostas 401/403
└── service/      regras de negócio
```
