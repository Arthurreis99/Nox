#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ublock_xpi="$project_dir/third_party/ublock-origin-1.74.0.xpi"

(cd "$project_dir/third_party" && sha256sum --check ublock-origin-1.74.0.sha256)
unzip -p "$ublock_xpi" manifest.json | jq -e \
  '.version == "1.74.0" and .browser_specific_settings.gecko.id == "uBlock0@raymondhill.net"' \
  >/dev/null
echo "uBlock Origin package validation passed"

node "$project_dir/app/src/test/js/filter-core.test.js"
node --check "$project_dir/app/src/main/assets/extensions/noxshield/content.js"
node --check "$project_dir/app/src/main/assets/extensions/noxshield/background.js"
jq -e '.manifest_version == 2' "$project_dir/app/src/main/assets/extensions/noxshield/manifest.json" >/dev/null
jq -e '.version == "1.1.0"' "$project_dir/app/src/main/assets/extensions/noxshield/manifest.json" >/dev/null
jq -e '.browser_specific_settings.gecko.id == "noxshield@arthurreis.dev"' \
  "$project_dir/app/src/main/assets/extensions/noxshield/manifest.json" >/dev/null
echo "Nox Shield manifest validation passed"
