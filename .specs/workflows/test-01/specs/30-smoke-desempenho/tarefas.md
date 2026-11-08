# Tarefas

## 1. Preparação
- [ ] 1.1 Ler `SPEC-42`, `NON_FUNCTIONAL_REQUIREMENTS.md` (NFR-02..04), `DATABASE_DESIGN.md` §3 e as referências de `java-persistence-performance`; registrar o desvio da pasta `06-execution-plan` inexistente
- [ ] 1.2 Conferir o que a etapa 29 entregou (limites de corpo, `limit_req`, limitador de login) e definir como a carga evita acioná-los; confirmar Docker disponível
- [ ] 1.3 Criar `tools/perf/` com README curto (pré-requisitos, `.env` descartável, comandos e limpeza)

## 2. Dados sintéticos
- [ ] 2.1 Escrever script idempotente que gera 1.000 procedimentos e 10.000 autorizações fictícios no PostgreSQL do Compose
- [ ] 2.2 Subir `postgres` e `api` com `.env` descartável, carregar os dados e confirmar as contagens por consulta

## 3. Carga HTTP
- [ ] 3.1 Criar o executor de carga (20 conexões, 60 s, aquecimento) que faz um login e reutiliza o token, medindo p50/p95/p99, throughput e taxa de erro
- [ ] 3.2 Executar e registrar o **baseline** de `GET /procedimentos`, `GET /autorizacoes`, `GET /painel/resumo` e `POST /autorizacoes`
- [ ] 3.3 Comparar os resultados com NFR-02 e anotar quais metas passam ou falham

## 4. Consultas
- [ ] 4.1 Contar o SQL por requisição em `GET /procedimentos` e `GET /autorizacoes` com tamanhos de página diferentes e confirmar contagem constante (NFR-03)
- [ ] 4.2 Rodar `EXPLAIN (ANALYZE, BUFFERS)` das consultas do painel e das listas e registrar os planos relevantes

## 5. Web
- [ ] 5.1 Medir o tamanho gzip do bundle inicial do build de produção contra 250 kB (NFR-04)
- [ ] 5.2 Medir o LCP em perfil móvel simulado servido pelo Nginx do Compose, ou registrar a limitação da ferramenta

## 6. Ajustes (somente se alguma meta falhar)
- [ ] 6.1 Diagnosticar a causa de cada meta falha e propor o menor ajuste, com `EXPLAIN` ou medição que o sustente
- [ ] 6.2 Aplicar o ajuste (migration V5 nova, `@BatchSize`/fetch, lazy route ou tamanho de página) com teste de regressão, sem mudar comportamento nem contrato
- [ ] 6.3 Remedir e registrar antes e depois; se nenhuma meta falhou, registrar que não houve ajuste

## 7. Relatório e verificação
- [ ] 7.1 Escrever `.specs/05-quality/PERFORMANCE_REPORT.md` (ambiente, parâmetros, baseline, resultados, conclusões e limitações) e ajustar as metas em `NON_FUNCTIONAL_REQUIREMENTS.md` só se a base medida justificar
- [ ] 7.2 Derrubar o Compose com `down -v`, confirmar que não restam processos nem contêineres e que nenhum segredo foi versionado
- [ ] 7.3 Rodar `./mvnw verify` em `api-autorizador-processo` e `npm run lint`, `npm run build` e `npm test` em `app-autorizador-processo`, corrigindo falhas
