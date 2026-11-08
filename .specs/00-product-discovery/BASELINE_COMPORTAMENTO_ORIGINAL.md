# BASELINE DE COMPORTAMENTO — versão original da API

> Evidência **executada**: o código original (Spring Boot 2.2.6, Java 8, perfil `test`/H2) foi iniciado em container `maven:3.6.3-jdk-8` em 2026-11-08 e sondado com `curl`. Esta é a fonte de verdade do que "preservar" significa. Substitui as marcas **[INFERIDO]** de `.specs/design/DESIGN_SYSTEM_DISCOVERY.md` que são confirmadas ou refutadas abaixo.
>
> Convenção de evidência: **[EXEC]** observado em execução · **[CÓDIGO]** lido no código · **[INFERÊNCIA]** dedução não testada · **[DECISÃO]** escolha desta modernização.
> Nomenclatura: a versão anterior é chamada de "versão original". O termo "legado" não deve aparecer em código, pacotes, pastas, testes ou scripts do backend.

## 1. Casos observados [EXEC]

| # | Requisição | Resposta observada |
|---|---|---|
| B01 | `GET /procedimentos` (carga inicial do perfil `test`) | `200`; 3 itens (`1234`,`4567`,`6789`); `sexos` como nomes (`"F"`,`"M"`); `idades` **sem ordem garantida** (`[20,10]`) |
| B02 | `POST /procedimentos` `{"numProcedimento":9999,"sexos":[1,2],"idades":[18,30]}` | `201`, sem corpo, `Location: http://host/procedimentos/{id}` |
| B03 | `POST /procedimentos` com `"sexos":["M","F"]` | `400` padrão Spring (`Cannot deserialize ... Integer from String "M"`) → **assimetria entrada(1/2) × saída(M/F) CONFIRMADA** |
| B04 | `POST /procedimentos` com `numProcedimento` já existente | `403` `{timestamp(ms), status:403, error:"Não Autorizado", message:"Procedimento já Cadastrado na Base de Dados : Nº 9999", path}` |
| B05 | `POST /procedimentos` sem `numProcedimento` | `400` padrão Spring com `errors[].defaultMessage = "informe o número do procedimento para prosseguir com a operação."` |
| B06 | `POST /procedimentos` com `"sexos":[3]` | **`201` (aceito)** — nenhuma validação de código de sexo |
| B07 | `GET /procedimentos` após B06 | **`500`** `Could not write JSON: Código inválido :3` → **um único cadastro inválido quebra a listagem inteira** (defeito grave) |
| B08 | `POST /procedimentos` JSON malformado | `400` padrão Spring |
| B09 | `POST /autorizadores/4567?idade=20&sexo=F` | `200`, corpo vazio |
| B10 | `POST /autorizadores/4567?idade=30&sexo=m` (minúsculo) | `200` → comparação **case-insensitive CONFIRMADA** |
| B11 | `POST /autorizadores/1234?idade=30&sexo=M` | `403` `message:"Operação não Autorizada, Sexo e/ou Idade não aderentes ao Procedimento Solicitado."` |
| B12 | `POST /autorizadores/55555?idade=30&sexo=M` (procedimento inexistente) | **`500`** `NullPointerException` em `SolicitarProcedimentoService.save:37`; o corpo expõe **stack trace** (CONFIRMADO) |
| B13 | `POST /autorizadores/4567?idade=20` (sem `sexo`) | `400` `Required String parameter 'sexo' is not present` (+ `trace`) |
| B14 | `POST /autorizadores/abc?idade=20&sexo=F` | `400` `Failed to convert ... Integer` (+ `trace`) |
| B15 | `GET /autorizadores` após B09/B10 | `200`; **dois registros para o mesmo procedimento 4567** → o `@OneToOne` **NÃO** impede várias autorizações (hipótese 10.2 do discovery anterior **REFUTADA**) |
| B16 | `OPTIONS` com `Origin: http://localhost:4200` | `403 Invalid CORS request` → **sem CORS CONFIRMADO** |
| B17 | `GET /actuator/health` | `404` → sem actuator/health |

Item de B15: formato `[{"id":1,"procedimento":{"id":2,"numProcedimento":4567,"sexos":["M","F"],"idades":[20,30]},"permitido":true}, ...]`.

## 2. Esquema relacional gerado [EXEC — DDL do Hibernate]

```text
procedimento(id bigint identity PK, numero_procedimento int NOT NULL, UNIQUE(numero_procedimento))
sexos(procedimento_id bigint NOT NULL FK, sexos int)            -- sem PK, sem UNIQUE
idades(procedimento_id bigint NOT NULL FK, idades int)          -- sem PK, sem UNIQUE
solicitar_procedimento(id bigint identity PK, permitido boolean NOT NULL, procedimento_id bigint NULL)  -- SEM unique, SEM FK explícita
revinfo(rev int identity PK, revtstmp bigint)
procedimento_audit / solicitar_procedimento_audit / sexos_aud / idades_aud   -- Envers
```

Observações: `permitido` é sempre `true` na prática [CÓDIGO]; não há data, paciente, idade, sexo ou solicitante persistidos; negativas não deixam rastro (RN8).

## 3. Divergências entre o original e a modernização (DIV)

Cada DIV é **decisão registrada** — ver `01-architecture/DECISIONS.md` (ADR/DIV) e `API_CONTRACTS.md`. O que não está aqui é **preservado** exatamente.

| ID | Original | Modernização | Motivo |
|---|---|---|---|
| DIV-01 | Procedimento inexistente → `500` (NPE) | `404` `PROCEDIMENTO_NAO_ENCONTRADO` | B12 é defeito, nenhum cliente pode depender dele; o handler de 404 já existia sem uso |
| DIV-02 | `sexos` entra como `1/2`, sai como `M/F`; valores inválidos aceitos e quebram o `GET` | `sexos` é `M`/`F` na entrada e na saída; valor inválido → `400` | B03, B06, B07 |
| DIV-03 | `POST /procedimentos` → `201` sem corpo; `Location` aponta para rota inexistente | `201` com corpo do recurso e `GET /procedimentos/{id}` existente | B02; permite à UI exibir sem refetch |
| DIV-04 | Corpo de erro heterogêneo (StandardError / padrão Spring com `trace`) | RFC 9457 `ProblemDetail` + `code` estável + `errors[]`; **sem stack trace** | B05, B12–B14; UI distingue os dois `403` por `code` |
| DIV-05 | `POST /autorizadores/{num}?idade=&sexo=` → `200` vazio | `POST /autorizacoes` com corpo JSON `{numProcedimento, idade, sexo}` → `201` com o registro | dados do paciente fora da URL (logs/proxy); validação tipada. **Resultado de negócio (autorizado/negado) idêntico** |
| DIV-06 | Sem validação de conteúdo | `numProcedimento > 0`; `idade` inteira `0..150`; `sexos ⊆ {M,F}`; listas vazias **continuam permitidas** (RN9) | integridade mínima; limite 150 é premissa PRM-05 |
| DIV-08 | `sexo` fora de `M/F` (ex.: "Masculino") na solicitação → negação `403` | `400 VALIDACAO_INVALIDA` (aceita `m`/`f` minúsculos, RN7) | o campo é tipado; "nunca casa" deixa de ser um resultado silencioso |
| DIV-07 | Listas sem paginação | Listas paginadas (`page`,`size`, padrão 20, máx. 200) e ordenação estável por `numProcedimento` / `id` | determinismo e escala |

**Preservado integralmente:** RN1–RN10 (ver `PRODUCT_CONTEXT.md` §7): unicidade do número e `403` na duplicidade; autorização exige sexo **E** idade por **igualdade exata**; sexo case-insensitive; só concedidas são gravadas; várias autorizações por procedimento; auditoria Envers das duas entidades; `403` para negativa de negócio.

## 4. Funcionalidades NOVAS (inexistentes na versão original)

Instrução do responsável pelo produto (2026-11-08): o backend não possui os dados da página **Cadastrar procedimento** do protótipo; eles entram como **nova feature de backend** (não como campos "conceituais" no frontend). Não são divergências: são acréscimos aditivos.

| ID | Nova feature | Spec |
|---|---|---|
| NEW-01 | Catálogo estendido do procedimento: `nome`, `codigoTuss`, `categoria`, `especialidade`, `tipoCobertura`, `quantidadeMaxima`, `periodo`, `exigeAutorizacaoPrevia`, `exigeJustificativaMedica` | SPEC-12 (backend) + SPEC-17 (frontend) |
| NEW-02 | Resumo do painel calculado no servidor | SPEC-09 |
| NEW-03 | `GET /procedimentos/{id}` | SPEC-07 |
| NEW-04 | Autenticação: login por e-mail/senha com JWT; todas as rotas protegidas | SPEC-22 (backend) + SPEC-23 (frontend) |

Regra de contenção de escopo: os novos campos são **dados de catálogo**. Eles **não alteram a decisão de autorização** (RN5–RN7 continuam sendo só sexo E idade exata) até que o produto defina regras para cobertura/quantidade/prévia (pendência PEN-02).
