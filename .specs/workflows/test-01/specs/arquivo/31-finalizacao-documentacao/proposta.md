# Finalização e aceite

Etapa 31 de 31 · Fase 4 · derivada da `SPEC-43` (`.specs/05-quality/SPEC-43-finalizacao-documentacao.md`). Última etapa do loop: fecha a modernização com validação global, documentação coerente com o resultado e arquivamento das specs.

## Objetivo
Encerrar a modernização do Autorizador: provar, na mesma sessão e a partir de um estado limpo, que o monorepo sobe, passa nos testes e atende aos critérios de aceite globais; deixar README e documentos `.specs/` coerentes com o que foi realmente entregue; e arquivar as specs concluídas. **Sem código novo de negócio** (limite do plano).

## Ponto de partida (conferido contra o repositório atual, após a etapa 30)
- `.specs/06-execution-plan/` (com `STATE.md` e `DECISIONS.md`) **não existe** (desvio a registrar). A tarefa T6 da SPEC-43 ("relatório final em `STATE.md`") vira um `RELATORIO_FINAL.md` em `.specs/05-quality/` mais a atualização do `.specs/INDEX.md`. As decisões de arquitetura ficam em `.specs/01-architecture/DECISIONS.md`.
- Raiz: `README.md`, `compose.yaml`, `scripts/` (checks de contrato), `tools/` (`contract`, `parity`, `perf`, `security`), `.github/workflows/` (`backend-ci.yml`, `frontend-ci.yml`, `integracao-ci.yml`). Projetos `api-autorizador-processo/` e `app-autorizador-processo/` com Dockerfile e README próprios.
- As specs derivadas das etapas 1–30 estão em `.specs/workflows/test-01/specs/arquivo/`. Em `.specs/archive/` há 19 `SPEC-*.md` originais. **Ainda fora de `archive/`**: SPEC-01 (em `01-architecture/`), 02, 03, 08, 09, 11, 12, 20, 21, 23, 42 e 43. `INDEX.md` ainda diz "só documentação / nada foi implementado" e lista apenas 4 specs concluídas: está desatualizado.
- `git grep -i "legado\|legacy" -- api-autorizador-processo` só encontra o README do backend (linha que descreve a regra) e o `ArchitectureTest` (regra 9, que cita o termo para proibi-lo). Isso é esperado: a varredura distingue **nome no código** (proibido) de **regra de proibição** (permitida).
- `PERFORMANCE_REPORT.md` e `NON_FUNCTIONAL_REQUIREMENTS.md` existem em `.specs/05-quality/`; `FUNCTIONAL_TRACEABILITY.md` está em `.specs/00-product-discovery/`.

## Escopo
1. **Verificação global (T1)**: a partir de checkout limpo (clone local descartável do HEAD, sem `node_modules`/`target`), na mesma sessão: `./mvnw verify`, `npm ci`, `npm run lint`, `npm run build`, `npm test`, `docker compose up -d --build`, `npm run e2e`, `node tools/parity/replay.mjs --target v1` e os checks de contrato de `scripts/`. Derrubar o Compose com `down -v` no fim. Comandos conferidos nos `package.json`/READMEs reais; se algum nome da SPEC não existir, usar o equivalente e registrar.
2. **Varreduras (T2)**: "legado/legacy" no backend; segredos/credenciais versionados; `TODO(domain)`/`TODO`/`FIXME` pendentes; dependências sem uso (sem instalar nada novo).
3. **Documentação (T3)**: README raiz, do backend e do frontend com stack real, comandos validados na T1, endpoints, variáveis (`.env.example`), riscos aceitos e uso de `tools/`/CI; nota em `.specs/design/DESIGN_SYSTEM_DISCOVERY.md` de que o baseline executado substitui os pontos [INFERIDO]; `INDEX.md` e `memory/` ajustados ao estado real.
4. **Rastreabilidade (T4)**: conferir `FUNCTIONAL_TRACEABILITY.md`: todo F-item com spec concluída ou fora de escopo declarado; pendências de produto (PEN-xx) continuam registradas, não resolvidas.
5. **Arquivamento (T5)**: preencher "Resultado" e mover com `git mv` para `.specs/archive/` as specs concluídas ainda fora dele (`como-executar.md` §4), inclusive a SPEC-43 ao final.
6. **Relatório final (T6)**: `.specs/05-quality/RELATORIO_FINAL.md` com resultado da verificação global, desvios, riscos aceitos e pendências abertas (PEN-xx; SPEC-50 não aplicável).

## Fora do escopo
- Funcionalidade nova, mudança de contrato OpenAPI, de regra de negócio ou refatoração. Falha real encontrada na verificação recebe correção mínima registrada, nunca feature nova.
- Resolver pendências de produto (PEN-xx); papéis/perfis, multi-tenant, recuperação de senha.
- Deploy, publicação de imagens, IaC, migração de dados reais (SPEC-50 segue não aplicável).
- Alterar histórico git ou a tag `v0-original`; reescrever o protótipo HTML.
- Os commits: feitos pelo devkit.

## Critérios de aceitação
- Todos os comandos de T1 executados com sucesso na mesma sessão, com resultado observado registrado; sem Docker, as partes dependentes ficam **bloqueadas e não marcadas**.
- Varreduras limpas ou justificadas: nenhum "legado/legacy" em nome de código do backend, nenhuma credencial versionada, nenhum `TODO(domain)` pendente, nenhuma dependência sem uso.
- READMEs (raiz, backend, frontend) refletem a stack e os comandos validados; `INDEX.md` sem afirmações obsoletas.
- Matriz de rastreabilidade sem F-item órfão.
- Nenhuma spec concluída fora de `.specs/archive/`; sem spec pendente além da SPEC-50 (não aplicável, arquivada).
- Nenhum processo, contêiner ou volume ativo ao fim; nenhum segredo versionado.

## Decisões tomadas sem consulta
- **`STATE.md` substituído por `RELATORIO_FINAL.md` + `INDEX.md`**, pois `06-execution-plan/` não existe; a pasta não é recriada. **[REVISAR]**
- **"Checkout limpo" = clone local descartável do HEAD em pasta temporária fora do repositório**, removida ao fim (`git clean`/`checkout` são proibidos no loop). Como o clone não contém alterações não commitadas desta etapa, a verificação final (tarefa 6.1) roda também no working tree. **[REVISAR]**
- **Varredura de "legado/legacy"**: ocorrências na regra do ArchUnit e no README que a descreve são aceitas por documentarem a proibição. **[REVISAR]**
- **Arquivamento das specs originais restantes** só das que têm etapa derivada em `workflows/.../arquivo/` com tarefas concluídas; qualquer uma sem evidência fica fora e é listada no relatório. **[REVISAR]**
- **Dependências sem uso**: `dependency:analyze` é só informativo (falsos positivos em starters); remover apenas o comprovadamente sem uso e com testes verdes. **[REVISAR]**
- **Desvio: `docker compose` (plugin) indisponível; usado o binário `docker-compose` v5.6.1**, como o próprio `e2e/ambiente.ts` já faz. **[REVISAR]**
- **Portas alternativas na verificação** (API 18081, banco 15433, projeto Compose `autz31`, e2e com `APP_PORT=14200`): 8080 e 5432 estavam ocupadas por contêineres de outro projeto, que foram preservados. O projeto `autz31` evita apagar o volume `autorizador-processo_postgres-data` pré-existente. **[REVISAR]**
- **Correção mínima (achado da verificação): `.gitattributes` na raiz** forçando LF em `mvnw`, `*.sh`, `Dockerfile`, `*.inc`, `*.template` e `*.conf`. O primeiro clone, com `core.autocrlf=true` global do Windows, gerou `mvnw` com CRLF e o build da imagem da API falhou (`./mvnw: not found`). Um segundo clone com `core.autocrlf=false` passou. O HEAD verificado (35f1529) não contém o `.gitattributes`; os índices já estão em LF, então não há mudança de conteúdo. **[REVISAR]**
- **Resultado do lote 1 (HEAD 35f1529)**: backend 247 testes + ArchUnit 23/23; frontend lint/build/226 testes; Compose saudável; e2e 30/30; paridade 23/23; contrato OK.
- **Lote 2: `dependency:analyze` indisponível** (plugin não resolve por erro PKIX no ambiente); substituído por revisão manual de imports/uso, sem remoções. **[REVISAR]**
- **Lote 2: link para `RELATORIO_FINAL.md` no README da raiz** já aponta para o arquivo que a tarefa 5.3 cria neste mesmo loop. **[REVISAR]**
- **Lote 2: "legacy" em `app/tsconfig.json`** é nome de opção do Angular, não nome de código; mantido.
- **Lote 3: "Resultado" das specs arquivadas** é texto padronizado que aponta para a evidência da etapa derivada (sem números inventados); as já arquivadas em etapas anteriores não foram alteradas. **[REVISAR]**
- **Lote 3: numeração das PEN-xx** no `RELATORIO_FINAL.md` segue `PRODUCT_CONTEXT.md` (PEN-10 refresh token, PEN-11 perfis, PEN-12 limitador); o README do backend usa numeração diferente para PEN-11/12. Divergência de documentação preexistente, não corrigida. **[REVISAR]**
- **Lote 3: reexecução no working tree** (6.1): backend exit 0 (248 testes somados nos relatórios do Surefire, contra 247 no checkout limpo; sem teste novo) e frontend lint/build/226 testes ok.
