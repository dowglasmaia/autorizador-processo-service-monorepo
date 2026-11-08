# Catálogo estendido

Etapa 9 de 31 · Fase 2 · derivada da `SPEC-12` (`.specs/02-backend/SPEC-12-procedimento-catalogo-estendido.md`, nova feature NEW-01). Proposta revisada contra o código da etapa 8, já entregue.

## Objetivo
Acrescentar de forma **aditiva** ao módulo `procedimento` os dados de cadastro do protótipo: `nome`, `codigoTuss`, `categoria`, `especialidade`, `tipoCobertura`, `quantidadeMaxima`, `periodo`, `exigeAutorizacaoPrevia` e `exigeJustificativaMedica`, com migração `V4`, contrato atualizado e testes. São **dados de catálogo**: `Procedimento.avaliar` e a decisão de autorização não mudam (ADR-10, PEN-02).

## Ponto de partida (conferido contra o código entregue pela etapa 8)
- Existem `V1__schema_base.sql`, `V2__auditoria.sql` e `V3__usuario.sql`; o seed local `db/seed/V1000__seed_local.sql` insere só `numero_procedimento`, sexos e idades (1234, 4567, 6789).
- `openapi/modules/procedimento.yaml` tem `ProcedimentoRequest` (obrigatório só `numProcedimento`), `Procedimento`, `PaginaProcedimentos` e `Sexo`; `autorizacao.yaml` referencia `Procedimento` de `procedimento.yaml`, então herda os campos novos.
- O módulo `procedimento` **já existe** (etapa 8): `domain/model/Procedimento` (`create(int, Collection<Sexo>, Collection<Integer>)`, `restore`, `avaliar`), `Sexo`, `Pagina`; casos de uso `CadastrarProcedimentoUseCase`, `ListarProcedimentosUseCase`, `ObterProcedimentoUseCase`; porta `ProcedimentoRepository`; `ProcedimentoEntity` (`@Audited`, tabela `procedimento`, pacote-privada) com `ProcedimentoRepositoryAdapter`; `ProcedimentoController`; testes `ProcedimentoTest`, `Cadastrar...UseCaseTest`, `ProcedimentoRepositoryFake`, `ProcedimentoControllerTest`, `ProcedimentoApiTest`, `ProcedimentoPersistenciaTest`, `ProcedimentoConcorrenciaDesempenhoTest`. Esta etapa os estende; a assinatura de `create`/`restore` muda de forma aditiva e os testes existentes são ajustados só na chamada, sem mudar expectativas.
- A auditoria V2 usa `procedimento_audit`, `autorizacao_audit` e `procedimento_sexo_audit`; só `procedimento_audit` recebe as colunas novas.
- `tools/parity/cases.json` e `replay.mjs` existem desde a etapa 2. `.specs/06-execution-plan/` não existe (desvio a registrar).
- No protótipo (`.specs/design/prototype/Autorizador.dc.html`) as listas são: categorias Exames de imagem, Consultas, Cirurgias, Terapias, Exames laboratoriais; especialidades Cardiologia, Ortopedia, Pediatria, Ginecologia, Neurologia. O protótipo não traz nome/categoria para os 3 procedimentos do seed.

## Escopo
1. Contrato `procedimento.yaml` (skill `openapi-rest-contract`, módulo existente): novos campos em `ProcedimentoRequest` e `Procedimento`; enums `Categoria`, `Especialidade`, `TipoCobertura`, `Periodo`; `nome` e `categoria` obrigatórios no request; sem operação nova nem remoção; validar e regenerar.
2. `V4__catalogo_estendido.sql`: colunas nullable em `procedimento` com CHECKs (lista de categoria, cobertura, período, `quantidade_maxima >= 1`, `quantidade_maxima IS NULL OR periodo IS NOT NULL`) e espelho nas tabelas `*_audit`, conforme `DATABASE_DESIGN.md`.
3. Domínio: tipos `Categoria`, `Especialidade`, `TipoCobertura`, `Periodo`; invariantes (`nome` 1..200 após trim; `quantidadeMaxima` e `periodo` andam juntos); `create` aceita os novos dados; `avaliar` inalterado, com teste de regressão explícito.
4. Casos de uso, entidade e adaptador: persistir e ler os campos; padrões `tipoCobertura=PLANO` e booleanos `false` na criação; linhas antigas saem com `null`.
5. Controller: mapear request/response estendidos; `VALIDACAO_INVALIDA` com `errors[]` por campo (`nome`, `categoria`, `periodo`/`quantidadeMaxima`).
6. Testes: domínio, migração sobre dados existentes (contagens iguais antes/depois), `@WebMvcTest`, ponta a ponta, auditoria dos novos campos e regressão completa da suíte da etapa 8 sem alterar expectativas.
7. Seed local: manter os campos nulos (o protótipo não define valores para 1234/4567/6789) e documentar.
8. `tools/parity/cases.json`: casos NEW-01 marcados `novo`, fora da paridade original.

## Fora do escopo (fica para outras etapas)
- Módulos `autorizacao` e `painel` e qualquer mudança na decisão de autorização.
- Formulário de cadastro em 4 etapas e exibição no frontend: SPEC-17.
- Fechamento da paridade (SPEC-31), detecção de desvio de contrato (SPEC-32) e migração de dados (SPEC-50).
- Update/delete de procedimento e novos endpoints.

## Critérios de aceitação
- *Dado* a API com V4, *quando* `POST /procedimentos` com `nome`, `categoria`, `tipoCobertura=REGRAS`, `quantidadeMaxima=2`, `periodo=ANO`, *então* `201` e o `GET` devolve os mesmos valores.
- *Quando* o `POST` vem sem `nome` (ou sem `categoria`) → `400 VALIDACAO_INVALIDA` com `errors[]` do campo; `quantidadeMaxima=2` sem `periodo` (ou o inverso) → `400`.
- *Dado* o procedimento 1234 anterior, *então* seus campos novos são `null` e `POST /autorizacoes` continua decidindo só por sexo e idade.
- Sem `tipoCobertura` o padrão é `PLANO`; booleanos omitidos viram `false`.
- V4 aplicada sobre banco com dados de V1–V3 não perde linhas; auditoria registra os novos campos.
- `./mvnw verify` verde (incluindo `ArchitectureTest`, `ModularityTests` e a regressão da etapa 8); Testcontainers exige Docker, senão registrar bloqueio; nenhum nome "legado/legacy".

## Riscos
- Mudar a assinatura de `Procedimento.create` quebra chamadas dos testes da etapa 8; usar sobrecarga/valor de catálogo opcional para não alterar expectativas.
- `*_audit` desalinhadas com a entidade `@Audited` (Envers) quebram a gravação; espelhar as colunas exatamente.
- Alterar o contrato sem regenerar quebra a compilação; manter a mudança aditiva.
- Docker indisponível bloqueia os testes de persistência e de migração.

## Decisões tomadas sem consulta
- **Contrato aprovado:** a SPEC-12 e `API_CONTRACTS.md` §2 valem como aprovação; nenhuma operação nova.
- **Listas fechadas:** os enums seguem `API_CONTRACTS.md` (valores em `SNAKE_CASE`), derivados das listas do protótipo (PRM-06).
- **Seed sem valores novos:** o protótipo não define nome/categoria para os 3 itens; permanecem nulos e a decisão é documentada.
- **[REVISAR] Obrigatoriedade só na API:** `nome` e `categoria` obrigatórios apenas na criação; o banco aceita nulos para linhas antigas.
- **[REVISAR] Padrões no servidor:** `tipoCobertura=PLANO` e booleanos `false` aplicados pelo backend quando omitidos (a UI pré-seleciona `true` na SPEC-17).
- **Escopo do contrato aninhado:** `Autorizacao.procedimento` herda os campos por referência; nenhuma mudança em `autorizacao.yaml`.
- **Lote 1 — domínio com `DadosCatalogo`:** em vez de ampliar a lista de parâmetros, `Procedimento.create/restore` ganharam sobrecargas com um registro `DadosCatalogo` (`VAZIO` para o que é anterior); as assinaturas antigas seguem valendo e a suíte da etapa 8 não mudou. `nome` nulo é aceito no domínio (obrigatoriedade é da API, tarefa 6.2).
- **Lote 1 — resposta do contrato:** campos novos de `Procedimento` ficam opcionais (`$ref`/tipos simples, sem `oneOf` com `null`), porque o gerador usa `openApiNullable=false`; linhas antigas saem sem o campo/`null`.
- **Lote 1 — pendências conhecidas:** `nome`/`categoria` agora são obrigatórios no `ProcedimentoRequest`, então os testes de API da etapa 8 que fazem POST sem eles só serão ajustados (apenas na carga enviada) nos lotes 5–7; `V4` ainda não foi executada em PostgreSQL (tarefa 7.1, Testcontainers).
- **Desvio registrado:** `.specs/06-execution-plan/` não existe.
- **Lote 2 — borda sem domínio:** o `ArchitectureTest` proíbe o controller de usar `domain.model`; por isso o caso de uso expõe `ObterProcedimentoUseCase.Catalogo` (enums como texto, nome do valor) em `executar(..., Catalogo)` e em `Resultado.catalogo()`. A conversão para os enums de domínio e os padrões (`PLANO`, booleanos `false`) ficam no `CadastrarProcedimentoUseCase`; as assinaturas antigas continuam valendo.
- **Lote 2 — erros por campo:** novo `CampoInvalidoException` (shared/error) tratado no `GlobalExceptionHandler` como `400 VALIDACAO_INVALIDA` com `errors[]` do campo. O controller o usa para `nome` só com espaços, `periodo` ausente e `quantidadeMaxima` ausente; `nome`/`categoria` ausentes e `quantidadeMaxima < 1` saem do Bean Validation do contrato.
- **Lote 2 — 7.3 sem `/autorizacoes`:** o módulo `autorizacao` só existe na etapa 10; a garantia de que a decisão não mudou foi verificada com `Procedimento.avaliar` sobre o seed 1234 persistido (campos novos nulos).
- **Lote 2 — testes existentes:** ajustados só nas cargas (acréscimo de `nome`/`categoria`), nas chamadas do mock (`executar` com 4 argumentos) e na contagem de migrações (`MigracaoTest`: 4; `SeedLocalTest`: versões `1,2,3,4,1000`), efeito direto da V4.
- **Lote 2 — migração:** `MigracaoCatalogoEstendidoTest` usa Flyway direto num contêiner próprio (migra até V3, insere dados, migra até V4). A auditoria dos novos campos (7.4) fica para o lote seguinte.
- **Lote 3 — evidência (resumo):** `./mvnw verify` verde (170 testes, 0 falhas, incluindo `ArchitectureTest` com 21 regras, regra de nome "legado/legacy" e a suíte da etapa 8). 7.4: `AuditoriaCatalogoTest` confirma os 9 campos em `procedimento_audit` (com e sem catálogo). 8.1: seed mantido sem valores novos, decisão comentada em `V1000__seed_local.sql`. 8.2: casos N01–N04 (`novo`, `apenasV1`) em `cases.json`; B02 e B04 receberam `nome`/`categoria` só na carga v1 (obrigatórios agora); o replay HTTP não foi rodado (exige servidor e Postgres ativos), fica para a SPEC-31.
- **Lote 3 — `check-architecture.mjs`:** o script da skill não existe no repositório; a verificação equivalente é o `ArchitectureTest` (0 violações). Resumo conforme `como-executar.md`: todas as tarefas 1.1–9.2 concluídas; o `git mv` da spec para `.specs/archive/` é feito pelo devkit no passo de arquivamento.
