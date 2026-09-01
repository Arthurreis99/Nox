# Nox

Nox é um aplicativo Android dedicado à versão web móvel do YouTube, com foco principal em bloqueio de anúncios e redução de rastreamento. Ele usa o GeckoView — o mesmo mecanismo do Firefox — e inicia com as proteções ativadas.

> Projeto independente e não afiliado ao YouTube, Google, Mozilla ou aos mantenedores do uBlock Origin.

## Proteções

- uBlock Origin completo, integrado como extensão nativa do GeckoView.
- Camada complementar Nox Shield para banners e anúncios do player.
- Enhanced Tracking Protection em modo estrito.
- Isolamento de cookies de terceiros.
- Global Privacy Control e `DNT: 1`.
- Remoção de parâmetros de rastreamento em links.
- Bloqueio de HTTP sem criptografia.
- Sem Firebase, analytics, anúncios próprios ou servidores Nox.
- Dados e login armazenados somente no perfil local do aplicativo.

O login não torna a atividade anônima para o Google. Quando uma conta está conectada, o YouTube ainda pode associar pesquisas e visualizações a essa conta.

## Aparência

O design combina o grafite do GitHub com o vermelho do YouTube:

| Uso | Cor |
|---|---|
| Fundo | `#0D1117` |
| Superfície | `#161B22` |
| Superfície elevada | `#21262D` |
| Borda | `#30363D` |
| Texto | `#F0F6FC` |
| Texto secundário | `#8B949E` |
| Ação | `#FF0033` |
| Proteção ativa | `#3FB950` |

## Compilar

Requisitos: JDK 17 e Android SDK 37.1 para compilação. O wrapper já fixa o Gradle 9.5. O aplicativo continua compatível com Android 8 ou mais recente e tem `targetSdk 37`.

```bash
./scripts/fetch-ublock.sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Os APKs são gerados em `app/build/outputs/apk/debug/`. A variante `arm64-v8a` atende à maioria dos celulares Android atuais; a variante `x86_64` é destinada a emuladores.

O APK de CI é uma prévia assinada com chave de desenvolvimento. Para uma distribuição pessoal permanente, gere uma chave privada fora do repositório e assine a variante `release`; nunca versione o arquivo de chave nem sua senha.

O procedimento completo de assinatura e atualização está em [docs/RELEASE.md](docs/RELEASE.md).

Os testes da extensão podem ser executados separadamente:

```bash
node app/src/test/js/filter-core.test.js
```

## Componentes de terceiros

- GeckoView `154.0.20260824154132`, Mozilla Public License 2.0.
- uBlock Origin `1.74.0`, GNU GPL v3. O pacote oficial incorporado possui SHA-256 `175756d74468c9ba45863f7fc333d3be670f82d5b066314e915814dd547d1652`.

O pacote oficial do uBlock Origin é baixado por `scripts/fetch-ublock.sh`, validado pelo SHA-256 fixado e extraído pelo Gradle durante a compilação. A licença e os avisos permanecem dentro do XPI.

## Licença

GNU General Public License v3.0 only. Consulte [LICENSE](LICENSE).
