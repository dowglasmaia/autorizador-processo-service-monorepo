# Tarefas — Estrutura do monorepo

## 1. Ponto de partida
- [ ] 1.1 Confirmar com `git status` que não há alteração em arquivos rastreados e registrar a lista de não rastreados
- [ ] 1.2 Registrar o desvio: `.specs/06-execution-plan/` não existe

## 2. Tag da versão original
- [ ] 2.1 Criar a tag anotada `v0-original` no `HEAD` com mensagem em português
- [ ] 2.2 Verificar `git tag -n1 v0-original` e `git show v0-original:pom.xml` (Spring Boot 2.2.6)

## 3. Protótipo
- [ ] 3.1 Mover `Sistema Autorizador com Dashboard/` para `.specs/design/prototype/` sem alterar o conteúdo
- [ ] 3.2 Verificar que `Autorizador.dc.html`, `support.js`, `_ds/` e `uploads/` estão no destino, que a pasta antiga não existe e que `DESIGN_SYSTEM_DISCOVERY.md` foi preservado

## 4. .gitignore
- [ ] 4.1 Acrescentar `node_modules/`, `.angular/`, `.env` e `*.log`, mantendo `target/`, `dist/`, IDEs e `!.mvn/wrapper/maven-wrapper.jar`
- [ ] 4.2 Verificar com `git ls-files --others --exclude-standard` e `git check-ignore -v` que `node_modules`, `target` e `.env` são ignorados e que o wrapper continua rastreado

## 5. README do monorepo
- [ ] 5.1 Reescrever `README.md` com visão, estrutura de destino, como subir com Compose e links para `.specs/`, marcando "em construção" o que ainda não existe
- [ ] 5.2 Manter os créditos e o link do LinkedIn do README atual e usar "versão original" em vez de "legado"
- [ ] 5.3 Conferir o README contra `MONOREPO_STRUCTURE.md`

## 6. Índice e verificação final
- [ ] 6.1 Atualizar o status do conjunto em `.specs/INDEX.md`
- [ ] 6.2 Buscar `legado|legacy` fora de `.specs/` e `.git`; esperado: nenhum resultado
- [ ] 6.3 Confirmar com `git status` que nenhum arquivo rastreado foi removido e que o código original não foi alterado
- [ ] 6.4 Registrar a evidência por tarefa e o resumo, e arquivar a spec com `git mv` conforme `como-executar.md`
