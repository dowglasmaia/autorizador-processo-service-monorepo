# SPEC-18 — Funcionalidade Solicitar autorização

| Campo | Valor |
|---|---|
| Domínio / projeto | Frontend · `features/solicitar` |
| Prioridade | P1 |
| Depende de | SPEC-15, SPEC-16, SPEC-23, SPEC-08 (e SPEC-07 para o seletor) |
| Sucessoras | SPEC-20, SPEC-21, SPEC-30 |
| Paralelismo | junto com SPEC-17, SPEC-19 |

## 1. Objetivo
Implementar o fluxo central do produto conforme a tela do protótipo: escolher procedimento, informar idade e sexo, solicitar e ver o resultado ao lado — **Autorizado** (cartão verde, texto "Procedimento n liberado para paciente masculino/feminino de N anos", link "Ver autorizações concedidas") ou **Negado** (cartão coral, mensagem do backend por `code`, nota "A API não informa se falhou o sexo ou a idade" reescrita para "A regra não informa qual critério falhou"), e o estado inicial "O resultado aparece aqui.". Botões **Solicitar autorização** e **Limpar**.
**Regras de UI:** o `403 AUTORIZACAO_NEGADA` é **resultado**, não erro (SPEC-16 T5); `404 PROCEDIMENTO_NAO_ENCONTRADO` mostra erro no seletor e recarrega a lista; `VALIDACAO_INVALIDA` junto aos campos; `?numero=` pré-seleciona o procedimento; seletor "Nº n" (+ nome quando existir) carregado por `GET /procedimentos?size=200&sort=numProcedimento,asc`; resultado anunciado por `aria-live="polite"`; **contadores da sessão** (concedidas/negadas) mantidos em memória para o KPI "Aprovação" do painel (ADR-11), sem persistir.
**Restrições:** nenhuma decisão de elegibilidade no cliente; dados do paciente não são armazenados nem logados no cliente; sexo enviado como `M`/`F`.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** protótipo (tela Solicitar, `submitReq`), `API_CONTRACTS.md` (autorizacao), baseline B09–B12, `FUNCTIONAL_TRACEABILITY.md` F08–F10, F13, F15; skill `angular-module-scaffold`

## 3. Tarefas
- [ ] **T1.** Rotas lazy, `doc.md`, serviço `autorizacao.service.ts` (`solicitarAutorizacao`) retornando `{resultado:'autorizado'|'negado', autorizacao?, detail?}`.
- [ ] **T2.** Serviço de sessão `SessaoSolicitacoes` (signals `concedidas`, `negadas`) em `core`, injetado pelo painel (SPEC-20).
- [ ] **T3.** Página com formulário tipado: procedimento (obrigatório), idade (inteiro 0..150), sexo (`SegmentedChoice` M/F com `aria-pressed`); validação e mensagens por campo.
- [ ] **T4.** Painel de resultado (3 estados) com ícones, animação curta (respeita `prefers-reduced-motion`) e `aria-live`.
- [ ] **T5.** Tratamento de `PROCEDIMENTO_NAO_ENCONTRADO`, rede e servidor (modal com nova tentativa; formulário preservado).
- [ ] **T6.** Testes: autorizado, negado, inexistente, validação, rede, `?numero=`, contadores de sessão.

**Aceite (G/W/T):** *Given* seed · *When* solicitar 4567/20/F · *Then* cartão "Autorizado" e link ao histórico. *When* 1234/30/M · *Then* cartão "Negado" sem toast de erro. *When* procedimento removido entre a carga e o envio · *Then* erro tratado e lista recarregada.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T6 | `angular-module-scaffold`, `angular-architect` |
| T3–T4 | `angular-tailwind-ui-specialist`, `ui-ux-pro-max` |
| textos de resultado | `copywriting` |

## 5. Verificação obrigatória
- `npm run build && npm test && npm run lint` → verdes
- Fluxo manual/automatizado contra Compose: autorizado, negado, inexistente
- Definition of Done: operação `solicitarAutorizacao` coberta; negativa tratada como resultado; acessibilidade do resultado verificada.

## 6. Resultado
_Preencher ao concluir._
