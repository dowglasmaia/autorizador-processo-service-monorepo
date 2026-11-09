#!/usr/bin/env bash
# Smoke de desempenho (SPEC-42): sobe um Compose DESCARTÁVEL (postgres + api, projeto/portas/.env próprios, segredos
# aleatórios), carrega os dados sintéticos, roda a carga e derruba tudo com o volume, mesmo em falha.
# Uso: .specs/workflows/ferramentas/perf/perf.sh [dados|carga]   (dados = só carrega e confere contagens; carga = padrão, inclui dados)
# Variáveis: CONEXOES (20), SEGUNDOS (60), AQUECIMENTO (5), SAIDA (arquivo JSON opcional).
# Requisitos: Docker com Compose e Node >= 20. Não toca no .env do desenvolvedor.
set -euo pipefail
MODO="${1:-carga}"
RAIZ="$(cd "$(dirname "$0")/../../../.." && pwd)"
TMP="$(mktemp -d)"
PROJETO="smoke-desempenho"
API_PORT=18091
PG_PORT=15441
aleatorio() { node -e "console.log(require('crypto').randomBytes($1).toString('$2'))"; }
SENHA_ADMIN="$(aleatorio 12 base64url)"
EMAIL_ADMIN="perf@exemplo.com"
cat > "$TMP/.env" <<ENV
POSTGRES_PASSWORD=$(aleatorio 12 hex)
POSTGRES_PORT=$PG_PORT
API_PORT=$API_PORT
JWT_SECRET=$(aleatorio 48 base64url)
AUTH_BOOTSTRAP_EMAIL=$EMAIL_ADMIN
AUTH_BOOTSTRAP_SENHA=$SENHA_ADMIN
SPRING_PROFILES_ACTIVE=prod
ENV
if docker compose version >/dev/null 2>&1; then DC=(docker compose); else DC=(docker-compose); fi
compose() { "${DC[@]}" --project-directory "$RAIZ" --env-file "$TMP/.env" -p "$PROJETO" "$@"; }
limpar() { compose down -v --remove-orphans >/dev/null 2>&1 || true; rm -rf "$TMP"; }
trap limpar EXIT
psql() { compose exec -T postgres psql -U autorizador -d autorizador -v ON_ERROR_STOP=1 "$@"; }

if [ "$MODO" = "web" ]; then
  # NFR-04: Lighthouse (npx, sem dependência no projeto) em perfil móvel simulado contra o Nginx do Compose.
  APP_PORT=18092; echo "APP_PORT=$APP_PORT" >> "$TMP/.env"
  compose up -d --build --wait --wait-timeout 300 postgres api app
  CHROME_PATH="${CHROME_PATH:-/c/Program Files/Google/Chrome/Application/chrome.exe}"; export CHROME_PATH
  for i in 1 2 3; do
    npx --yes lighthouse "http://localhost:$APP_PORT/login" --only-categories=performance --form-factor=mobile       --chrome-flags="--headless=new --no-sandbox" --output=json --output-path="$TMP/lh$i.json" --quiet >/dev/null
    node -e "const r=require('$(cygpath -m "$TMP")/lh$i.json');const a=r.audits;console.log('execução $i: LCP',a['largest-contentful-paint'].displayValue,'| FCP',a['first-contentful-paint'].displayValue,'| TBT',a['total-blocking-time'].displayValue,'| CLS',a['cumulative-layout-shift'].displayValue,'| nota',Math.round(r.categories.performance.score*100))"
  done
  exit 0
fi
compose up -d --build --wait --wait-timeout 300 postgres api
psql < "$RAIZ/.specs/workflows/ferramentas/perf/dados.sql" >/dev/null
echo "Contagens (procedimentos | autorizações):"
psql -At -c "select (select count(*) from procedimento), (select count(*) from autorizacao)"
# Reexecutar o script não duplica:
psql < "$RAIZ/.specs/workflows/ferramentas/perf/dados.sql" >/dev/null
psql -At -c "select 'após reexecução: ' || (select count(*) from procedimento) || ' | ' || (select count(*) from autorizacao)"
[ "$MODO" = "dados" ] && exit 0

if [ "$MODO" = "consultas" ]; then
  # NFR-03: conta o SQL por requisição pelo log do PostgreSQL (log_statement=all, sem BEGIN/COMMIT) e imprime os SQL.
  psql -At -c "alter system set log_statement='all'" >/dev/null
  psql -At -c "select pg_reload_conf()" >/dev/null
  TOKEN="$(curl -fsS -X POST "http://localhost:$API_PORT/api/v1/auth/login" -H 'Content-Type: application/json'     -d "{\"email\":\"$EMAIL_ADMIN\",\"senha\":\"$SENHA_ADMIN\"}" | node -e "process.stdin.on('data',d=>console.log(JSON.parse(d).accessToken))")"
  for rota in procedimentos autorizacoes; do
    for tam in 5 20 100 200; do
      curl -fsS -o /dev/null -H "Authorization: Bearer $TOKEN" "http://localhost:$API_PORT/api/v1/$rota?page=1&size=$tam" # aquece
      sleep 1; MARCA="$(date -u +%Y-%m-%dT%H:%M:%S)"; sleep 1
      curl -fsS -o /dev/null -H "Authorization: Bearer $TOKEN" "http://localhost:$API_PORT/api/v1/$rota?page=1&size=$tam"
      sleep 1
      N="$(compose logs --no-log-prefix --since "$MARCA" postgres 2>&1 | grep -E 'LOG:  (execute|statement)' | grep -vE 'statement: *$' | grep -viE ': (BEGIN|COMMIT|ROLLBACK|SET |SHOW )' | tee "$TMP/sql-$rota-$tam.txt" | wc -l)"
      echo "SQL por requisição: GET /$rota size=$tam -> $N"
    done
  done
  echo "--- SQL emitido (GET /procedimentos e /autorizacoes, size=20) ---"
  cat "$TMP/sql-procedimentos-20.txt" "$TMP/sql-autorizacoes-20.txt" | sed 's/^.*LOG:  //'
  echo "--- EXPLAIN (ANALYZE, BUFFERS) ---"
  psql -f - <<'SQL'
\echo '== procedimentos: página (order by numero_procedimento limit 20)'
explain (analyze, buffers) select p.* from procedimento p order by p.numero_procedimento limit 20 offset 20;
\echo '== procedimentos: count'
explain (analyze, buffers) select count(*) from procedimento;
\echo '== procedimentos: filtro por prefixo'
explain (analyze, buffers) select p.* from procedimento p where cast(p.numero_procedimento as text) like '12%' order by p.numero_procedimento limit 20;
\echo '== autorizacoes: página (order by criada_em desc limit 20)'
explain (analyze, buffers) select a.* from autorizacao a order by a.criada_em desc, a.id desc limit 20 offset 20;
\echo '== autorizacoes: count'
explain (analyze, buffers) select count(*) from autorizacao;
\echo '== autorizacoes: procedimentos em lote'
explain (analyze, buffers) select * from procedimento where id in (1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20);
\echo '== painel: cobertura por idade'
explain (analyze, buffers) select idade, count(*) from procedimento_idade group by idade order by idade;
\echo '== painel: distribuição de sexos'
explain (analyze, buffers) select sexo, count(distinct procedimento_id) from procedimento_sexo group by sexo;
\echo '== painel: totais'
explain (analyze, buffers) select count(*) from autorizacao;
SQL
  exit 0
fi

node "$RAIZ/.specs/workflows/ferramentas/perf/carga.mjs" --api "http://localhost:$API_PORT" --email "$EMAIL_ADMIN" --senha "$SENHA_ADMIN" \
  --conexoes "${CONEXOES:-20}" --segundos "${SEGUNDOS:-60}" --aquecimento "${AQUECIMENTO:-5}" ${SAIDA:+--saida "$SAIDA"}
