# Tarefas

## 1. Preparação
- [x] 1.1 Ler `SPEC-42`, `NON_FUNCTIONAL_REQUIREMENTS.md` (NFR-02..04), `DATABASE_DESIGN.md` §3 e as referências de `java-persistence-performance`; registrar o desvio da pasta `06-execution-plan` inexistente
- [x] 1.2 Conferir o que a etapa 29 entregou (`limites.conf.template`, `limit_req` do login, `LimitadorTentativasAdapter`) e definir como a carga evita acioná-los; confirmar Docker disponível
- [x] 1.3 Criar `tools/perf/` (padrão de `tools/security/sondagem.sh`: Compose descartável com `trap` de limpeza) com README curto: pré-requisitos, comandos e limpeza

## 2. Dados sintéticos
- [x] 2.1 Escrever script idempotente que gera 1.000 procedimentos e 10.000 autorizações fictícios no PostgreSQL do Compose
- [x] 2.2 Subir `postgres` e `api` com `.env` descartável, carregar os dados e confirmar as contagens por consulta

## 3. Carga HTTP
- [x] 3.1 Criar o executor de carga (20 conexões, 60 s, aquecimento) que faz um login e reutiliza o token, medindo p50/p95/p99, throughput e taxa de erro
- [x] 3.2 Executar e registrar o **baseline** de `GET /procedimentos`, `GET /autorizacoes`, `GET /painel/resumo` e `POST /autorizacoes`
- [x] 3.3 Comparar os resultados com NFR-02 e anotar quais metas passam ou falham

## 4. Consultas
- [x] 4.1 Contar o SQL por requisição em `GET /procedimentos` e `GET /autorizacoes` com tamanhos de página diferentes e confirmar contagem constante (NFR-03)
- [x] 4.2 Rodar `EXPLAIN (ANALYZE, BUFFERS)` das consultas do painel e das listas e registrar os planos relevantes

## 5. Web
- [x] 5.1 Medir o tamanho gzip do bundle inicial do build de produção contra 250 kB (NFR-04)
- [x] 5.2 Medir o LCP em perfil móvel simulado servido pelo Nginx do Compose, ou registrar a limitação da ferramenta

## 6. Ajustes (somente se alguma meta falhar)
- [x] 6.1 Diagnosticar a causa de cada meta falha e propor o menor ajuste, com `EXPLAIN` ou medição que o sustente
- [x] 6.2 Aplicar o ajuste (migration V5 nova, `@BatchSize`/fetch, lazy route ou tamanho de página) com teste de regressão, sem mudar comportamento nem contrato
- [x] 6.3 Remedir e registrar antes e depois; se nenhuma meta falhou, registrar que não houve ajuste

## 7. Relatório e verificação
- [x] 7.1 Escrever `.specs/05-quality/PERFORMANCE_REPORT.md` (ambiente, parâmetros, baseline, resultados, conclusões e limitações) e ajustar as metas em `NON_FUNCTIONAL_REQUIREMENTS.md` só se a base medida justificar
- [x] 7.2 Derrubar o Compose com `down -v`, confirmar que não restam processos nem contêineres e que nenhum segredo foi versionado
- [x] 7.3 Rodar `./mvnw verify` em `api-autorizador-processo` e `npm run lint`, `npm run build` e `npm test` em `app-autorizador-processo`, corrigindo falhas
