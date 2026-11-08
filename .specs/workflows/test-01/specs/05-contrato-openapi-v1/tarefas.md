# Tarefas — Contrato OpenAPI v1

## 1. Preparação
- [ ] 1.1 Ler `INDEX.md`, `memory/`, `shared/como-executar.md`, `API_CONTRACTS.md` §1–§3, `BASELINE_COMPORTAMENTO_ORIGINAL.md` e as referências `contract-conventions.md`, `modular-contracts.md`, `input-format.md` da skill `openapi-rest-contract`; registrar o desvio de `.specs/06-execution-plan/`
- [ ] 1.2 Reconferir o `pom.xml` (já com as dependências da etapa 4) e `src/main/resources/openapi/`; configurar `JAVA_HOME=C:\jdk-25.0.2` (T1)
- [ ] 1.3 Confirmar o layout `openapi/modules` + `openapi/common` e decidir o destino do `openapi.yaml` placeholder, registrando em decisões (T1)

## 2. Erros comuns
- [ ] 2.1 Criar `openapi/common/responses.yaml` com `ProblemDetail` (`code`, `timestamp`, `errors[]`) (T2)
- [ ] 2.2 Adicionar as respostas padronizadas `400`, `403`, `404` e `500` com exemplos dos `code` do catálogo (T2)

## 3. Contratos por módulo
- [ ] 3.1 Criar `modules/procedimento.yaml` com `listarProcedimentos` e seus parâmetros `q`, `page`, `size`, `sort` (T3)
- [ ] 3.2 Acrescentar `cadastrarProcedimento` (`201` com cabeçalho `Location`) e `obterProcedimento` (T3)
- [ ] 3.3 Definir `Sexo`, `ProcedimentoRequest`, `Procedimento` e `PaginaProcedimentos`, sem campos NEW-01 (T3)
- [ ] 3.4 Criar `modules/autorizacao.yaml` com `solicitarAutorizacao` e `listarAutorizacoes` (T4)
- [ ] 3.5 Definir `AutorizacaoRequest`, `Autorizacao` e `PaginaAutorizacoes` (T4)
- [ ] 3.6 Criar `modules/painel.yaml` com `obterResumoPainel` e `ResumoPainel` (T5)

## 4. Geração das interfaces
- [ ] 4.1 Configurar no `pom.xml` uma execução do gerador por módulo (IDs determinísticos, pacotes e saídas isolados, `interfaceOnly`) preservando o restante (T6)
- [ ] 4.2 Garantir que a configuração aceite um quarto módulo (`auth`) sem refatorar, sem contratá-lo (T5b)
- [ ] 4.3 Ajustar ArchUnit/Modulith, se necessário, para aceitar os pacotes gerados, sem enfraquecer as regras

## 5. Validação
- [ ] 5.1 Rodar `validate.mjs` da skill; se inviável, registrar a causa e usar validação equivalente (OpenAPI 3.1, `$ref`, `operationId` únicos) (T7)
- [ ] 5.2 Rodar `./mvnw -q generate-sources compile` e inspecionar os pacotes gerados por módulo (T7)
- [ ] 5.3 Conferir o YAML contra `API_CONTRACTS.md` §1–§3 (operações, status, nomes) e confirmar `ProblemDetail` único
- [ ] 5.4 Rodar `./mvnw verify` e corrigir falhas; conferir que `git grep -il "legado|legacy" -- api-autorizador-processo` retorna vazio
- [ ] 5.5 Registrar evidência por tarefa, resumo na spec e `git mv` da spec para `.specs/archive/`
