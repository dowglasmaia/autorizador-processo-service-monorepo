# Relatório de desempenho (SPEC-42)

Smoke de desempenho do Autorizador, medido de verdade (sem estimativas) no Compose descartável de `tools/perf/perf.sh`.
Não é teste de carga pesado: serve para detectar regressões grosseiras e validar as metas provisórias NFR-02..04.

## Ambiente
- Máquina de desenvolvimento (Windows 11, Docker 29.9.2 com 16 CPU / 12 GB). **Gerador de carga, API, PostgreSQL e Nginx
  na mesma máquina**, então os números são otimistas em relação a um ambiente com rede e hosts separados.
- API: Spring Boot 4.1.2, Java 25, perfil `prod`. Banco: PostgreSQL 16 (`postgres:16-alpine`), pool padrão. Web: Angular 21,
  build de produção servido pelo Nginx do Compose.
- Compose descartável (projeto `smoke-desempenho`, portas próprias, segredos aleatórios, `down -v` em `trap`).

## Parâmetros
- Dados sintéticos fictícios (`tools/perf/dados.sql`): 1.000 procedimentos e 10.000 autorizações (idempotente).
- Carga (`tools/perf/carga.mjs`): 20 conexões, 60 s por endpoint, 5 s de aquecimento, páginas de 20 itens; um único login com
  o token reutilizado e chamadas direto à porta da API (sem o `limit_req` do login nem o `LimitadorTentativasAdapter`).
- O `POST /autorizacoes` roda por último e acrescenta ~36 mil linhas, sem afetar as leituras já medidas.

## Baseline (antes de qualquer ajuste) — NFR-02

| Endpoint | req | rps | p50 | p95 | p99 | erros |
|---|---|---|---|---|---|---|
| GET /procedimentos | 44866 | 747,6 | 23,8 ms | 48,3 ms | 69,5 ms | 0 |
| GET /autorizacoes | 33831 | 563,6 | 34,1 ms | 54,1 ms | 69,0 ms | 0 |
| GET /painel/resumo | 31228 | 520,2 | 37,1 ms | 54,7 ms | 68,5 ms | 0 |
| POST /autorizacoes | 36027 | 600,3 | 31,8 ms | 48,2 ms | 64,2 ms | 0 |

O `POST` devolve 201 mesmo quando a autorização é negada (`permitido=false`); erro = status ≠ 201.
**NFR-02 atende**: p95 de 48,3 ms (`GET /procedimentos`) e 48,2 ms (`POST /autorizacoes`) contra a meta de 200 ms.
Nenhum ajuste foi necessário para NFR-02.

## SQL por requisição — NFR-03
Contagem pelo log do PostgreSQL (`log_statement=all`, sem `BEGIN/COMMIT`), com 1.000/10.000 registros
(`tools/perf/perf.sh consultas`):

| Rota | size=5 | size=20 | size=100 | size=200 |
|---|---|---|---|---|
| GET /procedimentos — antes | 4 | 4 | 6 | 12 |
| GET /procedimentos — depois | 4 | 4 | 4 | 4 |
| GET /autorizacoes — antes | 5 | 5 | 8 | 11 |
| GET /autorizacoes — depois | 5 | 5 | 5 | 5 |

(“antes” = primeira medição, descontada 1 linha vazia de ruído do log; “depois” = após o ajuste abaixo.)

**Achado e ajuste.** Não havia N+1 por item, mas a contagem **crescia por degraus** com a página: os `@ElementCollection`
`sexos`/`idades` de `ProcedimentoEntity` usavam `@BatchSize(50)`, então cada 50 itens custava mais uma consulta por coleção
(páginas > 50 itens). Como o tamanho máximo de página é 200, o `@BatchSize` passou a **200** nas duas coleções: uma consulta
por coleção por página, em qualquer tamanho permitido. Sem mudança de contrato, de comportamento nem de migration.
Regressão: `ProcedimentoConcorrenciaDesempenhoTest.listagemNaPaginaMaximaMantemAMesmaContagemDeSql` (página de 200 → 4 SQL).
**NFR-03 atende** (constante em todos os tamanhos permitidos). O ajuste não muda a latência de forma relevante (planos abaixo
já são de submilissegundos); o ganho é a constância e menos idas ao banco nas páginas grandes.

## Planos de consulta (`EXPLAIN (ANALYZE, BUFFERS)`, 1.000/10.000 registros)
| Consulta | Plano | Execução |
|---|---|---|
| procedimentos: página (`order by numero_procedimento`) | Index Scan `uq_procedimento_numero`, 3 buffers | 0,11 ms |
| procedimentos: count | Seq Scan (8 buffers) | 0,14 ms |
| procedimentos: prefixo (`cast(... as text) like`) | Seq Scan + Sort (não usa índice) | 0,18 ms |
| autorizações: página (`order by criada_em desc, id desc`) | Seq Scan + top-N heapsort, 80 buffers | 1,19 ms |
| autorizações: count / painel totais | Seq Scan, 74 buffers | 0,76–0,96 ms |
| autorizações: procedimentos em lote | Seq Scan (1.000 linhas) | 0,07 ms |
| painel: cobertura por idade | Seq Scan + HashAggregate (16.000 linhas) | 2,98 ms |
| painel: distribuição de sexos | Seq Scan + GroupAggregate | 1,50 ms |

Todas ficam em poucos milissegundos nesta escala; **nenhum índice novo foi criado** (sem V5): nenhuma meta falhou e o custo
medido não o justifica. Pontos de atenção para escala maior: a página de autorizações ordena por `criada_em` sem índice (Seq Scan
+ top-N) e o filtro por prefixo faz `cast`; ambos viram candidatos a índice se a base crescer ordens de grandeza.

## Web — NFR-04
- **Bundle inicial (build de produção): 542,5 kB bruto, 131,3 kB com gzip nível 6** (soma dos arquivos referenciados por
  `index.html`; o Angular estima 113,86 kB de transferência). Meta ≤ 250 kB gz: **atende**. As páginas são rotas lazy.
- **LCP** (Lighthouse 13.6.0 via `npx`, sem adicionar dependência; perfil móvel padrão com throttling simulado; Chrome local;
  `/login` servida pelo Nginx do Compose, 3 execuções): **0,6–0,7 s** (FCP 0,6–0,7 s, TBT 0–50 ms, CLS 0, nota 100). Meta
  ≤ 2,5 s: **atende**.

## Conclusões
- NFR-02, NFR-03 e NFR-04 atendidos. Único ajuste: `@BatchSize` 50 → 200 (NFR-03 nas páginas grandes). Sem migration V5.
- As metas de `NON_FUNCTIONAL_REQUIREMENTS.md` **não foram alteradas**: a base medida tem grande folga, mas é uma máquina de
  desenvolvimento, sem rede, e não justifica apertar as metas.

## Limitações
- Gerador de carga e servidor na mesma máquina e sem rede; escala de 1.000/10.000 registros; 60 s por endpoint.
- LCP medido só na rota `/login` (a que carrega sem sessão); rotas autenticadas dependem de dados e não foram medidas.
- A contagem de SQL considera apenas `execute/statement` do log; o log foi ativado só no Compose descartável.
- Resultados variam entre execuções; use-os como ordem de grandeza e para detectar regressões, não como SLA.

## Como reproduzir
`tools/perf/perf.sh` (baseline), `tools/perf/perf.sh consultas` (SQL por requisição e `EXPLAIN`), `tools/perf/perf.sh web`
(LCP). Ver `tools/perf/README.md`.
