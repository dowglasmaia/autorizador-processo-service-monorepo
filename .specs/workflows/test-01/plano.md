# Refatoração do backend e criação completa do frontend do Autorizador

Loop `test-01` · gerado pelo devkit a partir do `loop.json` (não edite à mão).

## Objetivo

o Objetivo e ter todas as specs executadas sem invervenção humana e com o refectory do backend e a criação do front por compelto.

## Regras

## Fonte de verdade
- Cada etapa executa **uma spec já escrita** em `.specs/` (a SPEC-NN indicada na descrição). A spec da iteração deve ser derivada dela: mantenha objetivo, escopo, tarefas e critérios de aceitação; apenas divida tarefas se passar de ~30. **Não invente requisitos.**
- Leitura obrigatória antes de cada etapa: `.specs/INDEX.md`, `.specs/memory/produto.md`, `.specs/memory/contexto-tecnico.md`, `.specs/shared/como-executar.md` e a spec da etapa (com as dependências que ela cita). A pasta `.specs/06-execution-plan/` citada no índice **não existe**; ignore a referência e registre o desvio na evidência.
- Ao concluir, siga `como-executar.md`: evidência curta por tarefa, resumo e `git mv` da spec para `.specs/archive/`.

## Arquitetura e stack (decisões já tomadas)
- Monorepo: `api-autorizador-processo/` (Spring Boot 4.1.2, Java 25, Maven Wrapper, PostgreSQL 16, Flyway, Testcontainers, sem H2) e `app-autorizador-processo/` (Angular 21, TypeScript estrito, Tailwind, standalone, zoneless, Signals, Reactive Forms tipados). `compose.yaml` e `.env.example` na raiz.
- Backend em Hexagonal Pragmática, pacote por funcionalidade (`procedimento`, `autorizacao`, `painel`, `shared`), contrato OpenAPI 3.1 primeiro (`src/main/resources/openapi/`), erros RFC 9457, JWT com e-mail e senha. **ArchUnit é obrigatório** (`ArchitectureTest`).
- O frontend consome tipos **gerados** do OpenAPI e não duplica DTOs. O design vem do protótipo em `Sistema Autorizador com Dashboard/` (depois movido para `.specs/design/prototype/`) e da SPEC-15.
- Use as skills do projeto: `java-spring-scaffold`, `java-spring-create-module`, `java-persistence-performance`, `openapi-rest-contract`, `java-modern-engineering`, `angular-frontend-scaffold`, `angular-module-scaffold`, `angular-tailwind-ui-specialist`. A skill de scaffold nunca escreve em diretório existente: gere o projeto novo e depois ajuste.
- Ambiente: JDK 25.0.2 em `C:\jdk-25.0.2` (defina `JAVA_HOME` e `PATH`), Node 24, Docker 29, Windows com Git Bash. O contrato OpenAPI continua exigindo aprovação, que neste loop já está dada pelas specs; não altere contrato além do que a spec descreve.

## Convenções
- **Nenhum nome "legado/legacy"** no código do backend (use "versão original" apenas em documentação). O código antigo da raiz fica preservado pela tag `v0-original` (SPEC-01) e só é removido depois do baseline (SPEC-03).
- Dockerfile próprio e CI independente por projeto, disparado só por mudança no próprio caminho (SPEC-10, 13, 40).
- Segredos nunca versionados; só `.env.example`. Mensagens, textos de UI, commits e documentação em português.
- Cada etapa deve deixar o repositório compilando e com os testes do que já existe passando. Sem `--no-verify` e sem testes desabilitados para passar.
- Não há verificações automáticas configuradas no devkit porque os projetos novos ainda não existem. **Cada etapa deve rodar por conta própria** o build, os testes e o lint do(s) projeto(s) que tocar (`./mvnw verify` em `api-autorizador-processo`; `npm run lint`, `npm run build` e `npm test` em `app-autorizador-processo`) e corrigir as falhas antes de concluir.
- Etapas que dependem de Docker (Testcontainers, Compose, e2e): se o Docker não estiver disponível, registre bloqueio na tarefa afetada, siga com as independentes e não marque como concluída sem executar.

## Fase 1 · Fase 1 — Fundações do monorepo e do backend

1. **Estrutura do monorepo** · `01-estrutura-monorepo` · ✔ f70aeb1 — Executa a SPEC-01: tag `v0-original`, `.gitignore` raiz, pastas de destino e movimentação do protótipo para `.specs/design/prototype/`. Referências: `01-architecture/MONOREPO_STRUCTURE.md`. Limite: não gera os projetos ainda e não remove o código antigo.
2. **Baseline golden cases** · `02-baseline-golden-cases` · ✔ 43acc61 — Executa a SPEC-02: casos executáveis em `tools/parity/` (cases.json e replay.mjs) obtidos rodando a versão original da tag `v0-original` em contêiner JDK 8. Referências: `BASELINE_COMPORTAMENTO_ORIGINAL.md`. Limite: sem alterar a API original.
3. **Fundação do backend** · `03-fundacao-backend` · ✔ 1ce3f2f — Executa a SPEC-03: gera `api-autorizador-processo` (Java 25, Spring Boot 4.1.2, Modulith, ArchUnit) e remove o código antigo da raiz depois do baseline. Referências: `TARGET_ARCHITECTURE.md` e `DECISIONS.md`. Limite: sem regras de negócio.
4. **Fundação do banco** · `04-fundacao-banco` · ✔ b485c9c — Executa a SPEC-04: PostgreSQL 16, Flyway, migrações iniciais, auditoria e Testcontainers. Referências: `DATABASE_DESIGN.md` e a skill `java-persistence-performance`. Limite: sem módulos de negócio.
5. **Contrato OpenAPI v1** · `05-contrato-openapi-v1` · ✔ 14ea799 — Executa a SPEC-05: contratos OpenAPI 3.1 por módulo (procedimento, autorizacao, painel) e erros comuns, com geração das interfaces. Referências: `API_CONTRACTS.md` e a skill `openapi-rest-contract`. Limite: sem implementação dos casos de uso.
6. **Erros, validação e CORS** · `06-erros-validacao-cors` · ✔ 82ae4e0 — Executa a SPEC-06: tratamento global de erros RFC 9457, validação e CORS. Referências: `SECURITY_REQUIREMENTS.md`. Limite: não implementa autenticação.

## Fase 2 · Fase 2 — Módulos do backend refatorado

7. **Autenticação JWT no backend** · `07-autenticacao-jwt-backend` · ✔ — Executa a SPEC-22: login por e-mail e senha, emissão e validação de JWT (HS256) e proteção de todas as rotas, exceto login e health. Referências: `SECURITY_REQUIREMENTS.md`. Limite: sem papéis e sem cadastro de usuários.
8. **Módulo procedimento** · `08-modulo-procedimento` · · — Executa a SPEC-07: módulo `procedimento` em Hexagonal Pragmática, com paridade com a versão original. Referências: skill `java-spring-create-module` e golden cases. Limite: sem o catálogo estendido.
9. **Catálogo estendido** · `09-catalogo-estendido` · · — Executa a SPEC-12: campos de cadastro do protótipo no procedimento (nova feature NEW-01), com migração e contrato atualizado. Referências: o protótipo em `.specs/design/prototype/`. Limite: só o módulo procedimento.
10. **Módulo autorizacao** · `10-modulo-autorizacao` · · — Executa a SPEC-08: solicitar autorização com critérios de idade e sexo e consulta do histórico. Referências: `FUNCTIONAL_TRACEABILITY.md`. Limite: sem o painel.
11. **Módulo painel** · `11-modulo-painel` · · — Executa a SPEC-09: resumo agregado do painel (NEW-02), com consultas eficientes. Referências: protótipo e skill `java-persistence-performance`. Limite: somente leitura.
12. **Config e observabilidade** · `12-config-observabilidade` · · — Executa a SPEC-11: perfis de configuração, health, logs e métricas básicas. Referências: `NON_FUNCTIONAL_REQUIREMENTS.md`. Limite: sem stack externa de monitoramento.
13. **Docker Compose do backend** · `13-compose-backend` · · — Executa a SPEC-10: Dockerfile da API (`eclipse-temurin:25`) e ajuste do `compose.yaml` e do `.env.example` com PostgreSQL e API. Referências: `compose.yaml` já existente. Limite: o serviço do frontend é tratado na SPEC-13.
14. **Migração de dados condicional** · `14-migracao-dados-condicional` · · — Executa a SPEC-50 apenas se houver base de origem acessível; caso contrário, registra como não aplicável com evidência e a arquiva. Referências: SPEC-50. Limite: nunca versionar dados reais.

## Fase 3 · Fase 3 — Frontend Angular completo

15. **Fundação do frontend** · `15-fundacao-frontend` · · — Executa a SPEC-14: gera `app-autorizador-processo` (Angular 21, Tailwind, modo estrito, porta fixa) com a skill `angular-frontend-scaffold`. Referências: `TARGET_ARCHITECTURE.md`. Limite: apenas o shell mínimo.
16. **Design system e layout** · `16-design-system-layout` · · — Executa a SPEC-15: tokens derivados do protótipo, componentes base e shell de navegação. Referências: `design/DESIGN_SYSTEM_DISCOVERY.md` e a skill `angular-tailwind-ui-specialist`. Limite: sem telas de negócio.
17. **Núcleo HTTP e tipos** · `17-nucleo-http-tipos` · · — Executa a SPEC-16: tipos gerados do OpenAPI, interceptors e mapeamento de erros RFC 9457. Referências: contrato em `api-autorizador-processo/src/main/resources/openapi/`. Limite: sem DTOs duplicados à mão.
18. **Login no frontend** · `18-login-frontend` · · — Executa a SPEC-23: tela de login, guarda de rotas, armazenamento do token e logout. Referências: SPEC-22 e o design system. Limite: sem cadastro e sem recuperação de senha.
19. **Contêiner Nginx do frontend** · `19-container-nginx-frontend` · · — Executa a SPEC-13: Dockerfile do frontend com Nginx, proxy de `/api/` e inclusão do serviço `app` no `compose.yaml`. Referências: `compose.yaml`. Limite: sem pipeline de CI.
20. **Tela de procedimentos** · `20-feature-procedimentos` · · — Executa a SPEC-17: lista e cadastro em 4 etapas, com o catálogo estendido. Referências: skill `angular-module-scaffold` e protótipo. Limite: sem solicitação de autorização.
21. **Tela solicitar autorização** · `21-feature-solicitar-autorizacao` · · — Executa a SPEC-18: formulário de solicitação com seletor de procedimento e exibição do resultado. Referências: SPEC-08 e protótipo. Limite: sem histórico.
22. **Tela de autorizações** · `22-feature-autorizacoes` · · — Executa a SPEC-19: histórico de autorizações com filtros e paginação. Referências: SPEC-08 e protótipo. Limite: somente leitura.
23. **Tela do painel** · `23-feature-painel` · · — Executa a SPEC-20: painel com indicadores e gráficos, usando o resumo do backend e os contadores de sessão. Referências: SPEC-09 e protótipo. Limite: sem novos endpoints.
24. **Microcopy e acessibilidade** · `24-microcopy-acessibilidade` · · — Executa a SPEC-21: revisão de textos, estados vazios e de erro, foco, contraste e navegação por teclado (WCAG 2.1 AA). Referências: todas as telas. Limite: sem novas funcionalidades.

## Fase 4 · Fase 4 — Integração, qualidade e finalização

25. **Testes e2e do ambiente** · `25-e2e-ambiente-completo` · · — Executa a SPEC-30: testes ponta a ponta contra o ambiente completo do Compose, cobrindo login e os fluxos principais. Referências: `TEST_STRATEGY.md`. Limite: sem alterar regras de negócio.
26. **Paridade com o baseline** · `26-paridade-baseline` · · — Executa a SPEC-31: roda os golden cases de `tools/parity/` contra a nova API e documenta diferenças aceitas. Referências: SPEC-02. Limite: divergência inesperada vira correção, não exceção.
27. **Desvio de contrato** · `27-contract-drift-check` · · — Executa a SPEC-32: verificação de que os tipos regenerados são idênticos aos versionados e de que as respostas reais respeitam o contrato. Referências: SPEC-05 e SPEC-16. Limite: sem mudar o contrato.
28. **CI por projeto** · `28-ci-por-projeto` · · — Executa a SPEC-40: `backend-ci.yml`, `frontend-ci.yml` e `integracao-ci.yml`, cada um disparado só por mudança no próprio caminho. Referências: `.github/workflows`. Limite: sem deploy.
29. **Segurança e hardening** · `29-seguranca-hardening` · · — Executa a SPEC-41: revisão de segurança, cabeçalhos, segredos, limites de requisição e dependências. Referências: `SECURITY_REQUIREMENTS.md`. Limite: correções dentro do escopo da spec.
30. **Smoke de desempenho** · `30-smoke-desempenho` · · — Executa a SPEC-42: smoke de desempenho e dimensionamento básico no ambiente do Compose. Referências: `NON_FUNCTIONAL_REQUIREMENTS.md`. Limite: sem testes de carga pesados.
31. **Finalização e aceite** · `31-finalizacao-documentacao` · · — Executa a SPEC-43: README do monorepo, documentação final, conferência dos critérios de aceite globais e arquivamento de todas as specs. Referências: `.specs/INDEX.md`. Limite: sem código novo de negócio.

## Fora do escopo

- Novas funcionalidades além das specs (papéis/perfis de usuário, multi-tenant, cadastro de usuários, recuperação de senha).
- Deploy em nuvem, infraestrutura como código e publicação de imagens em registry.
- Alterar o histórico git ou a tag `v0-original` depois de criada.
- Reescrever o protótipo HTML (`Sistema Autorizador com Dashboard/`); ele só é movido e consultado como referência de design.
- Migração de dados reais de produção: a SPEC-50 é condicional e só roda se houver base de origem acessível; caso contrário, registre como não aplicável.
- Qualquer app mobile.

## Verificações

- nenhuma
