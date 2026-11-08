# SPEC-20 — Funcionalidade Painel

| Campo | Valor |
|---|---|
| Domínio / projeto | Frontend · `features/painel` |
| Prioridade | P2 |
| Depende de | SPEC-15, SPEC-16, SPEC-23, SPEC-09, SPEC-18 (contadores de sessão) |
| Sucessoras | SPEC-21, SPEC-30 |
| Paralelismo | nenhum com SPEC-21 |

## 1. Objetivo
Tela inicial do protótipo alimentada pela API real (`obterResumoPainel`): cabeçalho com data por extenso (pt-BR), hero "Autorize um procedimento em segundos" + "Nova solicitação", 4 KPIs (**Procedimentos**, **Autorizações** concedidas, **Idades cobertas**, **Aprovação** — percentual da **sessão atual**: concedidas ÷ (concedidas + negadas) via `SessaoSolicitacoes`; "—" sem tentativas; legenda "solicitações desta sessão"), **Cobertura por idade** (barras horizontais), **Sexos aceitos** (rosca M × F com legenda e total), **Últimas autorizações** (até 4, "Ver todas →") e estado vazio "Nenhuma autorização concedida ainda."
**Restrições:** valores vêm do servidor (sem recomputar a partir de listas); KPI "Aprovação" **não** é dado persistido (PEN-01) e deve indicar claramente que é da sessão; gráficos acessíveis (equivalente textual/tabela oculta, não só cor; cores com contraste AA); barras/rosca sem biblioteca de gráficos (CSS/SVG simples) salvo necessidade justificada.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** protótipo (tela Painel, `renderVals`), `API_CONTRACTS.md` (`ResumoPainel`), `FUNCTIONAL_TRACEABILITY.md` F12–F13; skills `angular-module-scaffold`, `dataviz` (cores/legibilidade de gráficos)

## 3. Tarefas
- [ ] **T1.** Rotas lazy (`/painel` padrão), `doc.md`, `painel.service.ts` (`obterResumoPainel`).
- [ ] **T2.** Componentes de apresentação: `KpiCard`, `BarraCobertura`, `RoscaSexos` (SVG/conic-gradient com `role="img"` e descrição), `UltimasAutorizacoes`.
- [ ] **T3.** KPI de sessão com `SessaoSolicitacoes`; testes de cálculo (0/0 → "—", 1/1 → 100%, 1/3 → 33%).
- [ ] **T4.** Estados: esqueleto, erro com nova tentativa, vazio por seção.
- [ ] **T5.** Responsividade (KPIs `auto-fit`, gráficos empilhados < md) e `prefers-reduced-motion`.
- [ ] **T6.** Testes de componente e serviço.

**Aceite:** *Given* seed e nenhuma autorização · *When* abrir `/painel` · *Then* KPIs 3 / 0 / 3 / "—", barras 10→2, 20→2, 30→1, rosca M=2 F=3 e mensagem de vazio. *Given* uma autorização concedida na sessão · *Then* "Aprovação" 100%.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T6 | `angular-module-scaffold`, `angular-architect` |
| T2, T5 | `angular-tailwind-ui-specialist`, `ui-ux-pro-max`, `dataviz` |
| textos | `copywriting` |

## 5. Verificação obrigatória
- `npm run build && npm test && npm run lint` → verdes
- Verificação contra Compose com seed; capturas 320/768/1280 px comparadas ao protótipo
- Definition of Done: `obterResumoPainel` coberta; KPI de sessão rotulado; gráficos com alternativa textual.

## 6. Resultado
_Preencher ao concluir._
