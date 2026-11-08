#!/usr/bin/env bash
# Sondagens ativas de segurança (SPEC-41): sobe um Compose DESCARTÁVEL (projeto, portas e .env próprios, segredos
# aleatórios), roda sondagem.mjs e derruba tudo (inclusive o volume) ao terminar, mesmo em caso de falha.
# Não toca no .env do desenvolvedor nem nos contêineres do projeto "autorizador-processo".
# Requisitos: Docker com Compose (plugin `docker compose` ou `docker-compose`) e Node >= 20.
set -euo pipefail
RAIZ="$(cd "$(dirname "$0")/../.." && pwd)"
TMP="$(mktemp -d)"
PROJETO="sondagem-seguranca"
API_PORT=18090
APP_PORT=14210
PG_PORT=15440
aleatorio() { node -e "console.log(require('crypto').randomBytes($1).toString('$2'))"; }
SEGREDO="$(aleatorio 48 base64url)"
SENHA_ADMIN="$(aleatorio 12 base64url)"
EMAIL_ADMIN="sondagem@exemplo.com"
EMAIL_INATIVO="inativo@exemplo.com"

cat > "$TMP/.env" <<ENV
POSTGRES_PASSWORD=$(aleatorio 12 hex)
POSTGRES_PORT=$PG_PORT
API_PORT=$API_PORT
APP_PORT=$APP_PORT
JWT_SECRET=$SEGREDO
AUTH_BOOTSTRAP_EMAIL=$EMAIL_ADMIN
AUTH_BOOTSTRAP_SENHA=$SENHA_ADMIN
CORS_ALLOWED_ORIGINS=http://localhost:4200
# Perfil restritivo: sem seed, sem Swagger e health sem detalhes (o que vale fora da máquina local).
SPRING_PROFILES_ACTIVE=prod
ENV

if docker compose version >/dev/null 2>&1; then DC=(docker compose); else DC=(docker-compose); fi
compose() { "${DC[@]}" --project-directory "$RAIZ" --env-file "$TMP/.env" -p "$PROJETO" "$@"; }
limpar() { compose down -v --remove-orphans >/dev/null 2>&1 || true; rm -rf "$TMP"; }
trap limpar EXIT

compose up -d --build --wait --wait-timeout 300
# Usuário inativo: copia o hash do bootstrap e desativa (a API não tem cadastro de usuários).
compose exec -T postgres psql -U autorizador -d autorizador -v ON_ERROR_STOP=1 -c \
  "insert into usuario (email, senha_hash, ativo) select '$EMAIL_INATIVO', senha_hash, false from usuario where email = '$EMAIL_ADMIN'" >/dev/null

node "$RAIZ/tools/security/sondagem.mjs" \
  --api "http://localhost:$API_PORT" --front "http://localhost:$APP_PORT" \
  --email "$EMAIL_ADMIN" --senha "$SENHA_ADMIN" --jwt-secret "$SEGREDO" --email-inativo "$EMAIL_INATIVO"
