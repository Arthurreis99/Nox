# Distribuição pessoal

O Nox não depende da Play Store. Um APK pode ser instalado diretamente no Android, mas toda atualização precisa usar a mesma chave de assinatura da primeira instalação.

## 1. Criar a chave uma única vez

Guarde a chave e as senhas fora do repositório e mantenha um backup seguro.

```bash
keytool -genkeypair -v \
  -keystore nox-release.jks \
  -alias nox \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

## 2. Compilar e assinar

```bash
export NOX_KEYSTORE_PATH="/caminho/absoluto/nox-release.jks"
export NOX_KEY_ALIAS="nox"
export NOX_STORE_PASSWORD="sua-senha"
export NOX_KEY_PASSWORD="sua-senha-da-chave"
./gradlew clean testDebugUnitTest lintDebug assembleRelease
```

Os APKs assinados serão gerados em `app/build/outputs/apk/release/`. Use a variante `arm64-v8a` em celulares atuais. Sem as quatro variáveis, o Gradle ainda valida a compilação de release, mas produz APKs não assinados.

## 3. Verificar antes de instalar

```bash
apksigner verify --verbose --print-certs \
  app/build/outputs/apk/release/app-arm64-v8a-release.apk
```

Não versione arquivos `.jks`, `.keystore`, `keystore.properties` ou senhas. Eles já estão cobertos pelo `.gitignore`.
