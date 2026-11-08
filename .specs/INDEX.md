# .specs — índice

Conjunto de especificações (Spec-Driven Development) para modernizar o **Autorizador de Procedimentos Médicos**: backend Java 25 refatorado e alinhado ao protótipo + frontend Angular 21 + PostgreSQL 16, em monorepo. **Só documentação**: nada foi implementado. Formato das specs: `shared/SPEC_TEMPLATE.md`; regras de execução: `shared/como-executar.md`.

## Status do conjunto
- **SPEC-01 (monorepo): concluída** em 2026-11-08 — tag `v0-original` criada, protótipo em `design/prototype/`, `.gitignore` e `README.md` do monorepo atualizados. Spec arquivada em `archive/01-estrutura-monorepo/`.
- **SPEC-07 (módulo procedimento): concluída** em 2026-11-08 — `./mvnw verify` verde (138 testes), replay procedimento 8/8, 0 violações de arquitetura. Arquivada em `archive/SPEC-07-modulo-procedimento.md`.
- SPEC-02 em diante: pendentes. Os projetos `api-autorizador-processo` e `app-autorizador-processo` ainda não existem.
- Observação: a pasta `06-execution-plan/` citada abaixo não existe; a evidência de execução fica em `tarefas.md` de cada spec.

## Leitura obrigatória (nesta ordem)
1. `memory/produto.md` e `memory/contexto-tecnico.md` (resumos)
2. `00-product-discovery/PRODUCT_CONTEXT.md`
3. `01-architecture/TARGET_ARCHITECTURE.md` e `DECISIONS.md`
4. `06-execution-plan/SPEC-EXECUTION-PLAN.md`, `EXECUTION_PLAN.md`, `STATE.md`

## Mapa
| Pasta | Conteúdo |
|---|---|
| `memory/` | resumos de produto e contexto técnico lidos por toda spec |
| `shared/` | template de spec e regras de execução (preexistente) |
| `design/` | `DESIGN_SYSTEM_DISCOVERY.md` (preexistente; os pontos [INFERIDO] foram verificados em `00-…/BASELINE_COMPORTAMENTO_ORIGINAL.md`) e, após SPEC-01, `prototype/` |
| `00-product-discovery/` | `PRODUCT_CONTEXT`, `CURRENT_STATE_ANALYSIS`, `FUNCTIONAL_TRACEABILITY`, `BASELINE_COMPORTAMENTO_ORIGINAL` (executado) |
| `01-architecture/` | `TARGET_ARCHITECTURE`, `MONOREPO_STRUCTURE`, `DATABASE_DESIGN`, `API_CONTRACTS`, `DECISIONS`, SPEC-01 |
| `02-backend/` | SPEC-03..09, 11, 12, **22 (auth JWT)**, 50 (**não aplicável** — sem base de origem; arquivada em `archive/`) |
| `03-frontend/` | SPEC-13..21, **23 (login)** |
| `04-integration/` | SPEC-10, 30, 31, 32 |
| `05-quality/` | `TEST_STRATEGY`, `SECURITY_REQUIREMENTS`, `NON_FUNCTIONAL_REQUIREMENTS`, SPEC-02, 40..43 |
| `06-execution-plan/` | `SPEC-EXECUTION-PLAN` (SPEC-00), `EXECUTION_PLAN`, `STATE` |

## Specs (31)
01 monorepo · 02 baseline golden · 03 backend foundation · 04 banco · 05 contrato OpenAPI · 06 erros/CORS · 07 procedimento · 08 autorizacao · 09 painel · 10 Compose · 11 config/observabilidade · 12 **catálogo estendido (nova feature)** · 22 **autenticação JWT (backend)** · 23 **login (frontend)** · 13 Nginx · 14 Angular foundation · 15 design system · 16 HTTP/tipos · 17 UI procedimentos · 18 UI solicitar · 19 UI autorizações · 20 UI painel · 21 microcopy/a11y · 30 e2e · 31 paridade · 32 contrato · 40 CI por projeto · 41 segurança · 42 desempenho · 43 finalização · 50 migração de dados (condicional).

## Instruções do responsável incorporadas
Specs em `.specs/` seguindo `shared/` · JDK 25.0.2 em `C:\jdk-25.0.2` e Spring Boot parent 4.1.2 · **login por e-mail e senha com JWT** (SPEC-22/23) · **ArchUnit** valida a arquitetura · skills `java-persistence-performance` e `angular-tailwind-ui-specialist` valem para este projeto · nenhum nome "legado" no código do backend · dados do cadastro do protótipo = nova feature de backend (SPEC-12) · backend refatorado para Java 25 refletindo o protótipo · Dockerfile próprio e CI independente por projeto, só com mudança no próprio caminho (SPEC-10, 13, 40).
