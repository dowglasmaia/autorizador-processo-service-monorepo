# Desvio de contrato

Etapa 27 de 31 · Fase 4 · derivada da `SPEC-32` (`.specs/04-integration/SPEC-32-contract-drift-check.md`), com referências à `SPEC-05` (contrato v1) e à `SPEC-16` (núcleo HTTP e tipos). Escrita antecipadamente e revisada contra a base entregue pela etapa 26 (paridade com o baseline, SPEC-31 já arquivada).

## Objetivo
Garantir que o frontend nunca fique defasado em relação ao OpenAPI: os tipos regenerados devem ser idênticos aos versionados e as respostas reais da API devem respeitar o contrato. Sem mudar o contrato.

## Ponto de partida (conferido após a etapa 26)
- Contrato em `api-autorizador-processo/src/main/resources/openapi/` (`common/` e `modules/` com `auth`, `autorizacao`, `painel`, `procedimento`). Há **8 operações** (`autenticar`, `obterUsuarioAtual`, `cadastrarProcedimento`, `listarProcedimentos`, `obterProcedimento`, `solicitarAutorizacao`, `listarAutorizacoes`, `obterResumoPainel`), não as 6 citadas na SPEC-32; vale o contrato real.
- Erros RFC 9457 com `code` em `common/responses.yaml` (11 exemplos: `VALIDACAO_INVALIDA`, `CORPO_INVALIDO`, `PARAMETRO_INVALIDO`, `PROCEDIMENTO_DUPLICADO`, `AUTORIZACAO_NEGADA`, `PROCEDIMENTO_NAO_ENCONTRADO`, `ERRO_INTERNO`, `CREDENCIAIS_INVALIDAS`, `NAO_AUTENTICADO`, `SESSAO_EXPIRADA`, `MUITAS_TENTATIVAS`), não os 5 citados na SPEC-32; a lista exata é conferida na execução.
- Geração de tipos já existe: `app-autorizador-processo/scripts/gerar-api-types.mjs` (`npm run api:types`) escreve `src/app/shared/api/api-types.ts` com `openapi-typescript`. Falta a verificação de desvio.
- Backend tem testes `*ApiTest` por módulo (Testcontainers) e `ArchitectureTest`; não há validação das respostas contra o esquema OpenAPI.
- A raiz tem `README.md`, `compose.yaml` e `tools/parity/` (etapa 26), mas não `scripts/` nem `.github/workflows` (CI é a etapa 28/SPEC-40); não há tag/branch base de contrato além de `v0-original` (sem OpenAPI).
- `.specs/06-execution-plan/` não existe; desvio registrado na evidência.

## Escopo
1. `scripts/check-contract.mjs` na raiz: regenera os tipos em diretório temporário e compara com `api-types.ts` versionado; sai com código ≠ 0 apontando o arquivo divergente. Para reutilizar a geração sem duplicá-la, o gerador existente passa a aceitar um caminho de saída opcional (comportamento padrão inalterado).
2. Teste de conformidade no backend: chama cada uma das 8 operações com a aplicação real (Testcontainers) e valida status e corpo contra o esquema do OpenAPI, cobrindo todos os `code` de erro do contrato (os 11 listados). Preferência por validação via testes do backend; se for necessária biblioteca de validação de esquema (ex.: `swagger-request-validator`), justificá-la em `doc.md`/README.
3. Verificação de compatibilidade: comparar o contrato atual com uma referência base e falhar em mudança incompatível (remoção, renomeação, nova obrigatoriedade) dentro de `/api/v1`; mudanças aditivas passam. Referência base: cópia do contrato versionada no commit de entrega da etapa anterior (ver decisões).
4. Documentar no `README.md` raiz como regenerar tipos e interpretar as falhas.

## Fora do escopo
- Qualquer alteração do contrato OpenAPI ou dos DTOs.
- Integração ao CI: fica para a etapa 28 (SPEC-40, CI independente por projeto).
- Novas ferramentas além de `openapi-typescript` e, no máximo, uma biblioteca de validação de esquema justificada.
- Paridade com o baseline (etapa 26) e testes e2e (etapa 25).

## Critérios de aceitação
- `node scripts/check-contract.mjs` → exit 0 no estado atual; remover um campo do YAML sem regenerar tipos → exit ≠ 0 apontando `api-types.ts`; adicionar campo opcional e regenerar → exit 0 (demonstrado e revertido, sem deixar o contrato alterado).
- Teste de conformidade no backend cobre as 8 operações e todos os `code` de erro, e falha se uma resposta divergir do esquema.
- A verificação de compatibilidade falha em remoção/renomeação/obrigatoriedade nova e passa em mudança aditiva (demonstrado e revertido).
- `./mvnw verify` verde em `api-autorizador-processo`; `npm run lint`, `npm run build` e `npm test` verdes em `app-autorizador-processo`.
- Se o Docker estiver indisponível, bloqueio registrado na tarefa do teste de conformidade, que não é marcada como concluída.
- README raiz explica regeneração e leitura das falhas; nenhum nome "legado/legacy" no código do backend.

## Decisões tomadas sem consulta
- Conformidade de respostas feita no backend (opção prevista pela SPEC-32), evitando nova dependência no frontend. Biblioteca de validação só se inevitável. **[REVISAR]**
- Referência base da compatibilidade: como não há tag do contrato v1, usa-se um instantâneo do contrato versionado em pasta própria (ex.: `tools/contract/baseline/`, ao lado de `tools/parity/`), atualizado conscientemente a cada mudança aditiva aprovada. Alternativa (comparar com branch/tag git) descartada por exigir histórico no CI. **[REVISAR]**
- Comparação de compatibilidade implementada em script Node sem nova ferramenta (diff estrutural de paths, propriedades e `required` do YAML já resolvido); caso falte biblioteca de YAML, reutiliza-se a que o `openapi-typescript` já traz.
- Número de operações segue o contrato real (8), não o texto da SPEC-32 (6).
- Execução do script no CI fica para a SPEC-40 (etapa 28), conforme a SPEC.

### Registradas na execução do lote 1
- **Ausência de `.specs/06-execution-plan/`** confirmada (referência do índice ignorada). `API_CONTRACTS.md` está em `.specs/01-architecture/` (não em `shared/`). Docker 29.9.2 disponível.
- **Estado após a etapa 26 conferido:** o contrato tem 8 operações e o enum `code` tem **12** valores (a proposta dizia 11 exemplos; `ACESSO_NEGADO` não tem exemplo nas respostas, mas está no enum). Cobertura: 10 por rota, `ERRO_INTERNO` com `@MockitoBean` num caso de uso, e `ACESSO_NEGADO` chamando o `ProblemAccessDeniedHandler` direto (nenhuma rota produz 403 de segurança, pois não há papéis). Um teste falha se o enum ganhar um `code` fora dessa lista.
- **Mecanismo de validação (3.1): validador mínimo no próprio teste** (`contrato/ContratoOpenApi`, SnakeYAML já transitivo do Spring Boot), sem nova dependência, em vez de `swagger-request-validator`. Resolve `$ref` entre arquivos, valida status declarado, cabeçalhos declarados, content-type e corpo; **propriedade não declarada no contrato falha**, e palavra-chave de esquema desconhecida lança erro para não passar sem verificação. **[REVISAR]**
- **Achado:** o contrato descreve campos opcionais do catálogo como "ausente (nulo)", mas o esquema não os marca como `nullable` e a API os serializa como `null` (ex.: `Procedimento.nome`, `codigoTuss`, `periodo`). O contrato não pode ser alterado nesta etapa; o validador aceita `null` em propriedade **opcional** como ausência. Se for desejável impor ausência real, é mudança de contrato/serialização fora desta etapa. **[REVISAR]**
- `check-contract.mjs` usa o gerador do app como subprocesso (`gerar-api-types.mjs <saida>`), comparando texto normalizado (CRLF→LF).

### Registradas na execução do lote 2
- **3.4 (adulteração):** `PainelController` devolveu temporariamente `202` em vez de `200`; `ContratoRespostasApiTest#obterResumoPainel` falhou (`but was: 202`, apontando `get /painel/resumo`). Controlador revertido (`git diff` sem alterações em `src/main`).
- **4.1:** `scripts/check-contract-compat.mjs` compara o contrato com o instantâneo `tools/contract/baseline/` (cópia de `openapi/`). Usa `js-yaml`, já instalado no app como dependência transitiva (nenhuma ferramenta nova; resolvido via `createRequire` a partir de `app-autorizador-processo`). Regras: falha em operação/parâmetro/propriedade/resposta/valor de enum removidos ou renomeados, mudança de tipo/formato e nova obrigatoriedade; passa em adições opcionais. Opção `--atualizar-base` regrava o instantâneo. Tornar uma propriedade antes obrigatória em opcional (relaxar) é aceito. **[REVISAR]**
- **4.2 (demonstração, em cópias temporárias; contrato real intocado):** nova obrigatoriedade, renomeação de propriedade de requisição e de resposta, e rota renomeada → exit 1 com a causa; nova propriedade opcional → exit 0.
- **5.1:** seção "Contrato OpenAPI" no `README.md` raiz.
- **5.2:** `node scripts/check-contract.mjs` OK; `./mvnw verify` verde (inclui os testes de conformidade); `npm run lint`, `npm run build` e `npm test` (226 testes) verdes.
- **5.3:** `SPEC-32` movida com `git mv` para `.specs/archive/`. Desvio: `.specs/06-execution-plan/` não existe. A única ocorrência de "legado/legacy" no backend é a regra do `ArchitectureTest` que a proíbe.
