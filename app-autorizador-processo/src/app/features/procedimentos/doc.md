# Procedimentos

Telas `/procedimentos` (lista) e `/procedimentos/novo` (cadastro em 4 etapas), dentro do `Shell` e protegidas por `authGuard`.

## Operações consumidas

| Operação (OpenAPI)      | Uso                                                                                                   | Onde                                   |
| ----------------------- | ----------------------------------------------------------------------------------------------------- | -------------------------------------- |
| `listarProcedimentos`   | `GET procedimentos` paginado (20 por página, `numProcedimento,asc`); `q` filtra por prefixo do número | `data-access/procedimentos.service.ts` |
| `cadastrarProcedimento` | `POST procedimentos` com `ProcedimentoRequest`; `201` devolve o `Procedimento`                        | `data-access/procedimentos.service.ts` |

Os tipos vêm de `shared/api` (gerados); nenhum DTO é duplicado.

## Peças

- `procedimentos.routes.ts`: rotas lazy `''` e `novo`.
- `pages/procedimentos-page.ts`: lista com esqueleto, vazio com CTA, erro e sucesso; "Solicitar →" leva a `/solicitar?numero=n`.
- `pages/procedimento-novo-page.ts`: `FormGroup` tipado por etapa (identificação, cobertura, elegibilidade, utilização), chips de navegação, resumo e envio.
- `regras-formulario.ts`: funções puras (`faixaParaIdades` 0..150, `ambosParaSexos`, `validarQuantidadePeriodo`).
- `rotulos.ts`: rótulos em português dos enums gerados.

## Cadastro

- Obrigatórios: número, nome e categoria. `quantidadeMaxima` exige `periodo` e vice-versa.
- Faixa de idade vira lista de idades (chips removíveis); sexo "Ambos" vira `["M","F"]`. Sem idade ou sexo, aviso (não bloqueio) de que nada será autorizado (RN9).
- Etapa 4: `exigeAutorizacaoPrevia` e `exigeJustificativaMedica` pré-selecionadas; texto de apoio informa que cobertura, quantidade, prévia e justificativa não alteram a autorização.
- Campos opcionais vazios são omitidos do corpo. Sucesso: toast "Procedimento n cadastrado" e volta à lista.

## Erros

| Situação                            | Apresentação                                                                   |
| ----------------------------------- | ------------------------------------------------------------------------------ |
| `PROCEDIMENTO_DUPLICADO`            | mensagem no campo número, volta à etapa 1                                      |
| `VALIDACAO_INVALIDA` com `errors[]` | mensagem em cada campo; vai à primeira etapa com erro; sem mapa → alerta geral |
| rede / servidor                     | modal com "Tentar novamente" (reenvia); formulário preservado                  |

O erro do servidor some quando qualquer valor do formulário é alterado.
