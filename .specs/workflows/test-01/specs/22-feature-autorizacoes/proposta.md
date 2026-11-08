# Tela de autorizações

Etapa 22 de 31 · Fase 3 · derivada da `SPEC-19` (`.specs/03-frontend/SPEC-19-feature-autorizacoes.md`). Revisada contra o código entregue até a etapa 21.

## Objetivo
Entregar `/autorizacoes` ("Autorizações concedidas"): histórico somente leitura, com cabeçalho (kicker "Histórico", nota "Somente aprovações são registradas — negativas não deixam rastro"), botão "Nova solicitação" e tabela **ID · Procedimento (+nome) · Regras do procedimento · Status "Autorizado" · Data**, paginada, mais recentes primeiro.

## Ponto de partida (conferido no código após a etapa 21)
- `features/autorizacoes/` tem só `autorizacoes.routes.ts` e `pages/autorizacoes-page.ts`, página-esqueleto com `EmptyState` "Em construção". Não há serviço, `doc.md` nem testes.
- Núcleo HTTP pronto: `ApiClient`, `AppError`, interceptores e `isAppError`. `shared/ui` já tem `DataTable` (+ `DataTableCell`), `PageHeader`, `EmptyState`, `Skeleton`, `Button`, `Tag`.
- Tipos gerados: `Autorizacao.Autorizacao` (`id`, `procedimento`, `permitido`, `criadaEm: string | null`) e `PaginaAutorizacoes` (`itens`, `pagina`, `tamanho`, `totalItens`, `totalPaginas`). `GET /autorizacoes` aceita `page` e `size` (máx. 200) e já devolve da mais recente para a mais antiga.
- Referência de padrão: `ProcedimentosService` (signals `itens/carregando/erro/pagina/totalPaginas`, `carregar()`) e `procedimentos-page.ts`; `procedimentos/rotulos.ts` tem `ROTULO_SEXO`.
- A etapa 21 está entregue: `/solicitar` já tem o link "Ver autorizações concedidas" para esta rota; `routerLink="/autorizacoes"`; nada a alterar lá.
- **Correção:** a tela de procedimentos **não tem controle de paginação** (só chama `carregar({ pagina: 0 })`), logo não há o que reaproveitar; o controle será criado nesta etapa, local à feature (extrair para `shared/ui` só se ficar genérico sem custo extra). `TAMANHO_PAGINA = 20` está em `procedimentos.service.ts`. `AutorizacaoDto` e `PaginaAutorizacoes` já são reexportados por `shared/api`.
- `.specs/06-execution-plan/` não existe; desvio a registrar na evidência.

## Escopo
1. Rotas lazy, `doc.md` e `data-access/autorizacoes.service.ts` com `listarAutorizacoes(pagina)` e signals `itens/carregando/erro/pagina/totalPaginas/totalItens`.
2. Função pura `descreverRegras(procedimento)` → "Masculino e Feminino · 20, 30 anos"; um sexo → "Feminino · …"; sem idades → "Sem idades cadastradas" (RN9). Com testes.
3. Página com quatro estados: carregando (esqueleto), erro (nova tentativa), vazio ("Nada por aqui ainda" + CTA "Solicitar a primeira" → `/solicitar`) e sucesso (`DataTable` responsiva).
4. Coluna Status sempre "Autorizado" (`Tag`); coluna Data formata `criadaEm`, `null` → "—"; nome do procedimento só quando existir.
5. Paginação operável por teclado, anunciada por `aria-live`, com controle Anterior/Próxima criado nesta etapa.
6. Botão "Nova solicitação" → `/solicitar`.
7. Testes de serviço (`HttpTestingController`), de `descreverRegras` e de componente (4 estados, ordem, paginação).

## Fora do escopo (fica para outras etapas)
- Qualquer edição, cancelamento ou exclusão (tela somente leitura); filtros por status/data.
- Painel (etapa 24), microcopy/acessibilidade global (SPEC-21), e2e contra o Compose (SPEC-30).
- Alterações no backend ou no contrato; registro de negativas.

## Critérios de aceitação
- *Given* 2 autorizações para 4567 · *When* abrir `/autorizacoes` · *Then* 2 linhas, mais recente primeiro.
- *Given* nenhuma autorização · *Then* estado vazio com CTA "Solicitar a primeira".
- *Given* falha de rede · *Then* estado de erro com "Tentar novamente".
- `criadaEm` nulo mostra "—"; procedimento sem idades mostra "Sem idades cadastradas".
- `npm run lint`, `npm run build` e `npm test` verdes; sem `any`.

## Decisões tomadas sem consulta
- **Serviço próprio com estado**, no mesmo molde do `ProcedimentosService`, sem compartilhar estado com outras telas.
- **Ordenação:** confiada ao backend (mais recentes primeiro); o cliente não reordena.
- **Tamanho de página:** 20, igual à tela de procedimentos. [REVISAR]
- **Formato de data:** `dd/MM/yyyy HH:mm` no fuso local, via `DatePipe`.
- **Verificação contra o Compose:** adiada para a SPEC-30; aqui valem testes com `HttpTestingController`. [REVISAR]
- **Controle de paginação novo e local** (a etapa 21 não criou nenhum e procedimentos não possui): componente simples Anterior/Próxima com "Página X de Y" em `aria-live`, na feature; generalização em `shared/ui` fica para depois. [REVISAR]
- **Constante de tamanho:** definida na própria feature (20), sem importar de `procedimentos`, para não acoplar features.
- **Filtros:** o plano diz "filtros", mas a SPEC-19 e o contrato (`page`/`size`) não os preveem; não serão criados, para não inventar requisito. [REVISAR]
