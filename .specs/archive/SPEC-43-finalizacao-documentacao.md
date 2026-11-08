# SPEC-43 — Finalização, documentação e aceite global

| Campo | Valor |
|---|---|
| Domínio / projeto | Qualidade · raiz do monorepo |
| Prioridade | P3 (última) |
| Depende de | SPEC-21, SPEC-30, SPEC-31, SPEC-40, SPEC-41, SPEC-42 |
| Sucessoras | — |
| Paralelismo | nenhum |

## 1. Objetivo
Encerrar a modernização: validação global a partir de checkout limpo, documentação coerente com o resultado, limpeza de resíduos e arquivamento das specs concluídas.
**Restrições:** sem funcionalidades novas; nenhum "legado/legacy" no backend; pendências de produto (PEN-xx) continuam registradas, não resolvidas aqui.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `06-execution-plan/STATE.md` (todas as evidências), `DECISIONS.md`

## 3. Tarefas
- [ ] **T1.** Checkout limpo + `docker compose up -d --build` + `npm run e2e` + `node tools/parity/replay.mjs --target v1` + `./mvnw verify` + `npm test`: tudo verde na mesma sessão.
- [ ] **T2.** Varreduras: `git grep -il "legado\|legacy" -- api-autorizador-processo` vazio; sem credenciais; sem `TODO(domain)` pendente; sem dependências não utilizadas.
- [ ] **T3.** `README.md` raiz, do backend e do frontend atualizados (stack real, comandos validados, endpoints, variáveis, riscos aceitos); `.specs/design/DESIGN_SYSTEM_DISCOVERY.md` recebe nota de que o baseline executado o substitui nos pontos [INFERIDO].
- [ ] **T4.** Conferir matriz `FUNCTIONAL_TRACEABILITY.md`: todo F-item com spec concluída ou fora de escopo declarado.
- [ ] **T5.** Preencher "Resultado" de cada spec concluída e movê-las para `.specs/archive/` com `git mv` (regra de `como-executar.md` §4).
- [ ] **T6.** Registrar relatório final e pendências abertas em `STATE.md`.

**Aceite:** todas as verificações de T1 executadas com sucesso na mesma sessão; `STATE.md` sem spec pendente (exceto SPEC-50 condicional, se não aplicável).

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T6 | `java-modern-engineering` e `angular-architect` (revisão final); `copywriting` (READMEs) |

## 5. Verificação obrigatória
- Execução de todos os comandos de T1 com resultado registrado
- Definition of Done: aceite global verde, documentação coerente, specs arquivadas.

## 6. Resultado
Concluída em 2026-11-08 pela etapa 31 do loop `test-01`. Verificação global em checkout limpo (backend 247 testes + ArchUnit 23/23; frontend lint, build e 226 testes; Compose saudável; e2e 30/30; paridade 23/23; contratos ok), varreduras limpas, READMEs e `INDEX.md` atualizados, specs arquivadas, `FUNCTIONAL_TRACEABILITY.md` sem F-item órfão (F16, F22 e F23 fora de escopo declarado; PEN-xx abertas). Relatório: `.specs/05-quality/RELATORIO_FINAL.md`. Desvios: `06-execution-plan/` inexistente (relatório no lugar de `STATE.md`), `docker-compose` no lugar do plugin, portas alternativas, `.gitattributes` como correção mínima.
