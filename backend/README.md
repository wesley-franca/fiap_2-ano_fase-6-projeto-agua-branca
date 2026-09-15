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

O MongoDB do compose não expõe porta na sua máquina (só a API o acessa), evitando conflito com um MongoDB já instalado.

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
└── config/     segurança (stateless) e OpenAPI
```
