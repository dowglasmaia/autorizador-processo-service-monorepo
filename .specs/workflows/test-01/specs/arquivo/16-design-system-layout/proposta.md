# Design system e layout

Etapa 16 de 31 · Fase 3 · derivada da `SPEC-15` (`.specs/03-frontend/SPEC-15-design-system-layout.md`). Proposta revisada contra o que a etapa 15 (SPEC-14) entregou.

## Objetivo
Traduzir a identidade efetiva do protótipo (`.specs/design/prototype/Autorizador.dc.html`) em tokens Tailwind, componentes compartilhados (`shared/ui`) e o shell de navegação responsivo, reutilizáveis por todas as telas seguintes. Os tokens locais do protótipo prevalecem sobre o Broadsheet (ADR-09). Sem telas de negócio.

## Ponto de partida (conferido no repositório)
- `app-autorizador-processo/` **existe** (etapa 15): Angular 21.2, Tailwind 4.3 via `@tailwindcss/postcss` (`@use 'tailwindcss'` em `src/styles.scss`, sem tokens e com fonte Inter provisória), Vitest/jsdom, `angular-eslint`, scripts `lint`/`build`/`test`, `environment.apiBaseUrl = '/api/v1'`. Não há `@fontsource/*` nem `@phosphor-icons/web`.
- Estrutura atual: `core/http` (token da URL base, `api-error.interceptor`, `api-error`), `features/home` (página provisória) e `app.html` com `<main class="app-shell">` + `<router-outlet />`; rota `**` redireciona para a raiz. Não existem `layout/` nem `shared/ui/`.
- A SPEC-16 (tipos OpenAPI, `AppError`, camada HTTP) é a etapa seguinte; aqui o `ErrorDialog` recebe apenas título/mensagem simples, sem depender de `AppError`.
- Existem `.specs/design/DESIGN_SYSTEM_DISCOVERY.md` e `.specs/design/prototype/`.
- A pasta `06-execution-plan/` citada no `INDEX.md` não existe; desvio a registrar na evidência.
- O endpoint `GET /actuator/health` depende do Nginx da SPEC-13 do front (etapa de Docker do frontend, ainda não feita); em desenvolvimento o serviço de status usa o `apiBaseUrl`/origem do backend.

## Escopo
1. **Tokens** em `src/styles.scss` (`@theme`/variáveis CSS) com nomes semânticos (`bg-surface`, `text-content`, `bg-accent`, `bg-accent-2`, `bg-danger`, `divider`, neutros 100–900, sidebar `#03242b→#021a20`; raios 20/pílula/10/22; Manrope 400–800), documentados em `shared/ui/doc.md`, com contraste AA verificado para os pares usados.
2. **Fontes e ícones empacotados** (`@fontsource/manrope`, `@phosphor-icons/web`), sem CDN; utilitário/componente `app-icon`.
3. **Componentes `shared/ui`**: `Button`, `Tag`, `Card`/`KpiCard`, `DataTable` responsiva, `Field`, `Chip`/`SegmentedChoice`, `Dialog`, `EmptyState`, `Skeleton`, `PageHeader`.
4. **`layout/Shell`**: sidebar escura (marca "Autorizador · Procedimentos médicos"; itens Painel, Solicitar autorização, Procedimentos, Autorizações), status da API via serviço de `core`, barra superior com botão de menu abaixo de `lg` (gaveta), `<main>` como landmark, item ativo e foco visíveis.
5. **`core`**: `NotificationService` (toast `role="status"`, 3,2 s, pausa ao foco), `LoadingService` (overlay só para ações do usuário; esqueleto para carga de página), `ErrorDialog`.
6. **Rotas lazy** vazias (substituem a `features/home` provisória; `<main>` passa para o shell) `/painel`, `/procedimentos`, `/solicitar`, `/autorizacoes` com páginas-esqueleto e `**` → "Página não encontrada".
7. **Testes** (estados dos componentes, shell em 320/768/1280 px, teclado, snapshot de tokens) e **galeria `/_ds`** apenas em desenvolvimento.

## Fora do escopo (fica para outras etapas)
- Tipos gerados do OpenAPI e camada HTTP (SPEC-16), login/autenticação e guards (SPEC-23), telas de procedimentos, solicitação, autorizações e painel (SPEC-17 a SPEC-20), microcopy/acessibilidade final (SPEC-21).
- Dockerfile/Nginx do frontend (SPEC-13 do front) e CI do frontend (SPEC-40).
- UI kit de terceiros, latência simulada do protótipo, novos papéis ou funcionalidades.

## Critérios de aceitação
- *Given* viewport 320 px · *When* abrir o app · *Then* o menu é acessado por botão e não há rolagem horizontal; em 1280 px a sidebar fica fixa com o item ativo destacado.
- *Given* teclado · *Then* toda ação é alcançável com foco visível (Tab/Enter/Esc).
- `npm run lint`, `npm run build` e `npm test` verdes em `app-autorizador-processo/`.
- `grep -r "googleapis\|unpkg" app-autorizador-processo/src` vazio; nenhuma cor literal em templates; animações respeitam `prefers-reduced-motion`.
- Tokens e componentes documentados em `shared/ui/doc.md`; backend inalterado.

## Decisões tomadas sem consulta
- **Status da API:** o rodapé consulta `GET /actuator/health` pela origem relativa; em desenvolvimento sem Nginx, a falha resulta no estado "indisponível" (comportamento previsto na spec), sem proxy. [REVISAR]
- **Ícones:** usar `@phosphor-icons/web` com componente `app-icon` fino, por manter fidelidade ao protótipo e evitar CDN. [REVISAR]
- **Verificação visual:** sem navegador automatizado garantido, a verificação em 320/768/1280 px é feita por testes de componente e, se houver navegador disponível, capturas; caso contrário, registrar a limitação na evidência. [REVISAR]
- **Lote 2 — Dialog:** o clique no fundo (scrim) não fecha o diálogo (Esc e botões fecham), para atender às regras de acessibilidade do lint sem elemento interativo extra.
- **Lote 2 — Field:** `Field` recebe o erro como texto (`error`) e a diretiva `appFieldControl` liga `id`/`aria-*`; a tradução de erros do `FormControl` fica nas telas.
- **Lote 2 — Teste/visual:** componentes novos ainda sem specs próprios (tarefa 5.1/5.2); este lote verificou lint, build e testes existentes.
- **Galeria `/_ds`:** registrada apenas quando `isDevMode()`/ambiente de desenvolvimento, para não entrar no build de produção.
- **Pasta `06-execution-plan/`:** inexistente; ignorada e registrada.
- **Revisão feita:** proposta conferida contra a etapa 15 (ver ponto de partida); `features/home` e a fonte Inter provisória são substituídas por esta etapa.
- **Numeração:** sucessoras corrigidas conforme os arquivos reais em `.specs/03-frontend/` (login é SPEC-23).
- **Tokens (lote 1):** o protótipo não define cor de erro; `danger` = `accent-2-700` (`#aa3a2a`), pois branco sobre `accent-2` (`#e5604a`) dá 3,44:1 e branco sobre `accent` dá 3,00:1. O botão primário usa texto `content` sobre `accent` (5,05:1). Contrastes em `shared/ui/doc.md`. [REVISAR]
- **Nomes dos tokens:** `canvas` (fundo `bg` do protótipo), `content`/`content-muted` (texto), por evitar `bg-bg`/`text-text`.
- **Fontes/ícones:** CSS do Manrope (400–800) e do Phosphor duotone carregados pelo array `styles` do `angular.json` (evita `@import` Sass); dependências `@fontsource/manrope` e `@phosphor-icons/web` adicionadas, conforme a spec.
- **Button:** seletor `button[app-button]` (atributo no `<button>` nativo) com `eslint-disable` pontual da regra de prefixo de componente.
- **Lote 3 — Testes de largura:** jsdom não aplica Tailwind; 320/768/1280 px são verificados por classes de breakpoint e comportamento (gaveta, Esc, `aria-current`). Sem capturas visuais. [REVISAR]
- **Lote 3 — Galeria:** controlada por `environment.production` (a rota não existe em produção; o chunk lazy ainda é emitido).
- **Lote 3 — Rota raiz:** `''` redireciona para `/painel`; `**` mostra página "Página não encontrada" em vez de redirecionar.
- **Lote 3 — Snapshot de tokens:** `tokens.spec.ts` lê `src/styles.scss` via `node:fs` (sem `@types/node`, com `@ts-expect-error`).
- **Lote 3 — Orçamento:** aviso de bundle inicial passou de 500 kB para 600 kB em `angular.json` (506 kB com fontes/ícones locais). [REVISAR]
