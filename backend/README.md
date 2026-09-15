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
