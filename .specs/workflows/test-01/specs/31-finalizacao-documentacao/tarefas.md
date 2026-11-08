# Tarefas — Finalização e aceite

## 1. Preparação
- [ ] 1.1 Conferir o estado atual: listar specs fora de `.specs/archive/`, localizar os comandos reais (`package.json`, READMEs, `tools/`, `scripts/`) e registrar o desvio de `06-execution-plan/` inexistente
- [ ] 1.2 Criar o clone local descartável do HEAD em pasta temporária fora do repositório para o checkout limpo

## 2. Verificação global (mesma sessão)
- [ ] 2.1 Rodar `./mvnw verify` no backend a partir do checkout limpo e registrar o resultado (testes e ArchUnit)
- [ ] 2.2 Rodar `npm ci`, `npm run lint`, `npm run build` e `npm test` no frontend a partir do checkout limpo e registrar o resultado
- [ ] 2.3 Subir `docker compose up -d --build` com `.env` temporário (segredos aleatórios) e confirmar saúde de `postgres`, `api` e `app`
- [ ] 2.4 Rodar `npm run e2e` e `node tools/parity/replay.mjs --target v1` contra o ambiente e registrar os resultados
- [ ] 2.5 Rodar os checks de contrato (`scripts/check-contract.mjs` e `check-contract-compat.mjs`) e registrar o resultado
- [ ] 2.6 Derrubar o Compose com `down -v`, remover o clone temporário e confirmar que não restam contêineres, volumes nem processos

## 3. Varreduras
- [ ] 3.1 Varrer "legado/legacy" no backend e confirmar que só restam a regra do ArchUnit e o README que a descreve
- [ ] 3.2 Varrer segredos e credenciais versionados e confirmar `.env` fora do índice e só `.env.example` versionado
- [ ] 3.3 Varrer `TODO(domain)`, `TODO` e `FIXME` pendentes nos dois projetos e resolver ou registrar cada um
- [ ] 3.4 Analisar dependências sem uso (`dependency:analyze`, `npm ls` e revisão de imports) e remover só as comprovadamente sem uso, com testes verdes

## 4. Documentação
- [ ] 4.1 Atualizar o `README.md` da raiz: stack real, pré-requisitos, comandos validados, Compose, `tools/`, CI e estrutura do monorepo
- [ ] 4.2 Atualizar `api-autorizador-processo/README.md`: endpoints, variáveis de ambiente, testes, ArchUnit e riscos aceitos
- [ ] 4.3 Atualizar `app-autorizador-processo/README.md`: comandos, tipos gerados do OpenAPI, Nginx/proxy, e2e e acessibilidade
- [ ] 4.4 Acrescentar nota em `.specs/design/DESIGN_SYSTEM_DISCOVERY.md` de que o baseline executado substitui os pontos [INFERIDO]
- [ ] 4.5 Atualizar `.specs/INDEX.md` e `.specs/memory/` ao estado real (remover "só documentação" e a lista de pendentes)

## 5. Rastreabilidade e arquivamento
- [ ] 5.1 Conferir `FUNCTIONAL_TRACEABILITY.md`: cada F-item com spec concluída (com evidência) ou fora de escopo declarado; PEN-xx mantidas abertas
- [ ] 5.2 Preencher "Resultado" e arquivar com `git mv` em `.specs/archive/` as specs concluídas ainda fora dele, listando as que ficarem de fora e o motivo
- [ ] 5.3 Escrever `.specs/05-quality/RELATORIO_FINAL.md` com resultados da verificação global, desvios, riscos aceitos e pendências abertas (PEN-xx; SPEC-50 não aplicável)

## 6. Fechamento
- [ ] 6.1 Reexecutar `./mvnw verify`, `npm run lint`, `npm run build` e `npm test` no working tree após as edições e corrigir falhas
- [ ] 6.2 Preencher o "Resultado" da SPEC-43 e arquivá-la com `git mv` para `.specs/archive/`, conforme `como-executar.md` §4
