# Tela do painel

Etapa 23 de 31 · Fase 3 · derivada da `SPEC-20` (`.specs/03-frontend/SPEC-20-feature-painel.md`). Referências: SPEC-09 (resumo no backend) e protótipo (`.specs/design/prototype/Autorizador.dc.html`, `renderVals`). Revisada contra a base entregue até a etapa 22.

## Objetivo
Entregar `/painel` (rota padrão) alimentado por `obterResumoPainel`: cabeçalho com data por extenso (pt-BR), hero "Autorize um procedimento em segundos" + "Nova solicitação", 4 KPIs (**Procedimentos**, **Autorizações**, **Idades cobertas**, **Aprovação** da sessão), **Cobertura por idade** (barras horizontais), **Sexos aceitos** (rosca M × F com legenda e total) e **Últimas autorizações** (até 4, "Ver todas →", vazio "Nenhuma autorização concedida ainda.").

## Ponto de partida (conferido no código após a etapa 22)
- `features/painel/` tem só `painel.routes.ts` e `pages/painel-page.ts`, esqueleto com `EmptyState` "Em construção". A rota `painel` já é o destino do redirect da raiz. Não há serviço, componentes, `doc.md` nem testes.
- Tipos gerados já reexportados por `shared/api`: `ResumoPainel`, `CoberturaIdade`, `DistribuicaoSexos` e `AutorizacaoDto`. Operação `obterResumoPainel` existe no contrato (`modules/painel.yaml`). **Sem novos endpoints.**
- `core/sessao/sessao-solicitacoes.ts` expõe `concedidas()` e `negadas()` (signals); a tela de solicitar já os alimenta.
- `shared/ui` já tem `KpiCard` (label/value/hint/icon/loading), `Card`, `PageHeader`, `EmptyState`, `Skeleton`, `Button`, `Tag`, `Icon`. Padrão de serviço com signals: `ProcedimentosService` (`ApiClient`, `AppError`).
- A etapa 22 foi entregue: a rota `autorizacoes` existe em `app.routes.ts` ("Ver todas →") e `features/autorizacoes/regras.ts` exporta `descreverRegras(procedimento)` (sexos + idades), reaproveitável em `UltimasAutorizacoes`. O padrão de serviço com signals (`AutorizacoesService`: `ApiClient`, `AppError`, `isAppError`) é o modelo a seguir. `ResumoPainel.ultimasAutorizacoes` usa o tipo `Autorizacao` do contrato do painel (confirmar compatibilidade com `AutorizacaoDto`).
- O locale `pt-BR` **não** está registrado (`registerLocaleData`/`LOCALE_ID` ausentes no código): a data por extenso usa `Intl.DateTimeFormat('pt-BR')`.
- `.specs/06-execution-plan/` não existe; desvio a registrar na evidência.

## Escopo
1. `data-access/painel.service.ts` com `carregar()` e signals `resumo/carregando/erro`; `doc.md` da feature.
2. Componentes de apresentação locais: `BarraCobertura`, `RoscaSexos` (SVG ou `conic-gradient`, `role="img"` com descrição), `UltimasAutorizacoes`; reaproveitar `KpiCard` existente.
3. Função pura `calcularAprovacao(concedidas, negadas)`: 0/0 → "—", 1/1 → "100%", 1/3 → "33%". KPI rotulado "solicitações desta sessão".
4. Estados: esqueleto, erro com "Tentar novamente", vazio por seção.
5. Responsividade (KPIs `auto-fit`, gráficos empilhados abaixo de `md`) e `prefers-reduced-motion`.
6. Gráficos sem biblioteca, com alternativa textual (tabela oculta visualmente) e cores com contraste AA.
7. Testes de serviço (`HttpTestingController`), cálculo, componentes e página.

## Fora do escopo (fica para outras etapas)
- Novos endpoints ou alteração do contrato/backend; persistir negativas (PEN-01).
- Revisão global de microcopy e acessibilidade (SPEC-21, etapa 24); e2e e comparação visual contra o Compose (SPEC-30).
- Biblioteca de gráficos.

## Critérios de aceitação
- *Given* seed e nenhuma autorização · *When* abrir `/painel` · *Then* KPIs 3 / 0 / 3 / "—", barras 10→2, 20→2, 30→1, rosca M=2 F=3 e "Nenhuma autorização concedida ainda."
- *Given* uma autorização concedida na sessão · *Then* "Aprovação" 100%.
- Falha de rede → estado de erro com nova tentativa.
- Valores vêm do servidor, sem recomputar a partir de listas.
- Regras dos itens recentes exibidas com `descreverRegras` (sem duplicar lógica).
- `npm run lint`, `npm run build` e `npm test` verdes; sem `any`.

## Decisões tomadas sem consulta
- **Componentes locais à feature** (não `shared/ui`), exceto o `KpiCard` já existente; generalizar só se houver reuso.
- **Sem biblioteca de gráficos:** CSS/SVG, conforme a SPEC-20. 
- **Verificação contra o Compose e capturas 320/768/1280:** adiadas para a SPEC-30; aqui valem testes com `HttpTestingController` e, se o Docker estiver disponível, conferência opcional. [REVISAR]
- **Cores dos gráficos:** tokens do design system existentes, verificados para contraste AA; dois tons distintos mais rótulo textual para M/F (não só cor).
- **Aprovação da sessão:** calculada no cliente a partir de `SessaoSolicitacoes`, arredondada ao inteiro; reinicia ao recarregar a página (documentado na tela).
- **Data por extenso:** `Intl.DateTimeFormat('pt-BR')` em função pura testável, pois o locale não está registrado no app (evita alterar a configuração global).
- **Barras:** largura proporcional ao maior valor devolvido pelo servidor.
- **Recarga:** o painel recarrega o resumo a cada entrada na rota (dados sempre atuais), sem cache. [REVISAR]
- **Lote 1 — compatibilidade de tipos:** `Autorizacao` do contrato do painel tem os mesmos campos de `AutorizacaoDto` (id, procedimento, permitido, criadaEm nulo); `UltimasAutorizacoes` usa `AutorizacaoDto` e `descreverRegras` direto.
- **Lote 1 — `calcularAprovacao` e `formatarDataExtenso`** ficam em `features/painel/calculos.ts` (funções puras, testadas em `calculos.spec.ts`). Aprovação 0/N → "0%".
- **Lote 1 — Rosca** em SVG com `stroke-dasharray` (M = `accent-700`, F = `accent-2-700`); a página só carrega o resumo no lote seguinte (3.2/3.3), por isso o `PainelPage` atual traz apenas cabeçalho, hero e botão.

## Evidência — lote 2
- **3.2** `PainelPage` com 4 `KpiCard` (Procedimentos, Autorizações, Idades cobertas do servidor; Aprovação via `SessaoSolicitacoes` + `calcularAprovacao`, legenda "solicitações desta sessão").
- **3.3** Esqueleto (`loading` nos KPIs, `app-skeleton` nas seções), erro com "Tentar novamente" (`EmptyState`) e vazio por seção.
- **3.4** KPIs em `grid-template-columns: repeat(auto-fit, minmax(14rem, 1fr))`; gráficos em `md:grid-cols-2` (empilhados abaixo de `md`); `motion-reduce:transition-none` nas barras.
- **3.5** Tabela `sr-only` nas barras; rosca com `role="img"`, `aria-label` e legenda textual; tokens `accent-700`/`accent-2-700` sobre fundo claro (contraste AA assumido dos tokens do design system; sem medição automatizada).
- **4.1–4.2** `painel.service.spec.ts`, `components/componentes.spec.ts`, `pages/painel-page.spec.ts` (seed 3/0/3/"—", barras 10→2/20→2/30→1, rosca M=2 F=3, vazio, 100%, erro e nova tentativa).
- **4.3** `npm run lint` OK; `npm run build` OK; `npm test`: 30 arquivos, 188 testes passando.
- **4.4** Desvio: a pasta `.specs/06-execution-plan/` citada no índice não existe (ignorada). Verificação contra o Compose e capturas ficam para a SPEC-30.

## Decisões — lote 2
- Ícones dos KPIs (`clipboard-text`, `check-circle`, `users`, `chart-pie`) e hints ("cadastrados", "concedidas", "faixas distintas") escolhidos sem referência no protótipo; microcopy será revisada na etapa 24.
- Com erro, a página oculta KPIs e seções e mostra só o estado de erro. O KPI "Aprovação" não usa esqueleto (é local).
