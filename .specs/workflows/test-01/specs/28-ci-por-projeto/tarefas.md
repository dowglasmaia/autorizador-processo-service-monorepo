## 1. Preparação
- [ ] 1.1 Ler `SPEC-40`, SPEC-10/13, `TEST_STRATEGY.md`, `SECURITY_REQUIREMENTS.md` (SEC-09) e `MONOREPO_STRUCTURE.md`; registrar a ausência de `.specs/06-execution-plan/`.
- [ ] 1.2 Revisar esta proposta contra a etapa 27 (existência de `scripts/check-contract.mjs`) e confirmar os scripts reais de cada projeto.

## 2. Workflow do backend
- [ ] 2.1 Criar `backend-ci.yml` com `paths`, `concurrency`, `permissions: contents: read` e `workflow_dispatch`.
- [ ] 2.2 Job `verify`: JDK 25, cache Maven, `./mvnw verify` (inclui `ArchitectureTest` e geração do contrato).
- [ ] 2.3 Jobs de build da imagem Docker do backend (sem publicar) e de auditoria Maven não bloqueante.
- [ ] 2.4 Job `paridade`: Compose com `postgres` + `api`, credenciais descartáveis e `replay.mjs --target v1`.

## 3. Workflow do frontend
- [ ] 3.1 Criar `frontend-ci.yml` com `paths`, `concurrency`, permissões mínimas e `workflow_dispatch`.
- [ ] 3.2 Jobs `npm ci`, `lint`, `test`, `build` com cache npm e Node 24.
- [ ] 3.3 Jobs de build da imagem Docker do frontend (sem publicar) e `npm audit`.

## 4. Workflow de integração
- [ ] 4.1 Criar `integracao-ci.yml` com `paths` de ambos os projetos, `compose.yaml`, `.env.example` e `scripts/check-contract.mjs`.
- [ ] 4.2 Job `check-contract` (bloqueado se a etapa 27 não entregou o script) e job `e2e` com Compose + Playwright, relatório só em falha.

## 5. Documentação
- [ ] 5.1 README raiz: badges por workflow, tabela "o que roda quando" e orientação sobre branch protection.

## 6. Verificação
- [ ] 6.1 Validar sintaxe com `actionlint` (ou equivalente) nos 3 arquivos e corrigir erros.
- [ ] 6.2 Conferir por script que os `paths` não cruzam projetos e simular os 3 cenários de aceite; registrar a execução real no GitHub como pendente.
- [ ] 6.3 Rodar localmente os comandos de cada job (`./mvnw verify`, `npm run lint/test/build`, `docker build` dos dois projetos) e corrigir falhas.
- [ ] 6.4 Registrar evidências por tarefa, resumo e `git mv` da spec para `.specs/archive/`.
