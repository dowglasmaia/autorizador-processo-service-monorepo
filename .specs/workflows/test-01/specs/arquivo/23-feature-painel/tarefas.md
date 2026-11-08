# Tarefas — Tela do painel

## 1. Dados e rota
- [x] 1.1 Reaproveitar o que a etapa 22 entregou (`descreverRegras`, rota `/autorizacoes`, padrão de serviço) e conferir a compatibilidade de `Autorizacao` do painel com `AutorizacaoDto`.
- [x] 1.2 Criar `features/painel/data-access/painel.service.ts` com `carregar()` e signals `resumo/carregando/erro`, usando `ApiClient` e `ResumoPainel`.
- [x] 1.3 Criar `doc.md` da feature e manter a rota lazy `/painel` como padrão.

## 2. Componentes
- [x] 2.1 Criar `BarraCobertura` (barras horizontais proporcionais, com rótulo e valor).
- [x] 2.2 Criar `RoscaSexos` (SVG/conic-gradient, `role="img"`, legenda e total).
- [x] 2.3 Criar `UltimasAutorizacoes` (até 4 itens, link "Ver todas →", vazio por seção).
- [x] 2.4 Criar `calcularAprovacao` (0/0 → "—", 1/1 → 100%, 1/3 → 33%) com testes.

## 3. Página
- [x] 3.1 Montar cabeçalho com data por extenso (`Intl` pt-BR, função pura testada), hero e botão "Nova solicitação" → `/solicitar`.
- [x] 3.2 Montar os 4 KPIs com `KpiCard`, incluindo "Aprovação" ligada a `SessaoSolicitacoes` e legenda "solicitações desta sessão".
- [x] 3.3 Implementar estados de esqueleto, erro com "Tentar novamente" e vazio.
- [x] 3.4 Garantir responsividade (KPIs auto-fit, gráficos empilhados < md) e `prefers-reduced-motion`.
- [x] 3.5 Adicionar alternativa textual (tabela oculta) e conferir contraste AA dos gráficos.

## 4. Testes e verificação
- [x] 4.1 Testes do serviço com `HttpTestingController` (sucesso e erro).
- [x] 4.2 Testes de componentes e da página: seed 3/0/3/"—", barras 10→2/20→2/30→1, rosca M=2 F=3, vazio e 100% com uma concedida.
- [x] 4.3 Rodar `npm run lint`, `npm run build` e `npm test` e corrigir falhas.
- [x] 4.4 Registrar evidência por tarefa e o desvio da pasta `06-execution-plan`; verificação contra o Compose fica para a SPEC-30.
