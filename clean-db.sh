#!/bin/bash

set -euo pipefail

DEFAULT_DB_URL="jdbc:mysql://localhost:3306/salon?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
DB_URL="${SALON_DB_URL:-$DEFAULT_DB_URL}"
DB_USER="${SALON_DB_USER:-root}"
DB_PASSWORD="${SALON_DB_PASSWORD:-password}"
MODE=""

usage() {
    cat <<'USAGE'
Usage:
  ./clean-db.sh --dry-run
  ./clean-db.sh --yes

Drops all application tables from the configured MySQL salon database.

Options:
  --dry-run  Print the resolved database target and SQL without executing it.
  --yes      Execute the database reset.
  --help     Show this help message.
USAGE
}

fail() {
    echo "Error: $*" >&2
    exit 1
}

while (($# > 0)); do
    case "$1" in
        --dry-run)
            [[ -z "$MODE" ]] || fail "Use only one of --dry-run or --yes."
            MODE="dry-run"
            ;;
        --yes)
            [[ -z "$MODE" ]] || fail "Use only one of --dry-run or --yes."
            MODE="execute"
            ;;
        --help|-h)
            usage
            exit 0
            ;;
        *)
            fail "Unknown option: $1. Use --help for usage."
            ;;
    esac
    shift
done

if [[ -z "$MODE" ]]; then
    usage
    fail "Refusing to clean the database without --yes. Use --dry-run to preview."
fi

parse_jdbc_url() {
    local url_without_query="${DB_URL%%\?*}"

    if [[ ! "$url_without_query" =~ ^jdbc:mysql://([^/:?]+)(:([0-9]+))?/([^/?]+)$ ]]; then
        fail "Unsupported SALON_DB_URL. Expected jdbc:mysql://host:port/database?params."
    fi

    DB_HOST="${BASH_REMATCH[1]}"
    DB_PORT="${BASH_REMATCH[3]:-3306}"
    DB_NAME="${BASH_REMATCH[4]}"

    [[ -n "$DB_NAME" ]] || fail "Could not parse database name from SALON_DB_URL."
}

parse_jdbc_url

read -r -d '' RESET_SQL <<'SQL' || true
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS availability_slots;
DROP TABLE IF EXISTS provider_date_overrides;
DROP TABLE IF EXISTS provider_weekly_hours;
DROP TABLE IF EXISTS providers;
DROP TABLE IF EXISTS admins;
DROP TABLE IF EXISTS stylists;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS services;
SET FOREIGN_KEY_CHECKS = 1;
SQL

if [[ "$MODE" == "dry-run" ]]; then
    cat <<EOF
Database reset dry run
Host:     $DB_HOST
Port:     $DB_PORT
Database: $DB_NAME
User:     $DB_USER

SQL:
$RESET_SQL
EOF
    exit 0
fi

command -v mysql >/dev/null 2>&1 || fail "mysql CLI is not installed or not on PATH."

echo "Dropping application tables from '$DB_NAME' on $DB_HOST:$DB_PORT as '$DB_USER'..."
mysql --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" --password="$DB_PASSWORD" "$DB_NAME" <<< "$RESET_SQL"
echo "Database reset complete. Start the app to recreate schema and default seed data."
