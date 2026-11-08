# API_CONTRACTS — contrato REST v1

Este documento é a **proposta de contrato aprovável** exigida pela skill `openapi-rest-contract` (módulos `procedimento`, `autorizacao`, `painel`, erros comuns). SPEC-05 implementa o contrato base; SPEC-12 faz a atualização aditiva do módulo `procedimento`. O OpenAPI gerado em `src/main/resources/openapi/` passa a ser a fonte de verdade; em divergência, **o YAML vence**.

Convenções: base `/api/v1` · JSON UTF-8 · datas ISO-8601 UTC · nomes `camelCase` · listas paginadas · **autenticação Bearer JWT em todas as rotas, exceto `POST /auth/login`** (§6) · erros RFC 9457 (`application/problem+json`).

## 1. Endpoints

| operationId | Método e rota | Sucesso | Erros |
|---|---|---|---|
| `listarProcedimentos` | `GET /procedimentos?q&page&size&sort` | `200 PaginaProcedimentos` | `400` |
| `cadastrarProcedimento` | `POST /procedimentos` | `201 Procedimento` + `Location: /api/v1/procedimentos/{id}` | `400 VALIDACAO_INVALIDA/CORPO_INVALIDO`, `403 PROCEDIMENTO_DUPLICADO` |
| `obterProcedimento` | `GET /procedimentos/{id}` | `200 Procedimento` | `404 PROCEDIMENTO_NAO_ENCONTRADO` |
| `solicitarAutorizacao` | `POST /autorizacoes` | `201 Autorizacao` | `400`, `403 AUTORIZACAO_NEGADA`, `404 PROCEDIMENTO_NAO_ENCONTRADO` |
| `listarAutorizacoes` | `GET /autorizacoes?page&size` | `200 PaginaAutorizacoes` (mais recentes primeiro) | `400` |
| `obterResumoPainel` | `GET /painel/resumo` | `200 ResumoPainel` | — |
Autenticação: `POST /auth/login` e `GET /auth/me` (§6). Operacional (fora do contrato de negócio): `GET /actuator/health`, `GET /actuator/health/liveness|readiness`.

Paginação: `page` (≥0, padrão 0), `size` (1..200, padrão 20), `sort` aceita apenas `numProcedimento,asc|desc` em procedimentos (padrão `asc`). `q`: dígitos; filtra `numProcedimento` por prefixo.

## 2. Esquemas (contrato base — SPEC-05)

```yaml
Sexo: {type: string, enum: [M, F]}                       # entrada aceita também minúsculas (RN7); saída sempre M/F
ProcedimentoRequest:                                     # required: numProcedimento
  numProcedimento: integer, minimum 1
  sexos:  array<Sexo>, uniqueItems            # pode ser vazio (RN9)
  idades: array<integer 0..150>, uniqueItems  # pode ser vazio (RN9)
Procedimento:                                            # saída
  id: integer(int64); numProcedimento: integer
  sexos: array<Sexo> (ordem M,F); idades: array<integer> (ordenada ascendente)
  criadoEm: date-time
AutorizacaoRequest:                                      # required: todos
  numProcedimento: integer >=1 ; idade: integer 0..150 ; sexo: string pattern ^[MFmf]$
Autorizacao:   { id: int64, procedimento: Procedimento, permitido: boolean, criadaEm: date-time|null }
Pagina<T>:     { itens: T[], pagina: int, tamanho: int, totalItens: int64, totalPaginas: int }
ResumoPainel:
  totalProcedimentos: int ; totalAutorizacoes: int ; idadesCobertas: int
  coberturaPorIdade: [{idade: int, quantidade: int}]      # nº de procedimentos que aceitam cada idade, idade asc
  distribuicaoSexos: {masculino: int, feminino: int}      # nº de procedimentos que aceitam cada sexo
  ultimasAutorizacoes: Autorizacao[] (até 4)
ProblemDetail (RFC 9457 + extensões):
  type: uri · title · status · detail (pt-BR) · instance
  code: string (estável, ver §3) · timestamp: date-time
  errors: [{campo, mensagem}]   # só em 400 de validação
```

### Acréscimo NEW-01 (SPEC-12, `Procedimento` e `ProcedimentoRequest`)
| Campo | Tipo | Obrigatório (criação) | Regra |
|---|---|---|---|
| `nome` | string 1..200 | **sim** | texto livre (trim) |
| `codigoTuss` | string ≤30 | não | |
| `categoria` | enum `EXAMES_IMAGEM`,`CONSULTAS`,`CIRURGIAS`,`TERAPIAS`,`EXAMES_LABORATORIAIS` | **sim** | lista do protótipo (PRM-06) |
| `especialidade` | enum `CARDIOLOGIA`,`ORTOPEDIA`,`PEDIATRIA`,`GINECOLOGIA`,`NEUROLOGIA` | não | lista do protótipo (PRM-06) |
| `tipoCobertura` | enum `PLANO`,`REGRAS`,`NAO_COBERTO` | não (padrão `PLANO`) | informativo (PEN-02) |
| `quantidadeMaxima` | integer ≥1 | não | exige `periodo` |
| `periodo` | enum `MES`,`ANO`,`VIDA` | condicional | exige `quantidadeMaxima` |
| `exigeAutorizacaoPrevia` | boolean | não (padrão `false`) | informativo; a UI pré-seleciona `true` como o protótipo |
| `exigeJustificativaMedica` | boolean | não (padrão `false`) | informativo; a UI pré-seleciona `true` |
Na **saída**, todos podem ser `null` para procedimentos anteriores à SPEC-12 (a UI exibe "Sem nome"/"—"). O contrato de listas `Procedimento` aninhado em `Autorizacao` inclui os mesmos campos.

## 3. Catálogo de erros
| `code` | HTTP | Quando | `detail` (pt-BR) |
|---|---|---|---|
| `VALIDACAO_INVALIDA` | 400 | campo inválido/ausente (corpo ou parâmetro) | "Dados inválidos." + `errors[]` |
| `CORPO_INVALIDO` | 400 | JSON malformado ou tipo incorreto | "Corpo da requisição inválido." |
| `PARAMETRO_INVALIDO` | 400 | `page/size/sort/id` inválidos | "Parâmetro inválido: x." |
| `PROCEDIMENTO_DUPLICADO` | 403 | nº já cadastrado (preserva status da versão original; PEN-04) | "Procedimento já cadastrado na base de dados: Nº 9999" |
| `AUTORIZACAO_NEGADA` | 403 | sexo e/ou idade não aderentes | "Operação não autorizada: sexo e/ou idade não aderentes ao procedimento solicitado." |
| `PROCEDIMENTO_NAO_ENCONTRADO` | 404 | nº/id inexistente | "Procedimento não cadastrado na base de dados: Nº 55555" |
| `ERRO_INTERNO` | 500 | falha inesperada | "Erro interno. Tente novamente." (sem stack trace; `traceId` no log) |
O frontend decide por `code`, nunca por texto. Os dois `403` são distinguidos por `code`.

## 4. Mapeamento original → v1 (usado por SPEC-02/31)
| Caso do baseline | Chamada v1 | Resultado esperado v1 |
|---|---|---|
| B01 | `GET /procedimentos` | 200; 3 itens da seed; `idades` ordenadas |
| B02 | `POST /procedimentos` `sexos:["M","F"]` | 201 (+ corpo) |
| B04 | repetir B02 | 403 `PROCEDIMENTO_DUPLICADO` |
| B05 | sem `numProcedimento` | 400 `VALIDACAO_INVALIDA` |
| B06/B07 | `sexos:["X"]` | 400 (DIV-02) e `GET` continua 200 |
| B09/B10 | `POST /autorizacoes` 4567/20/F e 30/m | 201 |
| B11 | 1234/30/M | 403 `AUTORIZACAO_NEGADA`; nada gravado |
| B12 | 55555/30/M | 404 (DIV-01) |
| B13/B14 | campo ausente / tipo inválido | 400 |
| B15 | `GET /autorizacoes` | 200; várias para 4567 (RN11) |
| B16 | origem `http://localhost:4200` | CORS permitido no perfil `local` |
| B17 | `GET /actuator/health` | 200 `UP` |
| (novo) | `sexo:"Masculino"` | 400 (DIV-08; original: 403) |
| (novo) | qualquer rota sem token / token expirado | 401 `NAO_AUTENTICADO` / `SESSAO_EXPIRADA` (NEW-04) |

## 5. Versionamento e compatibilidade
Prefixo `/api/v1`. Mudanças aditivas (campos opcionais) não incrementam a versão; remoções/renomeações exigem `/v2`. SPEC-32 detecta desvio entre contrato e tipos do frontend.

## 6. Autenticação (NEW-04, SPEC-22) — proposta de contrato do módulo `auth`
Esquema de segurança global `bearerAuth` (`type: http`, `scheme: bearer`, `bearerFormat: JWT`) aplicado a todas as operações; `autenticar` declara `security: []` (público).

| operationId | Método e rota | Sucesso | Erros |
|---|---|---|---|
| `autenticar` | `POST /auth/login` | `200 TokenResponse` | `400 VALIDACAO_INVALIDA`, `401 CREDENCIAIS_INVALIDAS`, `429 MUITAS_TENTATIVAS` (`Retry-After`) |
| `obterUsuarioAtual` | `GET /auth/me` | `200 Usuario` | `401 NAO_AUTENTICADO` / `SESSAO_EXPIRADA` |

```yaml
LoginRequest:  # required: email, senha
  email: string, format email, maxLength 254   # normalizado (trim, minúsculas)
  senha: string, minLength 1, maxLength 200    # nunca devolvida, nunca logada
TokenResponse: { accessToken: string, tokenType: "Bearer", expiraEm: integer (segundos), usuario: Usuario }
Usuario:       { id: int64, email: string }
```
JWT: HS256; claims `iss=autorizador-processo`, `sub`=id, `email`, `iat`, `exp` (= `iat` + `JWT_TTL_MINUTOS`, padrão 60). Sem refresh token (PEN-10).

| `code` | HTTP | Quando | `detail` (pt-BR) |
|---|---|---|---|
| `CREDENCIAIS_INVALIDAS` | 401 | e-mail inexistente, senha errada ou usuário inativo (indistinguíveis) | "E-mail ou senha incorretos." |
| `NAO_AUTENTICADO` | 401 | token ausente, malformado, assinatura/`iss` inválidos | "Autenticação necessária." |
| `SESSAO_EXPIRADA` | 401 | token válido porém expirado | "Sua sessão expirou. Entre novamente." |
| `MUITAS_TENTATIVAS` | 429 | 5 falhas em 15 min por (e-mail, IP) | "Muitas tentativas. Tente novamente em N minutos." |
| `ACESSO_NEGADO` | 403 | reservado (sem papéis na v1; PEN-11) | "Você não tem permissão para esta ação." |
Respostas `401` incluem `WWW-Authenticate: Bearer`. O frontend decide por `code`; `403` de negócio (`AUTORIZACAO_NEGADA`, `PROCEDIMENTO_DUPLICADO`) continua distinto de `ACESSO_NEGADO`.
Variáveis: `JWT_SECRET` (obrigatória), `JWT_TTL_MINUTOS`, `AUTH_BOOTSTRAP_EMAIL`, `AUTH_BOOTSTRAP_SENHA`.
