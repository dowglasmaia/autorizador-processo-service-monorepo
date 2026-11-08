# SPEC-31 — Paridade com o baseline da versão original

| Campo | Valor |
|---|---|
| Domínio / projeto | Integração · `tools/parity` × `api-autorizador-processo` |
| Prioridade | P1 |
| Depende de | SPEC-02, SPEC-07, SPEC-08, SPEC-12 |
| Sucessoras | SPEC-40, SPEC-43 |
| Paralelismo | junto com SPEC-17..19 |

## 1. Objetivo
Provar que a nova API **preserva o comportamento**: reexecutar os casos golden (SPEC-02) contra a nova API e classificar cada resultado como **idêntico**, **divergência aprovada (DIV-xx)** ou **falha**. Prioridade absoluta do produto.
**Restrições:** nenhuma divergência fora de DIV-01..08; ajustar `cases.json` só para refletir DIV aprovadas, nunca para esconder falhas; falha de paridade bloqueia a conclusão.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `00-product-discovery/BASELINE_COMPORTAMENTO_ORIGINAL.md`, `API_CONTRACTS.md` §4, `tools/parity/README.md`

## 3. Tarefas
- [ ] **T1.** Subir a nova API com seed local (`docker compose up -d postgres api` + perfil `local`) e executar `replay.mjs --target v1`.
- [ ] **T2.** Gerar relatório `tools/parity/RELATORIO.md`: tabela caso × original × v1 × status (`IDÊNTICO` / `DIV-xx` / `FALHA`).
- [ ] **T3.** Tratar toda `FALHA`: corrigir a implementação (spec responsável) ou, se for decisão de produto, registrar nova DIV e pedir autorização; repetir até zero falhas.
- [ ] **T4.** Adicionar o replay ao pipeline (SPEC-40) como job `paridade`.

**Aceite:** *Given* a API v1 com seed · *When* `node tools/parity/replay.mjs --target v1 --base-url http://localhost:8080` · *Then* exit 0 e nenhuma linha `FALHA`; B01, B02, B04, B09–B11, B15 `IDÊNTICO` quanto à decisão de negócio; B03, B06, B07, B12–B14 classificados em DIV.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T4 | `nw-bdd-methodology`; Node; Docker; `java-modern-engineering` apenas se houver correção de código |

## 5. Verificação obrigatória
- `node tools/parity/replay.mjs --target v1 --base-url http://localhost:8080 --email "$AUTH_BOOTSTRAP_EMAIL" --senha "$AUTH_BOOTSTRAP_SENHA"` → exit 0
- `tools/parity/RELATORIO.md` sem `FALHA`; todas as DIV citadas existem em `BASELINE_COMPORTAMENTO_ORIGINAL.md`
- Definition of Done: relatório versionado, zero falhas, divergências todas aprovadas.

## 6. Resultado
_Preencher ao concluir._
