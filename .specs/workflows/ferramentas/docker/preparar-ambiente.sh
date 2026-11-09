#!/usr/bin/env bash
# Uso: preparar-ambiente.sh <projeto-compose> "POSTGRES_PORT=5432,API_PORT=8080"
# Cria o .env a partir do .env.example e troca por portas livres as que estiverem ocupadas
# por outros processos/contêineres (portas de contêineres deste projeto não contam).
set -euo pipefail
projeto="${1:-}"; portas="${2:?informe VAR=padrão,...}"; env=.env; exemplo=.env.example
if [ ! -f "$env" ]; then
  [ -f "$exemplo" ] || { echo "falta $exemplo" >&2; exit 1; }
  cp "$exemplo" "$env"; echo "Criado $env"
fi

# portas publicadas pelos contêineres DESTE projeto (não são conflito: o compose as reutiliza)
proprias=""
if [ -n "$projeto" ] && command -v docker >/dev/null 2>&1; then
  proprias=$(docker compose -p "$projeto" ps --format '{{.Ports}}' 2>/dev/null || true)
fi
em_uso() {
  local p=$1
  if [ -n "$proprias" ] && echo "$proprias" | grep -qE ":${p}->"; then return 1; fi
  (ss -ltn 2>/dev/null || netstat -ltn 2>/dev/null) | grep -qE "[:.]${p}[[:space:]]" && return 0
  docker ps --format '{{.Ports}}' 2>/dev/null | grep -qE ":${p}->"
}

IFS=',' read -ra pares <<< "$portas"
for par in "${pares[@]}"; do
  nome="${par%%=*}"; padrao="${par#*=}"
  atual=$(grep -E "^${nome}=" "$env" | head -1 | cut -d= -f2 || true); atual="${atual:-$padrao}"
  porta=$atual
  while em_uso "$porta"; do porta=$((porta+1)); done
  if [ "$porta" != "$atual" ]; then echo "$nome=$atual ocupada -> usando $porta"; fi
  if grep -qE "^${nome}=" "$env"; then
    sed -i.bak -E "s|^${nome}=.*|${nome}=${porta}|" "$env" && rm -f "$env.bak"
  else
    echo "${nome}=${porta}" >> "$env"
  fi
done
grep -E '_PORT=' "$env" | sed 's/^/  /'
