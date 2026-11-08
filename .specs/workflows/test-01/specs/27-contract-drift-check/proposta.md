# Desvio de contrato

Etapa 27 de 31 · Fase 4 · derivada da `SPEC-32` (`.specs/04-integration/SPEC-32-contract-drift-check.md`), com referências à `SPEC-05` (contrato v1) e à `SPEC-16` (núcleo HTTP e tipos). Escrita antecipadamente, enquanto a etapa 26 (paridade com o baseline) é implementada; será revisada contra o que ela entregar.

## Objetivo
Garantir que o frontend nunca fique defasado em relação ao OpenAPI: os tipos regenerados devem ser idênticos aos versionados e as respostas reais da API devem respeitar o contrato. Sem mudar o contrato.

## Ponto de partida (conferido antes da etapa 26)
- Contrato em `api-autorizador-processo/src/main/resources/openapi/` (`common/` e `modules/` com `auth`, `autorizacao`, `painel`, `procedimento`). Há **8 operações** (`autenticar`, `obterUsuarioAtual`, `cadastrarProcedimento`, `listarProcedimentos`, `obterProcedimento`, `solicitarAutorizacao`, `listarAutorizacoes`, `obterResumoPainel`), não as 6 citadas na SPEC-32; vale o contrato real.
- Erros RFC 9457 com `code` em `common/responses.yaml` (`VALIDACAO_INVALIDA`, `CORPO_INVALIDO`, `PARAMETRO_INVALIDO`, `PROCEDIMENTO_DUPLICADO`, `AUTORIZACAO_NEGADA`, `PROCEDIMENTO_NAO_ENCONTRADO`, `CREDENCIAIS_INVALIDAS`, `ERRO_INTERNO`…); a lista exata é conferida na execução.
- Geração de tipos já existe: `app-autorizador-processo/scripts/gerar-api-types.mjs` (`npm run api:types`) escreve `src/app/shared/api/api-types.ts` com `openapi-typescript`. Falta a verificação de desvio.
- Backend tem testes `*ApiTest` por módulo (Testcontainers) e `ArchitectureTest`; não há validação das respostas contra o esquema OpenAPI.
- Não existem `scripts/` na raiz, `.github/workflows` (CI é a etapa 28/SPEC-40) nem tag/branch base de contrato além da tag `v0-original` (código original, sem OpenAPI).
- `.specs/06-execution-plan/` não existe; desvio registrado na evidência.

## Escopo
1. `scripts/check-contract.mjs` na raiz: regenera os tipos em diretório temporário e compara com `api-types.ts` versionado; sai com código ≠ 0 apontando o arquivo divergente. Para reutilizar a geração sem duplicá-la, o gerador existente passa a aceitar um caminho de saída opcional (comportamento padrão inalterado).
2. Teste de conformidade no backend: chama cada uma das 8 operações com a aplicação real (Testcontainers) e valida status e corpo contra o esquema do OpenAPI, cobrindo todos os `code` de erro do contrato. Preferência por validação via testes do backend; se for necessária biblioteca de validação de esquema (ex.: `swagger-request-validator`), justificá-la em `doc.md`/README.
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
- Referência base da compatibilidade: como não há tag do contrato v1, usa-se um instantâneo do contrato versionado em pasta própria (ex.: `tools/contract/baseline/`), atualizado conscientemente a cada mudança aditiva aprovada. Alternativa (comparar com branch/tag git) descartada por exigir histórico no CI. **[REVISAR]**
- Comparação de compatibilidade implementada em script Node sem nova ferramenta (diff estrutural de paths, propriedades e `required` do YAML já resolvido); caso falte biblioteca de YAML, reutiliza-se a que o `openapi-typescript` já traz.
- Número de operações segue o contrato real (8), não o texto da SPEC-32 (6).
- Execução do script no CI fica para a SPEC-40 (etapa 28), conforme a SPEC.
