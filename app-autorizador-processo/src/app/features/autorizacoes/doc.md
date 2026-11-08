# Autorizações

Tela `/autorizacoes` ("Autorizações concedidas"): histórico somente leitura, dentro do `Shell` e protegido por `authGuard`.

## Operações consumidas

| Operação (OpenAPI)   | Uso                                                                                         | Onde                                  |
| -------------------- | ------------------------------------------------------------------------------------------- | ------------------------------------- |
| `listarAutorizacoes` | `GET autorizacoes` paginado (20 por página); o backend ordena da mais recente à mais antiga | `data-access/autorizacoes.service.ts` |

Os tipos vêm de `shared/api` (gerados); nenhum DTO é duplicado.

## Peças

- `autorizacoes.routes.ts`: rota lazy `''` com título.
- `data-access/autorizacoes.service.ts`: signals `itens`, `carregando`, `erro`, `pagina`, `totalPaginas`, `totalItens`; `listarAutorizacoes(pagina)`.
- `regras.ts`: `descreverRegras(procedimento)` — "Masculino e Feminino · 20, 30 anos"; sem idades, "Sem idades cadastradas" (RN9).
- `pages/autorizacoes-page.ts`: cabeçalho, botão "Nova solicitação" (`/solicitar`) e estados carregando, erro ("Tentar novamente"), vazio ("Solicitar a primeira") e tabela (ID · Procedimento · Regras · Status "Autorizado" · Data; `criadaEm` nulo → "—").

## Pendências desta etapa

Controle de paginação (3.4) e testes de componente (4.1) vêm no próximo lote.

## Acessibilidade (etapa 24)

Textos vêm do catálogo `core/i18n/mensagens.ts`; a página é coberta por `src/app/a11y.spec.ts` (rótulos, nomes acessíveis, alvos ≥ 44 px). Detalhes e limitações em `shared/ui/doc.md`.
