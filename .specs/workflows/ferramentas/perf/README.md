# Smoke de desempenho (SPEC-42)

Mede a API no Compose contra o NFR-02 (p95 ≤ 200 ms com 1.000 procedimentos, 10.000 autorizações e 20 conexões).

- **Pré-requisitos:** Docker com Compose e Node ≥ 20 (nada é instalado).
- **Dados + contagens:** `tools/perf/perf.sh dados`
- **Carga completa (baseline):** `SAIDA=baseline.json tools/perf/perf.sh` (variáveis `CONEXOES`, `SEGUNDOS`, `AQUECIMENTO`).
- **SQL por requisição + EXPLAIN (NFR-03):** `tools/perf/perf.sh consultas`.
- **Web (NFR-04, Lighthouse via `npx`, exige Chrome; `CHROME_PATH`):** `tools/perf/perf.sh web`.
- **Limites de segurança:** a carga faz um único login e reutiliza o token, e fala direto com a porta da API (sem o
  Nginx), para não acionar o `limit_req` do login nem o `LimitadorTentativasAdapter`.
- **Limpeza:** automática (`trap`) com `down -v`, mesmo em falha. Projeto Compose `smoke-desempenho`, portas 18091/15441,
  `.env` temporário com segredos aleatórios. Conferir: `docker ps -a --filter name=smoke-desempenho`.
