# Estrutura do monorepo

Deriva da SPEC-01 (`.specs/01-architecture/SPEC-01-monorepo-estrutura.md`) e de `.specs/01-architecture/MONOREPO_STRUCTURE.md`.

## Por quê
A raiz mistura o projeto original (Spring Boot 2.2.6), o protótipo HTML e arquivos novos não rastreados (`compose.yaml`, `.env.example`, `.specs/`). Antes de gerar os projetos novos (SPEC-03, SPEC-14) é preciso preservar a versão original no histórico, tirar o protótipo da raiz e deixar a raiz pronta para dois projetos independentes.

## O que muda
- Tag anotada `v0-original` no `HEAD` atual, preservando o código original (a tag não é movida nem recriada depois).
- `Sistema Autorizador com Dashboard/` é movida para `.specs/design/prototype/`, com o conteúdo intacto (`Autorizador.dc.html`, `support.js`, `_ds/`, `uploads/`). A pasta `.specs/design/` já existe e tem `DESIGN_SYSTEM_DISCOVERY.md`, que deve ser preservado.
- `.gitignore` da raiz passa a cobrir `target/`, `node_modules/`, `dist/`, `.angular/`, `.env`, `*.log` e IDEs, mantendo `!.mvn/wrapper/maven-wrapper.jar`. O arquivo atual já cobre `target/`, `dist/` e IDEs; acrescenta-se o que falta.
- `README.md` da raiz reescrito como README do monorepo (visão, estrutura, como subir com Compose, links para `.specs/`), marcando "em construção" o que ainda não existe e mantendo os créditos e o link do LinkedIn do README atual.
- `.specs/INDEX.md` atualizado com o status; a evidência da conclusão vai na própria spec arquivada (ver decisões).

## O que fica de fora
- Gerar `api-autorizador-processo/` e `app-autorizador-processo/`: etapas das SPEC-03 e SPEC-14.
- Remover o código original da raiz (`pom.xml`, `src/`, `mvnw`, `.mvn/`): fica para a SPEC-03, depois do baseline (SPEC-02).
- Criar `tools/parity/`, `.github/workflows/`, Dockerfiles e ajustar `compose.yaml`/`.env.example`: SPEC-02, 10, 13 e 40. Pastas vazias não são versionadas pelo git, então as "pastas de destino" ficam documentadas no README e nascem com o primeiro arquivo de cada etapa.
- Qualquer alteração em código da aplicação, no histórico git ou em dependências.

## Decisões de design
- Tag anotada (`git tag -a`) com mensagem em português, para que `git tag -n1 v0-original` mostre a mensagem.
- Movimentação com `git mv` não serve, pois o protótipo não é rastreado; usa-se mover o diretório no sistema de arquivos. O devkit faz os commits; não se roda `git commit` nem `git push` aqui.
- Nenhum nome com "legado/legacy" em arquivos ou pastas fora de `.specs/`; no README, usar "versão original".
- Alterações não rastreadas existentes (`compose.yaml`, `.env.example`, `.specs/`) são preservadas.
- A referência a `.specs/06-execution-plan/STATE.md` (T6 da spec original) não se aplica: a pasta não existe (desvio registrado pelo plano). A evidência fica em `tarefas.md` e no resumo da spec arquivada.

## Critérios de aceite
- `git tag -n1 v0-original` lista a tag com mensagem, e `git show v0-original:pom.xml` mostra Spring Boot 2.2.6.
- `.specs/design/prototype/Autorizador.dc.html` existe, com `_ds/` e `uploads/`; a pasta antiga não existe mais; `.specs/design/DESIGN_SYSTEM_DISCOVERY.md` continua lá.
- `git ls-files --others --exclude-standard` não lista `node_modules`/`target`.
- `.gitignore` contém todos os itens listados e mantém a exceção do wrapper.
- `README.md` coerente com `MONOREPO_STRUCTURE.md`, sem afirmar o que não existe, com créditos preservados.
- Busca por `legado|legacy` fora de `.specs/` e `.git` sem resultados.
- Nenhum arquivo rastreado removido; `./mvnw` do código original continua compilando (nada de código foi tocado).

## Decisões tomadas sem consulta
- A pasta `.specs/06-execution-plan/` não existe; o item de `STATE.md` da T6 foi substituído por evidência em `tarefas.md`. Sem necessidade de revisão humana.
- O "início quando execução autorizada" da SPEC-01 é coberto pelo loop automático `test-01`, que já traz a autorização. **[REVISAR]** confirmar que a criação da tag `v0-original` sobre o `HEAD` atual (`71f43a7` ou o commit do devkit que o sucede, sem mudança de código) é o ponto desejado.
- Desvio confirmado (tarefa 1.2): `.specs/06-execution-plan/` não existe; a evidência fica em `tarefas.md`.
- Tag `v0-original` criada sobre o `HEAD` `7f7ce6e` (commit de proposta do devkit, sem mudança de código). **[REVISAR]**
