#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
target_dir="$project_dir/third_party"
work_dir="$(mktemp -d)"
trap 'rm -rf "$work_dir"' EXIT

curl -fsSL "https://addons.mozilla.org/api/v5/addons/addon/ublock-origin/" \
  -o "$work_dir/addon.json"

version="$(jq -r '.current_version.version' "$work_dir/addon.json")"
download_url="$(jq -r '.current_version.file.url' "$work_dir/addon.json")"
expected_hash="$(jq -r '.current_version.file.hash' "$work_dir/addon.json" | cut -d: -f2)"

curl -fsSL "$download_url" -o "$work_dir/ublock.xpi"
actual_hash="$(sha256sum "$work_dir/ublock.xpi" | cut -d' ' -f1)"

if [[ "$actual_hash" != "$expected_hash" ]]; then
  echo "uBlock Origin checksum mismatch" >&2
  exit 1
fi

mkdir -p "$target_dir"
target_file="$target_dir/ublock-origin-$version.xpi"
mv "$work_dir/ublock.xpi" "$target_file"
printf '%s  %s\n' "$actual_hash" "$(basename "$target_file")" \
  > "$target_dir/ublock-origin-$version.sha256"

echo "Updated uBlock Origin to $version"
echo "SHA-256: $actual_hash"
