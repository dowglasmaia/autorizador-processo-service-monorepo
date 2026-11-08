# Tarefas — Design system e layout

## 1. Ponto de partida
- [x] 1.1 Ler `.specs/INDEX.md`, `memory/`, `shared/como-executar.md`, SPEC-15, `design/DESIGN_SYSTEM_DISCOVERY.md` e o protótipo; registrar o desvio da pasta `06-execution-plan/` inexistente.
- [x] 1.2 Conferir a base da etapa 15 (Tailwind 4 via PostCSS, `core/http`, `features/home`, `app.html`) e planejar a substituição da home provisória.

## 2. Tokens, fontes e ícones
- [x] 2.1 Extrair do protótipo os valores efetivos (cores, rampas, raios, sidebar) e definir tokens semânticos em `src/styles.scss`.
- [x] 2.2 Verificar contraste AA dos pares de cor usados (ex.: texto sobre `accent`) e registrar.
- [x] 2.3 Empacotar Manrope e Phosphor via npm (sem CDN) e criar o componente `app-icon`.
- [x] 2.4 Documentar tokens e uso em `shared/ui/doc.md`.

## 3. Componentes base (`shared/ui`)
- [x] 3.1 `Button` (primário, secundário, ghost, ícone) e `Tag`.
- [x] 3.2 `Card`, `KpiCard`, `PageHeader`, `EmptyState` e `Skeleton`.
- [x] 3.3 `Field` com rótulo persistente, ajuda e erro (`aria-describedby`/`aria-invalid`), compatível com Reactive Forms.
- [x] 3.4 `Chip`/`SegmentedChoice` com `aria-pressed`.
- [x] 3.5 `DataTable` responsiva (tabela ≥ md; cartões rotulados em telas estreitas).
- [x] 3.6 `Dialog` acessível (foco preso, Esc, retorno do foco).

## 4. Núcleo e shell
- [x] 4.1 `NotificationService` (toast `role="status"`, 3,2 s, pausa ao foco) e `LoadingService` (overlay para ações do usuário; esqueleto para páginas).
- [x] 4.2 `ErrorDialog` ("Não foi possível carregar", Fechar/Tentar novamente).
- [x] 4.3 Serviço de status da API (`GET /actuator/health`; falha → "indisponível").
- [x] 4.4 `layout/Shell`: sidebar, gaveta e barra superior abaixo de `lg`, `<main>`, item ativo, foco visível.
- [x] 4.5 Remover `features/home`/`app-shell` provisórios; rotas lazy `/painel`, `/procedimentos`, `/solicitar`, `/autorizacoes` com páginas-esqueleto e rota `**`.
- [x] 4.6 Galeria `/_ds` apenas em desenvolvimento.

## 5. Testes e verificação
- [x] 5.1 Testes de componentes (desabilitado, erro, carregando) e dos serviços de `core`.
- [x] 5.2 Testes do shell em 320/768/1280 px e de teclado; teste de snapshot dos tokens.
- [x] 5.3 Conferir `grep -r "googleapis\|unpkg"` vazio e ausência de cores literais em templates.
- [x] 5.4 Rodar `npm run lint`, `npm run build` e `npm test`; corrigir falhas.
- [x] 5.5 Registrar evidência por tarefa, preencher "Resultado" da SPEC-15 e arquivá-la em `.specs/archive/` com `git mv`; atualizar o `INDEX.md`.
