# Relatório de paridade com o baseline (SPEC-31)

Execução dos golden cases (`cases.json`) contra a v1 (`api-autorizador-processo`) com a seed local
(1234, 4567, 6789), em ambiente Compose descartável. Sem credenciais nem dados reais.

- **Original:** status registrado no bloco `original` do `cases.json` (validado na SPEC-02, tag `v0-original`).
- **v1:** status obtido pelo `replay.mjs` na primeira execução completa em base limpa.
- **Classificação:** IDÊNTICO = decisão de negócio igual, sem DIV; DIV-xx = forma divergente aprovada e v1 conforme `mapeamentoV1`;
  NOVO = recurso que não existia na original e não tem DIV catalogada; FALHA = qualquer outro resultado.

## Resultado: 23/23 OK, zero FALHA (após corrigir o caso B03)

| Caso | Descrição resumida | Original | v1 | Status |
|------|--------------------|----------|----|--------|
| B01 | Listar procedimentos | 200 | 200 | DIV-07 (paginação; mesma decisão) |
| B02 | Cadastrar procedimento 9999 | 201 sem corpo | 201 com corpo | DIV-03 |
| B03 | Cadastrar com sexos `["M","F"]` | 400 | 201 | DIV-02 (caso corrigido; ver abaixo) |
| B04 | Cadastrar procedimento duplicado | 403 | 403 | DIV-04 (corpo RFC 9457) |
| B05 | Cadastro sem `numProcedimento` | 400 | 400 | DIV-06 |
| B06 | Cadastro com sexo inválido | 201 | 400 | DIV-02 |
| B07 | Listar após cadastro inválido | 500 | 200 | DIV-02 |
| B08 | JSON malformado | 400 | 400 | DIV-04 |
| B09 | Autorizar 4567, idade 20, sexo válido | 200 | 201 | DIV-05 (autorizado, igual) |
| B10 | Autorizar 4567, idade 30, sexo minúsculo | 200 | 201 | DIV-05 (autorizado, igual) |
| B11 | Autorizar 1234 fora da regra | 403 | 403 | DIV-05 (negado, igual) |
| B12 | Procedimento inexistente | 500 | 404 | DIV-01 |
| B13 | Autorizar sem `sexo` | 400 | 400 | DIV-05 |
| B14 | Número de procedimento não numérico | 400 | 400 | DIV-05 |
| B15 | Listar autorizações | 200 | 200 | DIV-07 |
| B16 | OPTIONS com Origin do front | 403 | 200 | NOVO (CORS inexistente na original; sem DIV catalogada) |
| B17 | `GET /actuator/health` | 404 | 200 | NOVO (Actuator inexistente na original; sem DIV catalogada) |
| A01 | `sexo: "Masculino"` na solicitação | só v1 | 400 | DIV-08 |
| A02 | Rota protegida sem token | só v1 | 401 | NOVO (JWT; só v1) |
| N01 | Cadastro com catálogo estendido | só v1 | 201 | NOVO (SPEC-12) |
| N02 | Cadastro sem `nome` | só v1 | 400 | NOVO (SPEC-12) |
| N03 | `quantidadeMaxima` sem `periodo` | só v1 | 400 | NOVO (SPEC-12) |
| N04 | Filtro por prefixo na listagem | só v1 | 200 | NOVO (SPEC-12) |

### Análise da falha inicial de B03 (resolvida)
- Causa: **o caso está desatualizado**, não a API. O `mapeamentoV1` de B03 envia só `numProcedimento`, `sexos` e `idades`; depois da
  SPEC-12 a v1 exige `nome` e `categoria` e responde `400 VALIDACAO_INVALIDA` (campos `categoria` e `nome`). B02, que já inclui esses campos, passa.
- Tratamento (aplicado): `nome` e `categoria` acrescentados ao corpo v1 de B03 em `cases.json`. Reflete o contrato aprovado (SPEC-12); a API não mudou e nenhuma DIV nova foi criada.
- Execução limpa final (ambiente `paridade` recriado do zero): `23/23 OK`, exit 0.

## Justificativa das DIV (todas existem em `BASELINE_COMPORTAMENTO_ORIGINAL.md`, tabela de divergências)

| DIV | Divergência | Justificativa |
|-----|-------------|---------------|
| DIV-01 | Inexistente: `500` (NPE) → `404 PROCEDIMENTO_NAO_ENCONTRADO` | Defeito da original; nenhum cliente pode depender dele. |
| DIV-02 | `sexos` 1/2 na entrada e M/F na saída → M/F; inválido → `400` | Elimina a assimetria e o `GET` quebrado por dado inválido. |
| DIV-03 | `201` sem corpo → `201` com o recurso e `GET /{id}` existente | A UI exibe sem nova consulta. |
| DIV-04 | Corpo de erro heterogêneo → RFC 9457 com `code` e `errors[]`, sem stack trace | Erros estáveis e sem vazamento de rastreio. |
| DIV-05 | `POST /autorizadores/{num}?idade&sexo` → `POST /autorizacoes` com corpo JSON, `201` | Dados do paciente fora da URL; resultado autorizado/negado idêntico. |
| DIV-06 | Sem validação → validações mínimas (listas vazias continuam permitidas, RN9) | Integridade mínima. |
| DIV-07 | Listas sem paginação → paginadas e ordenadas | Determinismo e escala. |
| DIV-08 | `sexo` fora de M/F → `403` → `400 VALIDACAO_INVALIDA` | Campo tipado; evita negação silenciosa. |

## Observações
- B16/B17 e os casos só da v1 não têm DIV catalogada porque a original não possuía esses recursos; ficam como NOVO, sem regressão.
- Leitura do aceite: a SPEC-31 pede B01, B02, B04, B09–B11 e B15 "IDÊNTICO", mas esses casos já têm DIV no `cases.json`. Aqui vale a
  decisão de negócio idêntica com forma divergente aprovada (classificação DIV-xx).
- Execuções parciais (`--only`) feitas depois da primeira rodada acusaram "base fora do estado inicial" (B03, N01, B15): o `replay.mjs`
  remove 9999 e 9998 ao final, mas não 9997 (N01), então a base não volta à semente. Corrigido: o `replay.mjs` agora também remove 9997 ao final. Uma segunda execução sem recriar a base ainda acusou base fora do estado inicial (18/23); por isso a validação oficial é sempre em ambiente recém-criado (`down -v` + `up`).

## Reprodução

```bash
# .env descartável (fora do git) com API_PORT, POSTGRES_PASSWORD, JWT_SECRET, AUTH_BOOTSTRAP_EMAIL e AUTH_BOOTSTRAP_SENHA
docker compose -p paridade --env-file <arquivo.env> up -d --build postgres api   # ou docker-compose
node .specs/workflows/ferramentas/parity/replay.mjs --target v1 --base-url http://localhost:<API_PORT> --email <e> --senha <s>
docker compose -p paridade --env-file <arquivo.env> down -v
```
