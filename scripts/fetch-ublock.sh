#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
target_dir="$project_dir/third_party"
target_file="$target_dir/ublock-origin-1.74.0.xpi"
expected_hash="175756d74468c9ba45863f7fc333d3be670f82d5b066314e915814dd547d1652"
download_url="https://addons.mozilla.org/firefox/downloads/file/4981431/ublock_origin-1.74.0.xpi"

mkdir -p "$target_dir"

if [[ -f "$target_file" ]] && \
   [[ "$(sha256sum "$target_file" | cut -d' ' -f1)" == "$expected_hash" ]]; then
    echo "uBlock Origin 1.74.0 already verified"
    exit 0
fi

temporary_file="$(mktemp "$target_dir/ublock-origin.XXXXXX.xpi")"
trap 'rm -f "$temporary_file"' EXIT
curl -fsSL --retry 3 "$download_url" -o "$temporary_file"

actual_hash="$(sha256sum "$temporary_file" | cut -d' ' -f1)"
if [[ "$actual_hash" != "$expected_hash" ]]; then
    echo "uBlock Origin checksum mismatch" >&2
    exit 1
fi

mv "$temporary_file" "$target_file"
echo "uBlock Origin 1.74.0 downloaded and verified"
