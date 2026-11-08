# Solicitar autorização

Tela `/solicitar`, dentro do `Shell` e protegida por `authGuard`. Escolhe o procedimento, informa idade e sexo e mostra o resultado ao lado (Autorizado, Negado ou estado inicial). Estrutura segue `features/login` e `features/procedimentos`.

## Operações consumidas

| Operação (OpenAPI)     | Uso                                                                                            | Onde                                 |
| ---------------------- | ---------------------------------------------------------------------------------------------- | ------------------------------------ |
| `solicitarAutorizacao` | `POST autorizacoes`; `201` → `autorizado`, `403 AUTORIZACAO_NEGADA` → `negado` (não é exceção) | `data-access/autorizacao.service.ts` |
| `listarProcedimentos`  | `GET procedimentos?size=200&sort=numProcedimento,asc` para o seletor, sem estado compartilhado | `data-access/autorizacao.service.ts` |

Os tipos vêm de `shared/api` (gerados); nenhum DTO é duplicado. O `sexo` segue como `string` no contrato; o front envia `M`/`F`.

## Peças

- `solicitar.routes.ts`: rota lazy `''` com título "Solicitar autorização · Autorizador".
- `data-access/autorizacao.service.ts`: serviço sem estado; devolve `{resultado, autorizacao?, detail?}`.
- `core/sessao/sessao-solicitacoes.ts`: signals `concedidas` e `negadas` (só em memória), para o KPI do painel.

Nenhuma decisão de elegibilidade no cliente; dados do paciente não são armazenados nem logados.

## Erros tratados em `pages/solicitar-page.ts`

- `AUTORIZACAO_NEGADA` (403): não é erro; vira o cartão **Negado**, sem toast.
- `PROCEDIMENTO_NAO_ENCONTRADO`: limpa a seleção, mostra a mensagem no seletor e recarrega a lista.
- `VALIDACAO_INVALIDA`: `campos` (`numProcedimento`, `idade`, `sexo`) aparecem em cada campo; outros vão para uma mensagem geral.
- Rede/servidor: `ErrorDialogService` com "Tentar novamente", que reenvia o formulário preservado.
- Falha ao carregar a lista: estado de erro no seletor com nova tentativa.

## Testes

`data-access/autorizacao.service.spec.ts`, `formulario-solicitacao.spec.ts`, `pages/solicitar-page.spec.ts` (autorizado, negado, inexistente, validação, rede, `?numero=`, Limpar e contadores) e `core/sessao/sessao-solicitacoes.spec.ts`.

## Acessibilidade (etapa 24)

Textos vêm do catálogo `core/i18n/mensagens.ts`; a página é coberta por `src/app/a11y.spec.ts` (rótulos, nomes acessíveis, alvos ≥ 44 px). Detalhes e limitações em `shared/ui/doc.md`.
