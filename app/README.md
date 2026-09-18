# App de Inovação — Águia Branca (Android)

Kotlin · Jetpack Compose · Material 3 · MVVM · Retrofit.

Este é o app da Sprint 1 **integrado ao backend real**: não há mais dados em memória, todas as telas
consomem a API REST do diretório [`../backend`](../backend).

## Antes de rodar: suba a API

```bash
cd ../backend
docker compose up --build
```

## Rodar no Android Studio

1. `File → Open` e selecione a pasta `app/` deste repositório.
2. Em `Settings → Build, Execution, Deployment → Build Tools → Gradle`, confira se **Gradle JDK** é um
   **JDK 17** (veja a nota abaixo).
3. Aguarde a sincronização do Gradle.
4. `Run → Run 'app'` em um emulador.

> **Erro `Incompatible Gradle JVM version` ou falha no `jlink`?** O Android Studio recente embute o Java 21
> ou 25, e o AGP 8.1 usado aqui só compila com Java 17. Selecionar um JDK 17 no campo **Gradle JDK** resolve —
> o projeto em si não muda. O mesmo vale pela linha de comando: `JAVA_HOME=/caminho/do/jdk-17 ./gradlew assembleDebug`.
>
> Mantivemos Gradle 8.5 e AGP 8.1.2 de propósito: subir para uma versão que aceite o Java 25 exigiria
> AGP 9 e Kotlin 2.2, uma troca grande de toolchain sem ganho para o funcionamento do app.

No emulador, o endereço `10.0.2.2` aponta para o computador onde a API está rodando, que é o padrão do projeto.

## Rodar em um aparelho físico

O celular precisa alcançar a API pela rede local. Descubra o IP da sua máquina (`hostname -I` no Linux) e
gere o APK apontando para ele:

```bash
./gradlew assembleDebug -PapiBaseUrl=http://192.168.0.10:8080/
```

O APK fica em `app/build/outputs/apk/debug/app-debug.apk`.

## Usuários de teste

Senha de todos: `senha123`.

| E-mail | Perfil | O que vê |
|---|---|---|
| `operador@aguiabranca.com` | Operador | orientação vigente e as próprias ideias |
| `gestor@aguiabranca.com` | Gestor | painel, fila de ideias e projetos |
| `lideranca@aguiabranca.com` | Liderança | dashboard executivo, orientações e andamento |

## Estrutura

```
app/src/main/java/com/aguiabranca/inovacao/
├── MainActivity.kt          navegação (NavHost) por perfil
├── data/
│   ├── model/               modelos usados pelas telas
│   ├── remote/              ApiService (Retrofit), DTOs e cliente HTTP com o token
│   ├── repository/          InovacaoRepository — única fonte de dados
│   └── session/             token e usuário da sessão atual
├── ui/screens/              login, operador, gestor e liderança
├── ui/viewmodel/            estado das telas (carregando, erro, dados)
└── util/Formatadores.kt     moeda, datas e percentuais exibidos nas telas
```

## Integração com a API

- O login chama `POST /api/auth/login`; o token JWT volta e é enviado em todas as chamadas seguintes.
- Cada perfil só enxerga o que a API permite: o operador recebe apenas as próprias ideias, e o
  dashboard executivo é recusado para quem não é da liderança.
- Erros da API viram mensagem legível na tela (sessão expirada, sem permissão, servidor fora do ar).

## Sessão

O token e o usuário ficam salvos no aparelho (DataStore). Ao abrir o app, a sessão é restaurada e
confirmada com `GET /api/auth/me` antes de qualquer tela aparecer — por isso o login não pisca na abertura.
Se a API recusar o token durante o uso, o app derruba a sessão e volta ao login avisando que ela expirou.
Sair pelo botão de logout apaga o token do aparelho.

## O que cada perfil faz no app

| Perfil | Telas |
|---|---|
| Operador | orientação vigente, lista das próprias ideias, detalhe, cadastro e edição (com escolha da orientação) e exclusão |
| Gestor | painel com KPIs, fila de ideias com filtros por etapa, prioridade e área, análise da ideia (priorizar, avançar etapa, rejeitar com motivo), projetos (criar, editar, excluir, atualizar progresso e registrar resultados) |
| Liderança | dashboard executivo com gráficos, orientações estratégicas (criar, editar, excluir e histórico), andamento dos projetos e retorno detalhado de cada projeto |

Os gráficos são desenhados em Canvas, sem biblioteca externa: lucro por orientação (barras),
projetos por situação e ideias por etapa (distribuição com legenda).

## Navegação

Cada perfil tem seu próprio grafo de navegação. O estado das telas é compartilhado dentro do perfil e criado
somente ao entrar nele — um operador não dispara as consultas de gestor e liderança, que a API recusaria.

## Pendências desta etapa

- Datas nos formulários são digitadas no formato `2026-12-31`; falta um seletor de data.
