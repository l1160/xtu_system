#!/usr/bin/env bash

set -euo pipefail

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-xtu_system}"
DB_USERNAME="${DB_USERNAME:-root}"
DB_PASSWORD="${DB_PASSWORD:-123456}"
BACKUP_DIR="${BACKUP_DIR:-/opt/xtu-system/backup}"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
OUTPUT_FILE="${BACKUP_DIR}/${DB_NAME}_${TIMESTAMP}.sql.gz"

mkdir -p "${BACKUP_DIR}"

MYSQL_PWD="${DB_PASSWORD}" mysqldump \
    -h "${DB_HOST}" \
    -P "${DB_PORT}" \
    -u "${DB_USERNAME}" \
    --single-transaction \
    --routines \
    --triggers \
    "${DB_NAME}" | gzip > "${OUTPUT_FILE}"

echo "Backup created: ${OUTPUT_FILE}"
