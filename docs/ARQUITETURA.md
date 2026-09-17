# Arquitetura

## Visão geral

```mermaid
flowchart TB
    subgraph Dispositivo["Aparelho Android"]
        UI["Telas · Jetpack Compose"]
        VM["ViewModels · StateFlow"]
        REPO["InovacaoRepository"]
        HTTP["Retrofit + OkHttp<br/>interceptor com o token"]
        STORE["DataStore<br/>token e usuário"]
        UI <--> VM <--> REPO --> HTTP
        REPO <--> STORE
    end

    HTTP -->|"JSON · Bearer JWT"| FILTRO

    subgraph Servidor["API · Spring Boot 4 (container)"]
        FILTRO["Spring Security<br/>valida o JWT e o perfil"]
        CTRL["Controllers<br/>/api/..."]
        SERV["Services<br/>regras de negócio"]
        REP["Repositories<br/>Spring Data"]
        FILTRO --> CTRL --> SERV --> REP
    end

    REP --> MONGO[("MongoDB<br/>(container)")]
```

Os dois containers sobem juntos com `docker compose up` em `backend/`. O app, no emulador, alcança a API
pelo endereço `10.0.2.2`, que aponta para o computador.

## Autenticação e permissões

```mermaid
sequenceDiagram
    participant App
    participant API
    participant Mongo

    App->>API: POST /api/auth/login (e-mail e senha)
    API->>Mongo: busca o usuário pelo e-mail
    Mongo-->>API: usuário com a senha criptografada (BCrypt)
    API->>API: confere a senha e assina o JWT (HS256, 8h)
    API-->>App: token + dados do usuário
    App->>App: guarda o token no aparelho

    App->>API: GET /api/projetos (Authorization: Bearer ...)
    API->>API: valida o token e lê o perfil
    alt perfil autorizado
        API->>Mongo: consulta
        Mongo-->>API: dados
        API-->>App: 200
    else perfil sem permissão
        API-->>App: 403 com mensagem explicativa
    end
```

O perfil do usuário viaja dentro do token, e cada rota declara quem pode acessá-la. O app também organiza
a navegação por perfil, então cada pessoa só chega às telas que lhe cabem — mas quem decide é sempre a API.

## Domínio

```mermaid
erDiagram
    USUARIO ||--o{ IDEIA : "cadastra"
    USUARIO ||--o{ PROJETO : "é responsável"
    ORIENTACAO ||--o{ IDEIA : "orienta"
    ORIENTACAO ||--o{ PROJETO : "orienta"
    IDEIA ||--o| PROJETO : "pode originar"

    USUARIO {
        string nome
        string email "único"
        string senhaHash "BCrypt"
        enum role "OPERADOR, GESTOR, LIDERANCA"
        string area
    }
    ORIENTACAO {
        string titulo
        string categoria
        string campanha
        string periodo
        bool vigente
        bool ativo "exclusão lógica"
        array historico "data, ação, autor"
    }
    IDEIA {
        string titulo
        string problemaObservado
        string suaProposta
        enum status "ENVIADA a PROJETO ou REJEITADA"
        enum prioridade "BAIXA, MEDIA, ALTA"
        string comentarioAvaliacao
    }
    PROJETO {
        string nome
        int etapa
        int progresso
        enum status "NO_PRAZO, ATRASADO, CONCLUIDO, CANCELADO"
        decimal investimento
        decimal retornoFinanceiro
        decimal custoEvitado
        array atualizacoes "histórico de progresso"
    }
```

## Ciclo de vida da ideia

```mermaid
stateDiagram-v2
    [*] --> ENVIADA: operador cadastra
    ENVIADA --> TRIAGEM: gestor
    ENVIADA --> ANALISE: gestor
    TRIAGEM --> ANALISE: gestor
    ANALISE --> DECISAO: gestor
    DECISAO --> PROJETO: aprovada, vira projeto
    ENVIADA --> REJEITADA: com motivo
    TRIAGEM --> REJEITADA: com motivo
    ANALISE --> REJEITADA: com motivo
    DECISAO --> REJEITADA: com motivo
    PROJETO --> [*]
    REJEITADA --> [*]
```

O operador só edita ou exclui a ideia enquanto ela está em `ENVIADA`. Qualquer transição fora deste fluxo
é recusada pela API com 409.

## Camadas do backend

| Camada | Responsabilidade |
|---|---|
| `controller` | recebe a requisição, valida o formato e declara qual perfil pode acessar |
| `service` | regras de negócio: quem é dono do quê, transições de status, cálculo dos indicadores |
| `repository` | acesso ao MongoDB |
| `domain` | documentos e enums |
| `dto` | contratos de entrada e saída, separados dos documentos |
| `security` | emissão e validação do JWT, respostas 401 e 403 em JSON |
| `exception` | tratamento global de erros no formato padronizado |
| `config` | segurança, OpenAPI, índices do banco e dados de demonstração |

## Decisões e porquês

| Decisão | Motivo |
|---|---|
| MongoDB com Spring Data | o desafio pede banco NoSQL; o equivalente ao JPA no mundo Mongo |
| JWT pelo próprio Spring Security | evita biblioteca extra e filtro escrito à mão |
| Indicadores calculados, nunca guardados | lucro e ROI nunca ficam inconsistentes com investimento e retorno |
| Valores financeiros em decimal | evita erro de arredondamento em dinheiro |
| Exclusão lógica de orientações | preserva o histórico e os vínculos com ideias e projetos |
| Gráficos desenhados em Canvas | sem dependência externa no app |
| Um grafo de navegação por perfil | cada perfil só carrega os dados que pode ver |
