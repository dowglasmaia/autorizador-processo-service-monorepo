# Tela solicitar autorização

Etapa 21 de 31 · Fase 3 · derivada da `SPEC-18` (`.specs/03-frontend/SPEC-18-feature-solicitar-autorizacao.md`). Revisada contra o código entregue pelas etapas 16 a 20.

## Objetivo
Entregar `/solicitar`: escolher o procedimento, informar idade e sexo, solicitar e ver o resultado ao lado — **Autorizado**, **Negado** ou o estado inicial "O resultado aparece aqui." — com os botões **Solicitar autorização** e **Limpar**. Sem histórico (fica para a etapa 22).

## Ponto de partida (conferido no repositório após a etapa 20)
- `features/solicitar/` tem só `solicitar.routes.ts` (rota `''`, lazy, título definido) e `pages/solicitar-page.ts`, página-esqueleto com `EmptyState` "Em construção". Não há serviço, `doc.md` nem testes.
- `app.routes.ts` já protege a rota com `authGuard` dentro do `Shell` (etapa 18).
- Núcleo HTTP pronto (etapa 17): `ApiClient`, `AppError` (`kind`, `code`, `campos`), `errorInterceptor`, `NotificationService`, `ErrorDialogService`, `LoadingService` e o operador `resultadoNegocio()` (`core/http/resultado-negocio.ts`), que converte `AUTORIZACAO_NEGADA` em `{resultado:'negado', detail}`.
- Tipos gerados em `shared/api`: `Autorizacao.AutorizacaoRequest` (`numProcedimento`, `idade`, `sexo`), `Autorizacao.Autorizacao` (`permitido`, `procedimento`, `criadaEm`), `Procedimento` e a página de procedimentos. O `sexo` é `string` no contrato; o front envia `M`/`F`.
- `shared/ui` (etapa 16): Button, Field, Card, Chip, Tag, EmptyState, PageHeader, Skeleton, Icon, Dialog. `SegmentedChoice` (`shared/ui/chip/chip.ts`, com `options`, `value` como `model` e `label`) já existe e é reaproveitado.
- A etapa 20 entregou `ProcedimentosService` (root, signals de lista paginada de 20 itens, `listarProcedimentos(q, pagina)` com `size` fixo em `TAMANHO_PAGINA`) e a ação "Solicitar →" para `/solicitar?numero=n`. Esse serviço não atende ao seletor (`size=200`) sem alterar o estado da tela de procedimentos; por isso a feature faz a própria leitura mínima da lista.
- Não existe `core/sessao/`; `features/autorizacoes` ainda é esqueleto (etapa 22).
- `.specs/06-execution-plan/` não existe; desvio a registrar na evidência.

## Escopo
1. Rotas lazy, `doc.md` do módulo e `data-access/autorizacao.service.ts` com `solicitarAutorizacao` (usa `resultadoNegocio()`), devolvendo `{resultado:'autorizado'|'negado', autorizacao?, detail?}`.
2. Serviço de sessão `SessaoSolicitacoes` em `core` (signals `concedidas` e `negadas`, só em memória, sem persistir), para o KPI "Aprovação" do painel (etapa 24).
3. Formulário tipado: procedimento (obrigatório), idade (inteiro 0..150) e sexo (escolha segmentada M/F com `aria-pressed`), com mensagens por campo.
4. Seletor "Nº n" (+ nome, quando existir) carregado, via método próprio de `autorizacao.service.ts` (sem tocar o estado de `ProcedimentosService`), por `GET /procedimentos?size=200&sort=numProcedimento,asc`; `?numero=` pré-seleciona o procedimento.
5. Painel de resultado com 3 estados e ícones, animação curta (respeita `prefers-reduced-motion`) e `aria-live="polite"`:
   - Autorizado (verde): "Procedimento n liberado para paciente masculino/feminino de N anos" e link "Ver autorizações concedidas" (`/autorizacoes`);
   - Negado (coral): mensagem do backend por `code` e nota "A regra não informa qual critério falhou", sem toast de erro;
   - Inicial: "O resultado aparece aqui.".
6. Erros: `PROCEDIMENTO_NAO_ENCONTRADO` mostra erro no seletor e recarrega a lista; `VALIDACAO_INVALIDA` mapeia `campos` aos campos; rede/servidor → modal com "Tentar novamente" e formulário preservado. Falha ao carregar a lista também mostra estado de erro com nova tentativa.
7. Botão **Limpar** restaura formulário e resultado; nenhuma decisão de elegibilidade no cliente; dados do paciente não são armazenados nem logados.
8. Testes de serviço e componente (autorizado, negado, inexistente, validação, rede, `?numero=`, contadores) e `doc.md`.

## Fora do escopo (fica para outras etapas)
- Histórico de autorizações na tela (etapa 22) e painel/KPI em si (etapa 24); aqui só os contadores de sessão são expostos.
- Microcopy e acessibilidade global (SPEC-21), e2e e capturas contra o Compose (SPEC-30).
- Alterações no backend ou no contrato; regra de elegibilidade no cliente.

## Critérios de aceitação
- *Given* seed · *When* solicitar 4567/20/F · *Then* cartão "Autorizado" e link para o histórico.
- *When* 1234/30/M · *Then* cartão "Negado", sem toast de erro.
- *When* o procedimento é removido entre a carga e o envio · *Then* erro tratado no seletor e lista recarregada.
- *Given* `/solicitar?numero=4567` · *Then* o procedimento vem pré-selecionado.
- Resultado anunciado por `aria-live`; contadores de sessão atualizados a cada resposta.
- `npm run lint`, `npm run build` e `npm test` verdes; sem `any`.

## Decisões tomadas sem consulta
- **Lista do seletor:** `ProcedimentosService` fixa `size=20` e guarda estado da tela de procedimentos; a feature terá `listarProcedimentos()` próprio (`size=200`, sem estado compartilhado), com os mesmos tipos gerados. [REVISAR]
- **Escolha segmentada:** reaproveita-se o `SegmentedChoice` de `shared/ui`; se faltar `aria-pressed` ou teclado, ajusta-se o componente compartilhado com teste. [REVISAR]
- **Nome do procedimento:** exibido no seletor apenas quando existir (registros antigos podem não ter).
- **Contadores:** apenas signals em memória, perdidos ao recarregar a página, conforme ADR-11.
- **Execução contra o Compose:** adiada para a SPEC-30; aqui valem testes com `HttpTestingController`. [REVISAR]
- **Limite de idade 0..150**, alinhado ao backend.
