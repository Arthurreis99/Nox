# Segurança

## Princípios

- Somente conexões HTTPS são permitidas.
- Esquemas externos como `file:`, `javascript:` e `intent:` são rejeitados.
- Dados de navegação não entram no backup do Android.
- Depuração remota do GeckoView existe apenas em builds `debug`.
- Extensões são empacotadas dentro do APK e carregadas como componentes internos.

## Atualizações

Atualizações do GeckoView e do uBlock Origin devem passar pelos testes, pelo lint Android e por uma compilação limpa. Use `scripts/update-ublock.sh` para obter a versão pública atual do AMO e registrar seu hash.

## Relatos

Não publique cookies, credenciais, tokens, históricos ou capturas que revelem dados de conta ao abrir um problema.
