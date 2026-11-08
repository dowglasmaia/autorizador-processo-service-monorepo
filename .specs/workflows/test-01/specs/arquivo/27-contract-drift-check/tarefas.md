## 1. Preparação
- [x] 1.1 Ler `SPEC-32`, `SPEC-05`, `SPEC-16`, `API_CONTRACTS.md` §5 e `core/http/doc.md`; registrar a ausência de `.specs/06-execution-plan/`.
- [x] 1.2 Conferir o estado após a etapa 26 (contrato, `api-types.ts`, Docker disponível) e revisar a proposta contra o que mudou.

## 2. Verificação de tipos gerados
- [x] 2.1 Fazer `scripts/gerar-api-types.mjs` aceitar caminho de saída opcional, mantendo o comportamento padrão.
- [x] 2.2 Criar `scripts/check-contract.mjs` na raiz: gera em diretório temporário, compara com `api-types.ts` e sai com código ≠ 0 apontando o arquivo.
- [x] 2.3 Demonstrar a detecção: remover um campo do YAML sem regenerar (falha) e adicionar campo opcional regenerando (passa); reverter tudo.

## 3. Conformidade das respostas no backend
- [x] 3.1 Escolher e justificar o mecanismo de validação contra o esquema OpenAPI (preferir sem nova dependência).
- [x] 3.2 Criar teste de conformidade que chama as 8 operações com a aplicação real e valida status e corpo.
- [x] 3.3 Cobrir todos os `code` de erro do contrato (o enum `code` de `common/responses.yaml` tem 12 valores: 10 provocados por rota em `ContratoRespostasApiTest`, `ERRO_INTERNO` em `ContratoErroInternoApiTest` e `ACESSO_NEGADO` em `AcessoNegadoContratoTest`; status 400, 401, 403, 404, 429, 500).
- [x] 3.4 Provar que o teste falha com uma resposta divergente (adulteração temporária) e reverter.

## 4. Compatibilidade do contrato
- [x] 4.1 Versionar o instantâneo base do contrato v1 e implementar a comparação (remoção, renomeação, nova obrigatoriedade falham; aditivo passa).
- [x] 4.2 Demonstrar os dois sentidos e reverter.

## 5. Documentação e verificação final
- [x] 5.1 Documentar no `README.md` raiz como regenerar tipos, atualizar o instantâneo e interpretar falhas.
- [x] 5.2 Rodar `./mvnw verify`, `npm run lint`, `npm run build` e `npm test` e `node scripts/check-contract.mjs`; corrigir falhas.
- [x] 5.3 Registrar evidência curta por tarefa e arquivar a spec conforme `como-executar.md`.
