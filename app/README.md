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
2. Aguarde a sincronização do Gradle.
3. `Run → Run 'app'` em um emulador.

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

## Pendências desta etapa

- O token ainda vive apenas em memória: fechar o app exige novo login (entra junto com a tela de login definitiva).
- O botão **Rejeitar** envia um motivo padrão; a tela de justificativa entra na evolução da fila do gestor.
- O botão **SSO** e o texto de credenciais de teste continuam na tela de login e serão removidos.
