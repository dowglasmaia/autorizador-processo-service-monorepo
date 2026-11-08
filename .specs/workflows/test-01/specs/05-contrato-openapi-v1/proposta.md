# Contrato OpenAPI v1

Etapa 5 de 31 · Fase 1 · derivada da `SPEC-05` (`.specs/02-backend/SPEC-05-api-contract-v1.md`).

## Objetivo
Criar o contrato OpenAPI 3.1 por módulo (`procedimento`, `autorizacao`, `painel`), com os erros comuns RFC 9457 em um único lugar, e a geração das interfaces Spring (`interfaceOnly`) **antes** de qualquer controller ou tela. O módulo `procedimento` nasce com o contrato base, sem os campos NEW-01 (entram na SPEC-12). Nenhum caso de uso é implementado.

## Ponto de partida (conferido contra o repositório; a etapa 4 ainda está em andamento)
- `api-autorizador-processo/` existe (entregue pela etapa 3). `src/main/resources/openapi/` contém só `openapi.yaml` placeholder (`paths: {}`, versão 1.0.0).
- O `pom.xml` tem **uma** execução `generate-openapi` do `openapi-generator-maven-plugin` 7.26.0 sobre `openapi/openapi.yaml`, com pacotes `org.maia.autorizador.generated.api|model`, `interfaceOnly`, `skipDefaultInterface`, `useSpringBoot4`, `useJackson3`, `openApiNullable=false`, `useTags`. Existe `generated/package-info.java`.
- Não existem `openapi/modules/` nem `openapi/common/`. A etapa 4 mexe em dependências de banco e no `pom.xml`: esta etapa deve reconferir o `pom.xml` antes de editar.
- `.specs/06-execution-plan/` não existe (desvio a registrar na evidência).
- A fonte do contrato é `01-architecture/API_CONTRACTS.md` §1–§3 (a aprovação da skill já está dada pela spec); em divergência, o YAML vence depois de gerado.

## Escopo
1. Inspecionar o `pom.xml` e fixar o layout `openapi/modules/*.yaml` + `openapi/common/responses.yaml` (T1).
2. `common/responses.yaml`: `ProblemDetail` (`type`, `title`, `status`, `detail`, `instance`, `code`, `timestamp`, `errors[{campo,mensagem}]`) e respostas padronizadas `400`, `403`, `404`, `500` (T2). `ProblemDetail` existe só aqui.
3. `modules/procedimento.yaml`: `listarProcedimentos` (`GET /procedimentos?q&page&size&sort`), `cadastrarProcedimento` (`POST`, `201` + `Location`), `obterProcedimento` (`GET /procedimentos/{id}`); esquemas `Sexo`, `ProcedimentoRequest`, `Procedimento`, `PaginaProcedimentos` (T3).
4. `modules/autorizacao.yaml`: `solicitarAutorizacao` (`POST /autorizacoes`), `listarAutorizacoes` (`GET /autorizacoes?page&size`); `AutorizacaoRequest`, `Autorizacao`, `PaginaAutorizacoes` (T4).
5. `modules/painel.yaml`: `obterResumoPainel` (`GET /painel/resumo`); `ResumoPainel` (T5).
6. Uma execução do gerador por módulo, IDs determinísticos, pacotes e saídas isolados, `spring`, `interfaceOnly=true`; configuração preparada para aceitar um quarto módulo (`auth`, SPEC-22) (T5b, T6).
7. Validação com `validate.mjs` e `./mvnw generate-sources compile` (T7).

Base de rota `/api/v1`, JSON UTF-8, `camelCase`, datas ISO-8601 UTC. Paginação: `page` ≥0 (padrão 0), `size` 1..200 (padrão 20), `sort` só `numProcedimento,asc|desc` em procedimentos. Cada erro referencia `../common/responses.yaml#/components/responses/...`; os `code` do catálogo (§3) ficam como exemplos/descrição, sem copiar `ProblemDetail`.

## Fora do escopo (fica para outras etapas)
- Controllers, serviços, casos de uso, persistência: etapas 8 a 10 (módulos) e seguintes.
- Tratamento global de erros, validação e CORS: etapa 6 (SPEC-06).
- Módulo `auth`, `bearerAuth` e respostas `401/429`: etapa 7 (SPEC-22). Aqui não se declara `security`.
- Campos NEW-01 (`nome`, `categoria`, etc.) em `procedimento`: SPEC-12.
- Tipos TypeScript do frontend e verificação de desvio: etapas 14 e 27.
- Dockerfile, CI, Compose.

## Critérios de aceitação
- `./mvnw generate-sources` gera interfaces em pacotes separados por módulo, sem duplicidade de rota ou `operationId`; `ProblemDetail` é único.
- `validate.mjs` (ou, se inviável como na etapa 3, validação equivalente registrada) sem falhas; `./mvnw -q generate-sources compile` com sucesso.
- Conferência manual do YAML contra `API_CONTRACTS.md` §1–§3: 6 operações, 3 módulos, status e nomes iguais.
- `./mvnw verify` continua verde (ArchUnit, Modulith; a etapa 4 define se exige Docker); nenhum código de negócio; nenhum nome "legado/legacy".

## Riscos
- `validate.mjs` pode exigir artefatos removidos na etapa 3 (módulo `example`): registrar a inviabilidade e substituir por validação alternativa.
- Interfaces geradas em `org.maia.autorizador.generated.*` podem violar regras do ArchUnit/Modulith: ajustar pacotes ou regras de forma mínima.
- O gerador 7.26.0 com Boot 4/Jackson 3 gera `Set` para listas com `uniqueItems`: validar compilação.
- Conflito de `pom.xml` com mudanças da etapa 4: reler o arquivo no início.

## Decisões tomadas sem consulta
- **Aprovação da skill:** a proposta de `API_CONTRACTS.md` já vale como aprovação (SPEC-05 §1); nada fora dele é gerado.
- **[REVISAR] `openapi.yaml` placeholder:** a skill pede para não criar um segundo contrato. Como o arquivo tem `paths: {}` e foi só um esqueleto da etapa 3, a decisão provisória é removê-lo (e a execução `generate-openapi`) em favor do layout modular; se algo da etapa 4 o referenciar, manter e registrar.
- **Sem `security` nem `401`:** ficam para a SPEC-22, conforme T5b.
- **Pacotes por módulo:** provisório `org.maia.autorizador.generated.<modulo>.api|model`, para separar o gerado do código escrito; reavaliar se o ArchUnit reclamar.
- **Etapa 4 em curso:** a proposta foi escrita sem a entrega dela e será revisada contra o `pom.xml` final.
