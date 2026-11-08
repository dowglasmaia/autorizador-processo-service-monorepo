# SPEC-15 — Design system e layout (shell)

| Campo | Valor |
|---|---|
| Domínio / projeto | Frontend · `core`/`layout`/`shared/ui` |
| Prioridade | P1 |
| Depende de | SPEC-14 |
| Sucessoras | SPEC-17, SPEC-18, SPEC-19, SPEC-20, SPEC-23 |
| Paralelismo | junto com SPEC-13 e SPEC-03 |

## 1. Objetivo
Traduzir a identidade **efetiva** do protótipo (`Autorizador.dc.html`) em tokens Tailwind, componentes compartilhados e o shell de navegação responsivo, que serão reutilizados por todas as funcionalidades. Os tokens locais do protótipo sobrepõem o Broadsheet (ADR-09).
**Tokens:** fonte Manrope 400–800; `bg #f4fafa`, `surface #fff`, `text #0b2a2f`, `accent #0fa88c` (rampa 100–900 do HTML), `accent-2 #e5604a` (rampa), `divider rgba(7,70,66,.14)`, neutros 100–900; sidebar `#03242b→#021a20`; raios: cartão 20, botão pílula, input 10, diálogo 22; ícones Phosphor duotone.
**Restrições:** sem CDN em produção (fontes e ícones empacotados via npm: `@fontsource/manrope`, `@phosphor-icons/web` — dependências de UI justificadas, sem UI kit); nenhuma cor literal em templates (só tokens); **a sidebar fixa de 250 px do protótipo não é responsiva** → menu recolhível/gaveta em telas estreitas; animações respeitam `prefers-reduced-motion`; não replicar latência simulada do protótipo.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `.specs/design/prototype/Autorizador.dc.html` e `uploads/`, `CURRENT_STATE_ANALYSIS.md` §3, `design/DESIGN_SYSTEM_DISCOVERY.md` (§7–9 para telas); skills `angular-tailwind-ui-specialist` (aplicar integralmente; os tokens de referência da skill — "Grafite Ciano" — são substituídos pelos tokens do Autorizador definidos aqui, e o equivalente de `references/neon-graphite-tokens.md` passa a ser `shared/ui/doc.md`), `ui-ux-pro-max`

## 3. Tarefas
- [ ] **T1.** Tokens em `src/styles.scss` (`@theme`/variáveis CSS) mapeados para nomes semânticos (`bg-surface`, `text-content`, `bg-accent`, `bg-danger`...); documentar em `shared/ui/doc.md`. Contraste AA verificado para os pares usados (ex.: texto sobre `accent`).
- [ ] **T2.** Fontes e ícones empacotados (sem requisição externa); utilitário de ícone (`<i class="ph-duotone ph-…">` ou componente `app-icon`).
- [ ] **T3.** Componentes `shared/ui`: `Button` (primário, secundário, ghost, ícone), `Tag` (accent, accent-2, neutral), `Card`/`KpiCard`, `DataTable` responsiva (tabela ≥ md; cartões rotulados em telas estreitas), `Field` (rótulo persistente + ajuda + erro com `aria-describedby`/`aria-invalid`), `Chip`/`SegmentedChoice` (botões com `aria-pressed`), `Dialog`, `EmptyState`, `Skeleton`, `PageHeader` (kicker + título + descrição + ação).
- [ ] **T4.** `layout/Shell`: sidebar escura (marca "Autorizador · Procedimentos médicos"; itens **Painel, Solicitar autorização, Procedimentos, Autorizações**; rodapé de status da API consultando `GET /actuator/health` (exposto pelo Nginx, SPEC-13) por um serviço de `core`; falha → estado "indisponível"); barra superior com botão de menu em telas < lg; `<main>` com landmark; item ativo destacado; foco visível.
- [ ] **T5.** `core`: `NotificationService` (toast, `role="status"`, 3,2 s, pausa ao foco), `LoadingService` (overlay só para ações do usuário — salvar/verificar — e **esqueleto** para carga de página), `ErrorDialog` ("Não foi possível carregar" com Fechar/Tentar novamente).
- [ ] **T6.** Rotas lazy vazias para `/painel`, `/procedimentos`, `/solicitar`, `/autorizacoes` com páginas-esqueleto; `**` → "Página não encontrada".
- [ ] **T7.** Testes: componentes (estados desabilitado/erro/carregando), shell em 320/768/1280 px, teclado (Tab/Enter/Esc), snapshot de tokens.
- [ ] **T8.** Galeria de componentes (rota `/_ds` apenas em desenvolvimento) para revisão visual e a11y.

**Aceite:** *Given* viewport 320 px · *When* abrir o app · *Then* o menu é acessado por botão, sem rolagem horizontal da página; em 1280 px a sidebar fica fixa com item ativo. *Given* teclado · *Then* toda ação é alcançável com foco visível.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T4, T8 | `angular-tailwind-ui-specialist`, `ui-ux-pro-max` |
| T4–T6 | `angular-architect` |
| T3, T5 (rótulos/mensagens) | `copywriting` (tom de produto, não marketing) |

## 5. Verificação obrigatória
- `npm run build && npm test && npm run lint` → verdes
- Verificação visual em 320/768/1280 px contra o protótipo (capturas anexas na evidência); checklist `ui-review-checklist` da skill; nenhuma requisição a `fonts.googleapis.com`/`unpkg.com` na aba de rede (`grep -r "googleapis\|unpkg" app-autorizador-processo/src` → vazio)
- Definition of Done: tokens e componentes documentados; shell responsivo; a11y básica verificada; nenhuma cor literal em templates.

## 6. Resultado
_Preencher ao concluir._
