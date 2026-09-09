#!/usr/bin/env sh
set -eu
HERE="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
ARCHIVE="$HERE/PROJECT_DARK_MASTER_DB.tar.gz"
DEST="${1:-$HERE/extracted}"
mkdir -p "$DEST"
tar -xzf "$ARCHIVE" -C "$DEST"
echo "PROJECT DARK Master DB extracted to: $DEST"
