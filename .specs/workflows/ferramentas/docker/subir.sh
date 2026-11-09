#!/usr/bin/env bash
# Sobe tudo (PostgreSQL + API + frontend) com um comando. Pode ser chamado de qualquer pasta.
set -euo pipefail
aqui="$(cd "$(dirname "$0")" && pwd)"
cd "$aqui/../../../.."   # raiz do projeto (onde ficam compose.yaml e .env)
bash "$aqui/preparar-ambiente.sh" autorizador-processo "POSTGRES_PORT=5432,API_PORT=8080,APP_PORT=4200"
docker compose up -d --build --wait
docker compose ps
set -a; . ./.env; set +a
echo; echo "Frontend:   http://localhost:${APP_PORT}"; echo "API:        http://localhost:${API_PORT}"; echo "PostgreSQL: localhost:${POSTGRES_PORT}"
