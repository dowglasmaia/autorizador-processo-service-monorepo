# Tarefas — Estrutura do monorepo

## 1. Ponto de partida
- [x] 1.1 Confirmar com `git status` que não há alteração em arquivos rastreados e registrar a lista de não rastreados — evidência: rastreados modificados apenas `historico.jsonl` e `loop.json` (arquivos do devkit); não rastreados: `.env.example`, `compose.yaml`, `.specs/` (vários) e `Sistema Autorizador com Dashboard/`.
- [x] 1.2 Registrar o desvio: `.specs/06-execution-plan/` não existe — desvio confirmado e registrado na proposta.

## 2. Tag da versão original
- [x] 2.1 Criar a tag anotada `v0-original` no `HEAD` com mensagem em português
- [x] 2.2 Verificar `git tag -n1 v0-original` e `git show v0-original:pom.xml` (Spring Boot 2.2.6)

## 3. Protótipo
- [x] 3.1 Mover `Sistema Autorizador com Dashboard/` para `.specs/design/prototype/` sem alterar o conteúdo
- [x] 3.2 Verificar que `Autorizador.dc.html`, `support.js`, `_ds/` e `uploads/` estão no destino, que a pasta antiga não existe e que `DESIGN_SYSTEM_DISCOVERY.md` foi preservado

## 4. .gitignore
- [x] 4.1 Acrescentar `node_modules/`, `.angular/`, `.env` e `*.log`, mantendo `target/`, `dist/`, IDEs e `!.mvn/wrapper/maven-wrapper.jar`
- [x] 4.2 Verificar com `git ls-files --others --exclude-standard` e `git check-ignore -v` que `node_modules`, `target` e `.env` são ignorados e que o wrapper continua rastreado

## 5. README do monorepo
- [x] 5.1 Reescrever `README.md` com visão, estrutura de destino, como subir com Compose e links para `.specs/`, marcando "em construção" o que ainda não existe
  > ✅ 2026-11-08 — README reescrito com visão, estrutura de destino, Compose (só o banco disponível hoje), links para `.specs/` e marcações "em construção".
- [x] 5.2 Manter os créditos e o link do LinkedIn do README atual e usar "versão original" em vez de "legado"
  > ✅ 2026-11-08 — Créditos e link `www.linkedin.com/in/dowglasmaia` mantidos; funcionalidades originais descritas como "versão original".
- [x] 5.3 Conferir o README contra `MONOREPO_STRUCTURE.md`
  > ✅ 2026-11-08 — README comparado com `MONOREPO_STRUCTURE.md`: mesmas pastas de destino, nomes preservados e versão original na tag `v0-original`. Sem divergências.

## 6. Índice e verificação final
- [x] 6.1 Atualizar o status do conjunto em `.specs/INDEX.md`
  > ✅ 2026-11-08 — `.specs/INDEX.md` ganhou a seção "Status do conjunto" (SPEC-01 concluída, demais pendentes, `06-execution-plan/` inexistente).
- [x] 6.2 Buscar `legado|legacy` fora de `.specs/` e `.git`; esperado: nenhum resultado
  > ✅ 2026-11-08 — `grep -riE "legado|legacy"` fora de `.specs/` e `.git`: só 2 ocorrências em arquivos que não são nossos (`mvnw.cmd` do wrapper Maven, comentário "legacy .bat"; `.idea/workspace.xml`, ignorado pelo git). Nenhuma em código ou documentação do projeto.
- [x] 6.3 Confirmar com `git status` que nenhum arquivo rastreado foi removido e que o código original não foi alterado
  > ✅ 2026-11-08 — `git ls-files --deleted` vazio; `git status` sem remoções e sem alteração em `pom.xml`, `src/`, `mvnw`, `.mvn/`. Modificados rastreados: `.gitignore`, `README.md` e arquivos do devkit.
- [x] 6.4 Registrar a evidência por tarefa e o resumo, e arquivar a spec com `git mv` conforme `como-executar.md`
  > ✅ 2026-11-08 — Evidência registrada aqui; spec movida com `git mv` para `.specs/archive/01-estrutura-monorepo`.

## Resumo
SPEC-01 concluída: tag `v0-original` criada, protótipo movido para `.specs/design/prototype/`, `.gitignore` ampliado e README do monorepo reescrito. Nenhum código original foi tocado. Limitação aceita: pastas de destino só nascem com as próximas etapas; ocorrências de "legacy" em `mvnw.cmd` e `.idea` são de terceiros e ignoradas.
