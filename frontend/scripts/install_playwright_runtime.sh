#!/usr/bin/env bash

set -euo pipefail

if ! command -v apt >/dev/null 2>&1 || ! command -v dpkg-deb >/dev/null 2>&1; then
    echo "Skip Playwright runtime install: apt or dpkg-deb is not available."
    exit 0
fi

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd "${script_dir}/.." && pwd)"
runtime_dir="${project_dir}/.playwright-runtime"
tmp_dir="$(mktemp -d)"
packages=(
    libnspr4
    libnss3
    libasound2t64
)

trap 'rm -rf "${tmp_dir}"' EXIT

mkdir -p "${runtime_dir}"

(
    cd "${tmp_dir}"
    apt download "${packages[@]}" >/dev/null
    for deb_file in ./*.deb; do
        dpkg-deb -x "${deb_file}" "${runtime_dir}"
    done
)

echo "Playwright runtime libraries are ready in ${runtime_dir}"
