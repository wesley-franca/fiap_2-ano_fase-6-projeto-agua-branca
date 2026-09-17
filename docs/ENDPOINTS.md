# Especificação dos endpoints

Gerado a partir do OpenAPI da API (`/v3/api-docs`). Documentação interativa: `/swagger-ui.html`.

Todas as rotas, exceto o login, exigem o cabeçalho `Authorization: Bearer <token>`.

Erros seguem o mesmo formato:

```json
{ "timestamp": "2026-09-17T00:00:00Z", "status": 403, "error": "Forbidden",
  "message": "Seu perfil não tem permissão para esta operação", "path": "/api/projetos" }
```

Códigos de erro usados em todas as rotas:

| Código | Quando acontece |
|---|---|
| 400 | dados inválidos (a resposta traz a lista de campos) ou corpo malformado |
| 401 | sem token, token inválido ou expirado; no login, e-mail ou senha incorretos |
| 403 | o perfil do usuário não pode executar a operação |
| 404 | registro não encontrado |
| 409 | operação incompatível com o estado atual (ex.: transição de status inválida) |

As listas abaixo trazem apenas as respostas de sucesso, como declaradas pelo OpenAPI.

## Autenticação

### `POST /api/auth/login`

Autentica com e-mail e senha e retorna um token JWT

**Corpo da requisição**

```json
{
  "email": "email",
  "senha": "texto"
}
```

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"token": "texto", "tipo": "texto", "expiraEm": "date-time", "usuario": "UsuarioResponse"}` |

### `GET /api/auth/me`

Retorna os dados do usuário autenticado

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"id": "texto", "nome": "texto", "email": "texto", "role": "OPERADOR | GESTOR | LIDERANCA", "area": "texto"}` |

## Dashboards e relatórios

### `GET /api/dashboard/gestor`

KPIs do painel do gestor

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"ideiasNovas": "número", "ideiasEmAnalise": "número", "ideiasAprovadas": "número", "ideiasRejeitadas": "nú...` |

### `GET /api/dashboard/orientacoes`

Resultado consolidado por orientação estratégica

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `[{"orientacaoId": "texto", "titulo": "texto", "campanha": "texto", "area": "texto", "ideias": "número", "in...` |

### `GET /api/dashboard/orientacoes/{id}`

Resultado de uma orientação específica

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"orientacaoId": "texto", "titulo": "texto", "campanha": "texto", "area": "texto", "ideias": "número", "ind...` |

### `GET /api/dashboard/projetos/{id}`

Retorno detalhado de um projeto

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"id": "texto", "nome": "texto", "status": "NO_PRAZO | ATRASADO | CONCLUIDO | CANCELADO", "etapa": "número"...` |

### `GET /api/dashboard/resumo`

Visão executiva: indicadores gerais e séries para gráficos

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"indicadores": "IndicadoresResponse", "projetosPorStatus": "object", "ideiasPorStatus": "object", "porOrie...` |

## Ideias de inovação

### `GET /api/ideias`

Lista ideias: operador vê apenas as próprias; gestor e liderança veem todas

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `status` | query | ENVIADA | TRIAGEM | ANALISE | DECISAO | PROJETO | REJEITADA | não |
| `prioridade` | query | BAIXA | MEDIA | ALTA | não |
| `area` | query | texto | não |
| `orientacaoId` | query | texto | não |

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `[{"id": "texto", "titulo": "texto", "categoria": "texto", "problemaObservado": "texto", "suaProposta": "tex...` |

### `POST /api/ideias`

Cadastra uma ideia vinculada a uma orientação (apenas operador)

**Corpo da requisição**

```json
{
  "titulo": "texto",
  "categoria": "texto",
  "problemaObservado": "texto",
  "suaProposta": "texto",
  "impacto": "BAIXO | MEDIO | ALTO",
  "orientacaoId": "texto"
}
```

**Respostas**

| Código | Conteúdo |
|---|---|
| 201 | `{"id": "texto", "titulo": "texto", "categoria": "texto", "problemaObservado": "texto", "suaProposta": "text...` |

### `GET /api/ideias/{id}`

Detalha uma ideia (operador apenas as próprias)

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"id": "texto", "titulo": "texto", "categoria": "texto", "problemaObservado": "texto", "suaProposta": "text...` |

### `PUT /api/ideias/{id}`

Edita a própria ideia enquanto ela estiver como ENVIADA

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Corpo da requisição**

```json
{
  "titulo": "texto",
  "categoria": "texto",
  "problemaObservado": "texto",
  "suaProposta": "texto",
  "impacto": "BAIXO | MEDIO | ALTO",
  "orientacaoId": "texto"
}
```

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"id": "texto", "titulo": "texto", "categoria": "texto", "problemaObservado": "texto", "suaProposta": "text...` |

### `DELETE /api/ideias/{id}`

Exclui a própria ideia enquanto ela estiver como ENVIADA

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Respostas**

| Código | Conteúdo |
|---|---|
| 204 | `—` |

### `PATCH /api/ideias/{id}/prioridade`

Define a prioridade da ideia (apenas gestor)

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Corpo da requisição**

```json
{
  "prioridade": "BAIXA | MEDIA | ALTA"
}
```

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"id": "texto", "titulo": "texto", "categoria": "texto", "problemaObservado": "texto", "suaProposta": "text...` |

### `PATCH /api/ideias/{id}/status`

Avança, aprova ou rejeita a ideia (apenas gestor)

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Corpo da requisição**

```json
{
  "status": "ENVIADA | TRIAGEM | ANALISE | DECISAO | PROJETO | REJEITADA",
  "comentario": "texto"
}
```

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"id": "texto", "titulo": "texto", "categoria": "texto", "problemaObservado": "texto", "suaProposta": "text...` |

## Orientações estratégicas

### `GET /api/orientacoes`

Lista as orientações estratégicas (todos os perfis)

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `vigente` | query | true/false | não |
| `area` | query | texto | não |

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `[{"id": "texto", "titulo": "texto", "descricao": "texto", "categoria": "texto", "campanha": "texto", "area"...` |

### `POST /api/orientacoes`

Cria uma orientação (apenas liderança)

**Corpo da requisição**

```json
{
  "titulo": "texto",
  "descricao": "texto",
  "categoria": "texto",
  "campanha": "texto",
  "area": "texto",
  "periodo": "texto",
  "indicadores": [
    "texto"
  ],
  "vigente": "true/false"
}
```

**Respostas**

| Código | Conteúdo |
|---|---|
| 201 | `{"id": "texto", "titulo": "texto", "descricao": "texto", "categoria": "texto", "campanha": "texto", "area":...` |

### `GET /api/orientacoes/{id}`

Detalha uma orientação (todos os perfis)

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"id": "texto", "titulo": "texto", "descricao": "texto", "categoria": "texto", "campanha": "texto", "area":...` |

### `PUT /api/orientacoes/{id}`

Atualiza uma orientação (apenas liderança)

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Corpo da requisição**

```json
{
  "titulo": "texto",
  "descricao": "texto",
  "categoria": "texto",
  "campanha": "texto",
  "area": "texto",
  "periodo": "texto",
  "indicadores": [
    "texto"
  ],
  "vigente": "true/false"
}
```

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"id": "texto", "titulo": "texto", "descricao": "texto", "categoria": "texto", "campanha": "texto", "area":...` |

### `DELETE /api/orientacoes/{id}`

Exclui (logicamente) uma orientação (apenas liderança)

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Respostas**

| Código | Conteúdo |
|---|---|
| 204 | `—` |

### `GET /api/orientacoes/{id}/historico`

Histórico de alterações da orientação (todos os perfis)

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `[{"data": "date-time", "acao": "CRIACAO | ATUALIZACAO | EXCLUSAO", "titulo": "texto", "categoria": "texto",...` |

## Projetos e iniciativas

### `GET /api/projetos`

Lista os projetos (gestor e liderança)

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `status` | query | NO_PRAZO | ATRASADO | CONCLUIDO | CANCELADO | não |
| `orientacaoId` | query | texto | não |

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `[{"id": "texto", "nome": "texto", "descricao": "texto", "responsavelId": "texto", "responsavelNome": "texto...` |

### `POST /api/projetos`

Cadastra um projeto, opcionalmente a partir de uma ideia em DECISAO

**Corpo da requisição**

```json
{
  "nome": "texto",
  "descricao": "texto",
  "orientacaoId": "texto",
  "ideiaOrigemId": "texto",
  "totalEtapas": "número",
  "dataInicio": "date",
  "prazo": "date",
  "investimento": "número",
  "prazoCoerente": "true/false"
}
```

**Respostas**

| Código | Conteúdo |
|---|---|
| 201 | `{"id": "texto", "nome": "texto", "descricao": "texto", "responsavelId": "texto", "responsavelNome": "texto"...` |

### `GET /api/projetos/{id}`

Detalha um projeto, com o histórico de atualizações

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"id": "texto", "nome": "texto", "descricao": "texto", "responsavelId": "texto", "responsavelNome": "texto"...` |

### `PUT /api/projetos/{id}`

Atualiza os dados do projeto (apenas gestor)

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Corpo da requisição**

```json
{
  "nome": "texto",
  "descricao": "texto",
  "orientacaoId": "texto",
  "ideiaOrigemId": "texto",
  "totalEtapas": "número",
  "dataInicio": "date",
  "prazo": "date",
  "investimento": "número",
  "prazoCoerente": "true/false"
}
```

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"id": "texto", "nome": "texto", "descricao": "texto", "responsavelId": "texto", "responsavelNome": "texto"...` |

### `DELETE /api/projetos/{id}`

Exclui um projeto (apenas gestor)

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Respostas**

| Código | Conteúdo |
|---|---|
| 204 | `—` |

### `PATCH /api/projetos/{id}/progresso`

Atualiza etapa, progresso e status, registrando o acompanhamento

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Corpo da requisição**

```json
{
  "etapa": "número",
  "progresso": "número",
  "status": "NO_PRAZO | ATRASADO | CONCLUIDO | CANCELADO",
  "observacao": "texto"
}
```

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"id": "texto", "nome": "texto", "descricao": "texto", "responsavelId": "texto", "responsavelNome": "texto"...` |

### `PATCH /api/projetos/{id}/resultados`

Registra os resultados obtidos (retorno, custo evitado e produtividade)

**Parâmetros**

| Nome | Em | Tipo | Obrigatório |
|---|---|---|---|
| `id` | path | texto | sim |

**Corpo da requisição**

```json
{
  "retornoFinanceiro": "número",
  "custoEvitado": "número",
  "aumentoProdutividade": "número"
}
```

**Respostas**

| Código | Conteúdo |
|---|---|
| 200 | `{"id": "texto", "nome": "texto", "descricao": "texto", "responsavelId": "texto", "responsavelNome": "texto"...` |

