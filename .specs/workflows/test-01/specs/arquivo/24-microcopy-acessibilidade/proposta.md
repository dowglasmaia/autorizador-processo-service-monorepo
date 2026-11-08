# Microcopy e acessibilidade

Etapa 24 de 31 · Fase 3 · derivada da `SPEC-21` (`.specs/03-frontend/SPEC-21-microcopy-acessibilidade.md`). Escrita antecipadamente e revisada contra o código entregue até a etapa 23 (painel já implementado).

## Objetivo
Passe final de qualidade sobre todas as telas do frontend (`/login`, `/painel`, `/procedimentos`, `/procedimentos/novo`, `/solicitar`, `/autorizacoes`): catálogo único de mensagens em pt-BR, revisão de acessibilidade (WCAG, nível AA), navegação por teclado, foco, contraste, estados vazios e de erro. **Sem novas funcionalidades**, sem mudança de regras nem de contrato; alterações restritas à camada de apresentação.

## Ponto de partida (conferido no código após a etapa 23)
- O painel já existe (`features/painel`, com `barra-cobertura`, `rosca-sexos`, `ultimas-autorizacoes`) e já usa `role="status"`/`aria-label`; falta só auditar gráficos (alternativa textual) e migrar seus textos.
- O shell já exibe o estado da API via `ApiStatusService` (`role="status"`), sem "perfil test"; resta conferir o texto e migrá-lo ao catálogo.
- Não existe `core/i18n/mensagens.ts`. Há só `features/login/mensagens-login.ts`; os demais textos estão soltos nos templates e componentes.
- `src/index.html` está com `<html lang="en">`; precisa ser `pt-BR`.
- Títulos de página por rota já existem (`'… · Autorizador'`) em todas as rotas.
- Já há `aria-live`/`role="alert"` em `toast-host`, `loading-overlay`, login, procedimentos, procedimento-novo, solicitar e autorizações (a 23 deve acrescentar o painel).
- `shared/ui` tem `dialog`, `chip`, `field`, `data-table`, `empty-state` etc.; foco preso/retorno do diálogo e teclado de chips/etapas ainda não foram auditados.
- Nenhuma biblioteca de a11y (axe) está no `package.json`; testes usam Vitest via `ng test`. Os grep proibidos (`perfil test`, `ná Cadastrado`, `Integridades`) já retornam vazio em `src`; o grep já está vazio, inclusive no shell.
- `.specs/06-execution-plan/` não existe; desvio a registrar na evidência.

## Escopo
1. Catálogo tipado `core/i18n/mensagens.ts` (títulos, ajudas, erros por `code`, vazios, toasts) usado por todas as telas; migrar `mensagens-login.ts` e remover textos soltos.
2. Revisão de cada mensagem: voz consistente, ação clara, sem jargão técnico ("API", "perfil test", códigos internos); grafia correta ("Nº", sem "Integridades"/"ná Cadastrado"). Rodapé do shell mostra estado genérico da API.
3. Auditoria a11y por tela: rótulos, ordem de foco, `aria-live`, contraste, alvos de toque ≥ 44 px, teclado em chips/etapas/diálogo (foco preso e retorno), landmarks, `lang="pt-BR"`, título por rota, incluindo o painel e seus gráficos (alternativa textual).
4. Auditoria responsiva em 320/768/1280 px e zoom 200 %; corrigir rolagem horizontal.
5. Teste automatizado de a11y nas 6 rotas, registrando violações restantes com justificativa.
6. Atualizar `doc.md` dos módulos afetados.

## Fora do escopo
- Novas funcionalidades, regras, endpoints ou alterações de contrato/backend.
- Novas dependências (a SPEC-21 proíbe); e2e e comparação visual contra o Compose (SPEC-30); textos de marketing.
- Conteúdo da SPEC-43 (sucessora).

## Critérios de aceitação
- *Given* as 6 rotas · *When* auditoria automatizada · *Then* 0 violações críticas/sérias.
- *Given* apenas teclado · *Then* cadastrar procedimento e solicitar autorização são concluíveis, com foco visível, diálogo com foco preso e retorno.
- `grep -rn "perfil test\|ná Cadastrado\|Integridades" app-autorizador-processo/src` vazio.
- Nenhum texto de UI solto: telas consomem o catálogo.
- `npm run lint`, `npm run build` e `npm test` verdes; sem `any`.

## Decisões tomadas sem consulta
- **Revisão (etapa 24):** painel e estado da API no shell já entregues pela etapa 23; a tarefa de rodapé virou conferência do texto. Nenhum requisito novo.
- **Nível WCAG:** o plano diz 2.1 AA e a SPEC-21 diz 2.2 AA; adotado **2.2 AA** (inclui o 2.1) e alvo de toque ≥ 44 px, como a spec. [REVISAR]
- **Teste automatizado de a11y sem nova dependência:** o `axe-core` não está instalado e este loop não autoriza instalar. Implementar verificações com Vitest/DOM (nomes acessíveis, `lang`, landmarks, rótulos, ordem de foco, `alt`/`aria-label`, contraste dos tokens calculado). Se o `axe-core` for aceito, é decisão humana. [REVISAR]
- **Catálogo:** objeto `as const` simples em TypeScript, sem biblioteca de i18n e sem `LOCALE_ID`; apenas pt-BR.
- **Erros por `code`:** o catálogo mapeia os `code` já existentes do contrato; código desconhecido cai em mensagem genérica acionável.
- **Capturas 320/768/1280:** feitas se houver navegador disponível; caso contrário, auditoria por CSS/classes e testes, registrando a limitação. [REVISAR]
- **Contraste:** corrigido nos tokens do design system só quando a medição falhar, preservando a identidade visual do protótipo.

- **Lote 1 (catálogo + revisão + lang):** catálogo em `core/i18n/mensagens.ts` (objeto `as const` + `mensagemDoErro`, com `satisfies Record<ProblemCode,string>`); `mapProblem` usa `problem.detail` do servidor e, na falta, a mensagem do catálogo por `code`. Rodapé do shell passou de "API: disponível" para "Sistema: disponível" (sem jargão). `<title>` do `index.html` trocado para "Autorizador". Textos de rótulos de enum (`rotulos.ts`, sexos, regras de formulário/idades e o resumo do cadastro) permanecem fora do catálogo neste lote — ficam para revisão posterior. [REVISAR]

## Evidência do lote 1
- 1.1 Inventário: shell (nav, menu, landmarks, status), login, painel (+3 componentes), procedimentos, procedimento-novo (cabeçalho, etapas, botões, erros), solicitar, autorizações, 404, diálogo de erro — todos migrados. Pendentes (rótulos de enum/regras): `rotulos.ts`, `regras-formulario.ts`, `regras.ts`, resumo do cadastro, design-system-page (página interna de demonstração).
- 1.2/1.3 Catálogo criado; `mensagens-login.ts` e `app-error.ts` consomem o catálogo.
- 1.4 `mensagens.spec.ts`: 12 codes do contrato, genérico para desconhecido, sem termos proibidos.
- 2.x Grep de termos proibidos em `src` vazio; estados vazios/erro das telas têm texto e ação ("Tentar novamente"/próximo passo).
- 3.1 `lang="pt-BR"`; títulos por rota existem; shell tem `header`, `nav`, `main`.
- Desvio: `.specs/06-execution-plan/` não existe.
- `npm run lint`, `npm run build` e `npm test` (191 testes) verdes.

## Evidência do lote 2
- 3.2 Auditoria por tela via `a11y.spec.ts` (nomes, rótulos, ids, `aria-describedby`, `caption`, níveis de título, sem `tabindex` positivo); `aria-live`/`role` já existentes mantidos; gráficos com `aria-label` e legenda textual; foco visível global (`:focus-visible`). Correção: gaveta do shell fechada ficou `invisible` abaixo de `lg` (links fora da tela não recebem foco).
- 3.3 Diálogo já tinha foco preso/retorno; agora coberto por teste (Tab, Shift+Tab, Esc, retorno). Chips, seletor de sexo e etapas são `<button>` nativos (Enter/Espaço) com `aria-pressed`/`aria-current`.
- 3.4 Contraste calculado a partir de `styles.scss` para 24 pares usados: todos passam; nenhum token alterado.
- 3.5 Links de ação em linha ganharam `min-h-11` (painel, solicitar, 404); teste verifica botões e links das 6 telas.
- 4.1 Sem navegador nesta sessão: auditoria por classes responsivas (cartões abaixo de `md`, rolagem só dentro da tabela, KPIs `auto-fit`, formulários empilhados); `break-words` nos valores dos cartões. Capturas 320/768/1280 e zoom 200 % não foram feitas. [REVISAR]
- 5.1 `src/app/a11y.spec.ts` (sem nova dependência). Violações restantes: nenhuma detectada pelas regras implementadas; limitações (sem layout real/axe) documentadas em `shared/ui/doc.md`.
- 5.2 Fluxo só com teclado verificado estruturalmente: todos os controles são elementos nativos (button/a/input/select), sem `(click)` em elementos não interativos, formulários com botão de envio (Enter). Sem teste e2e real de Tab (fica para a SPEC-30). [REVISAR]
- 5.3 `doc.md` atualizados: `shared/ui` (seção de acessibilidade) e das 5 telas.
- `npm run lint`, `npm run build` e `npm test` (226 testes) verdes.

## Evidência do lote 3
- 5.4: `npm run lint` → "All files pass linting"; `npm run build` → bundle gerado sem erros; `npm test` → 32 arquivos, 226 testes passando.
- Desvio registrado: a pasta `.specs/06-execution-plan/` citada no índice não existe; a referência foi ignorada.
