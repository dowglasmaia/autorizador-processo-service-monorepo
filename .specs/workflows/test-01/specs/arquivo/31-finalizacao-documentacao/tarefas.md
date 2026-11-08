# Tarefas — Finalização e aceite

## 1. Preparação
- [x] 1.1 Conferir o estado atual: listar specs fora de `.specs/archive/`, localizar os comandos reais (`package.json`, READMEs, `tools/`, `scripts/`) e registrar o desvio de `06-execution-plan/` inexistente — evidência: `.specs/06-execution-plan/` inexistente (confirmado); fora de `archive/`: SPEC-01, 02, 03, 08, 09, 11, 12, 20, 21, 23, 42, 43; comandos reais: `mvnw verify`; `npm run lint/build/test/e2e`; `scripts/check-contract*.mjs`; `tools/parity/replay.mjs`
- [x] 1.2 Criar o clone local descartável do HEAD em pasta temporária fora do repositório para o checkout limpo — evidência: clone de HEAD 35f1529 em pasta temporária do sistema (`%TEMP%utz31-*`), fora do repositório; um segundo clone com `core.autocrlf=false` foi necessário (ver desvio na proposta); ambos removidos na 2.6

## 2. Verificação global (mesma sessão)
- [x] 2.1 Rodar `./mvnw verify` no backend a partir do checkout limpo e registrar o resultado (testes e ArchUnit) — evidência: `./mvnw -B verify` (JDK 25.0.2) → BUILD SUCCESS, 247 testes, 0 falhas/erros/ignorados, `ArchitectureTest` 23/23, ~1m22
- [x] 2.2 Rodar `npm ci`, `npm run lint`, `npm run build` e `npm test` no frontend a partir do checkout limpo e registrar o resultado — evidência: `npm ci` ok; `npm run lint` "All files pass linting"; `npm run build` ok; `npm test` 32 arquivos, 226 testes passando
- [x] 2.3 Subir `docker compose up -d --build` com `.env` temporário (segredos aleatórios) e confirmar saúde de `postgres`, `api` e `app` — evidência: `docker-compose -p autz31 up -d --build --wait` (binário independente; sem plugin `docker compose`), `.env` temporário com segredos aleatórios e portas alternativas (18081/15433, pois 8080/5432 estavam ocupadas por outro projeto) → `postgres`, `api` e `app` healthy
- [x] 2.4 Rodar `npm run e2e` e `node tools/parity/replay.mjs --target v1` contra o ambiente e registrar os resultados — evidência: `npm run e2e` (APP_PORT=14200) → 30 passed (4,4 min, desktop+móvel); `replay.mjs --target v1` → 23/23 OK
- [x] 2.5 Rodar os checks de contrato (`scripts/check-contract.mjs` e `check-contract-compat.mjs`) e registrar o resultado — evidência: `check-contract.mjs` OK (api-types.ts corresponde ao contrato); `check-contract-compat.mjs` OK (8 operações, 8 no contrato atual)
- [x] 2.6 Derrubar o Compose com `down -v`, remover o clone temporário e confirmar que não restam contêineres, volumes nem processos — evidência: `down -v --remove-orphans` nos projetos `autz31` e `e2e` (teardown); clone temporário removido; sem contêineres, volumes ou redes `autz31*`/`e2e*`; imagens `autz31-*` removidas; sem processo escutando nas portas usadas (só soquetes TIME_WAIT); os contêineres e volumes de outros projetos foram preservados

## 3. Varreduras
- [x] 3.1 Varrer "legado/legacy" no backend e confirmar que só restam a regra do ArchUnit e o README que a descreve
 — evidência: `git grep -i "legado|legacy"`: só README do backend (regra descrita) e `ArchitectureTest` regra 9; a ocorrência em `app/tsconfig.json` é a opção `enableI18nLegacyMessageIdFormat` do Angular (frontend, fora do escopo da regra)
- [x] 3.2 Varrer segredos e credenciais versionados e confirmar `.env` fora do índice e só `.env.example` versionado
 — evidência: `.env` ignorado (`git check-ignore`); só `.env.example` (raiz e api) versionados; varredura de padrões de segredo/chave privada sem achados (valores de dev/teste documentados como tais)
- [x] 3.3 Varrer `TODO(domain)`, `TODO` e `FIXME` pendentes nos dois projetos e resolver ou registrar cada um
 — evidência: nenhum `TODO`, `TODO(domain)` ou `FIXME` nos dois projetos, `tools/`, `scripts/` e CI
- [x] 3.4 Analisar dependências sem uso (`dependency:analyze`, `npm ls` e revisão de imports) e remover só as comprovadamente sem uso, com testes verdes
 — evidência: `dependency:analyze` indisponível (plugin não baixa: erro PKIX de certificado, ambiente); revisão manual: todas as dependências do `pom.xml` e do `package.json` têm uso (Envers em entidades, Modulith, OTEL, prettier com `.prettierrc`, openapi-typescript em `api:types`, etc.); nada removido

## 4. Documentação
- [x] 4.1 Atualizar o `README.md` da raiz: stack real, pré-requisitos, comandos validados, Compose, `tools/`, CI e estrutura do monorepo
 — evidência: `README.md` da raiz atualizado (aviso de conclusão, pré-requisitos, comandos validados, `tools/`, estrutura); Compose e CI já estavam corretos
- [x] 4.2 Atualizar `api-autorizador-processo/README.md`: endpoints, variáveis de ambiente, testes, ArchUnit e riscos aceitos
 — evidência: README do backend: estado, endpoints (8 operações), testes, ArchUnit e riscos aceitos; variáveis já completas
- [x] 4.3 Atualizar `app-autorizador-processo/README.md`: comandos, tipos gerados do OpenAPI, Nginx/proxy, e2e e acessibilidade
 — evidência: README do frontend: `api:types`, seção de tipos gerados e de acessibilidade; Nginx/proxy e e2e já documentados
- [x] 4.4 Acrescentar nota em `.specs/design/DESIGN_SYSTEM_DISCOVERY.md` de que o baseline executado substitui os pontos [INFERIDO]
 — evidência: nota de encerramento acrescentada no topo de `DESIGN_SYSTEM_DISCOVERY.md`
- [x] 4.5 Atualizar `.specs/INDEX.md` e `.specs/memory/` ao estado real (remover "só documentação" e a lista de pendentes) — evidência: `INDEX.md` sem "só documentação" e sem lista de pendentes (status: todas arquivadas, PEN-xx abertas, SPEC-50 não aplicável); `memory/contexto-tecnico.md` atualizado

## 5. Rastreabilidade e arquivamento
- [x] 5.1 Conferir `FUNCTIONAL_TRACEABILITY.md`: cada F-item com spec concluída (com evidência) ou fora de escopo declarado; PEN-xx mantidas abertas — evidência: matriz conferida: F01–F21 com specs concluídas e evidência; F16, F22, F23 fora de escopo declarado (PEN-06, PEN-05, PEN-11); PEN-xx mantidas abertas; sem F-item órfão, sem edição necessária
- [x] 5.2 Preencher "Resultado" e arquivar com `git mv` em `.specs/archive/` as specs concluídas ainda fora dele, listando as que ficarem de fora e o motivo — evidência: 11 specs preenchidas e movidas com `git mv` para `archive/` (SPEC-01, 02, 03, 08, 09, 11, 12, 20, 21, 23, 42); nenhuma ficou de fora; as pastas `02-backend`, `03-frontend`, `04-integration` ficaram vazias (a SPEC-43 sai em 6.2)
- [x] 5.3 Escrever `.specs/05-quality/RELATORIO_FINAL.md` com resultados da verificação global, desvios, riscos aceitos e pendências abertas (PEN-xx; SPEC-50 não aplicável) — evidência: `.specs/05-quality/RELATORIO_FINAL.md` criado

## 6. Fechamento
- [x] 6.1 Reexecutar `./mvnw verify`, `npm run lint`, `npm run build` e `npm test` no working tree após as edições e corrigir falhas — evidência: `./mvnw -B verify` exit 0 (248 testes nos relatórios, 0 falhas); `npm run lint`, `npm run build`, `npm test` ok (226 testes)
- [x] 6.2 Preencher o "Resultado" da SPEC-43 e arquivá-la com `git mv` para `.specs/archive/`, conforme `como-executar.md` §4 — evidência: Resultado preenchido e SPEC-43 movida para `.specs/archive/`
