#!/usr/bin/env bash

set -euo pipefail

if [ "$#" -ne 1 ]; then
    echo "Usage: DB_PASSWORD=123456 $0 /path/to/backup.sql.gz"
    exit 1
fi

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-xtu_system}"
DB_USERNAME="${DB_USERNAME:-root}"
DB_PASSWORD="${DB_PASSWORD:-123456}"
BACKUP_FILE="$1"

if [ ! -f "${BACKUP_FILE}" ]; then
    echo "Backup file not found: ${BACKUP_FILE}"
    exit 1
fi

MYSQL_PWD="${DB_PASSWORD}" mysql \
    -h "${DB_HOST}" \
    -P "${DB_PORT}" \
    -u "${DB_USERNAME}" \
    -e "CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

if [[ "${BACKUP_FILE}" == *.gz ]]; then
    gzip -dc "${BACKUP_FILE}" | MYSQL_PWD="${DB_PASSWORD}" mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USERNAME}" "${DB_NAME}"
else
    MYSQL_PWD="${DB_PASSWORD}" mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USERNAME}" "${DB_NAME}" < "${BACKUP_FILE}"
fi

echo "Restore completed: ${BACKUP_FILE}"
