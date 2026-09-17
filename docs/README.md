# Documentação

| Arquivo | Conteúdo |
|---|---|
| [ARQUITETURA.md](ARQUITETURA.md) | diagramas de arquitetura, autenticação, domínio e ciclo de vida da ideia |
| [ENDPOINTS.md](ENDPOINTS.md) | especificação de cada rota: método, parâmetros, corpo e respostas |
| [postman_collection.json](postman_collection.json) | collection pronta para testar a API |
| [openapi.json](openapi.json) | contrato OpenAPI, gerado pela própria API |

Os três últimos são gerados a partir da API no ar, então não saem do lugar quando um endpoint muda.
Para regerá-los, suba a API (`cd backend && docker compose up`) e baixe `http://localhost:8080/v3/api-docs`.

## Usando a collection no Postman

1. Suba a API: `cd backend && docker compose up`.
2. No Postman, `Import` e escolha `postman_collection.json`.
3. Rode **1. Autenticação → Login**. O token é salvo sozinho e passa a ser usado nas demais requisições.
4. Para testar outro perfil, troque as variáveis `email` e `senha` da collection e rode o login de novo.

As rotas com `{id}` usam a variável `id` da collection: copie um identificador de uma listagem e cole nela.

## Documentação viva

Com a API no ar, o Swagger permite testar tudo pelo navegador, incluindo o botão **Authorize**
para colar o token: http://localhost:8080/swagger-ui.html
