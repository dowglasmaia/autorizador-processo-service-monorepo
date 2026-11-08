# Tarefas — Baseline golden cases

## 1. Ponto de partida e ambiente
- [ ] 1.1 Confirmar que a tag `v0-original` existe (`git tag -n1 v0-original`) e que o Docker responde (`docker version`); registrar bloqueio se algo faltar
- [ ] 1.2 Registrar o desvio: `.specs/06-execution-plan/` não existe
- [ ] 1.3 Extrair a tag com `git archive v0-original` para um diretório temporário fora do repositório

## 2. Versão original em contêiner
- [ ] 2.1 Subir `maven:3.6.3-jdk-8` com o temporário montado, `mvn spring-boot:run`, porta 18080, perfil `test` (usar `MSYS_NO_PATHCONV=1`)
- [ ] 2.2 Aguardar a subida e confirmar `GET /procedimentos` com os 3 itens da semente (1234, 4567, 6789)

## 3. cases.json
- [ ] 3.1 Sondar a original com `curl` para conferir B01–B17 contra o baseline e anotar campos estáveis e voláteis
- [ ] 3.2 Criar `tools/parity/cases.json` com `id`, `descricao`, requisição original, resposta original observada e normalizações dos casos B01–B17
- [ ] 3.3 Acrescentar `mapeamentoV1` conforme `API_CONTRACTS.md` §4 e `divergencia: DIV-xx` (DIV-01 a DIV-08)
- [ ] 3.4 Acrescentar a seção `adicionais` (`apenasV1`): `sexo:"Masculino"` → 400 e rota sem token → 401

## 4. replay.mjs
- [ ] 4.1 Criar `tools/parity/replay.mjs` (Node ≥ 20, sem dependências) com leitura de `--target`, `--base-url`, `--only`, `--email` e `--senha` e validação dos argumentos
- [ ] 4.2 Implementar o login da v1 (`/api/v1/auth/login`, Bearer) e execução sem autenticação para a original
- [ ] 4.3 Implementar execução ordenada, comparação por campos estáveis (conjuntos sem ordem), setup/limpeza próprios e aviso se a semente não estiver limpa
- [ ] 4.4 Imprimir a tabela `caso · esperado · obtido · OK/FALHA` e sair com código ≠ 0 em falha

## 5. Execução e documentação
- [ ] 5.1 Executar `node tools/parity/replay.mjs --target original --base-url http://localhost:18080` e confirmar exit 0 com B01–B17 OK; corrigir os casos se algo falhar
- [ ] 5.2 Confirmar a falha esperada: repetir a execução sem reiniciar o contêiner e ver o aviso/falha de base suja
- [ ] 5.3 Criar `tools/parity/README.md`: subir a original, replicar, política de divergências (DIV), como adicionar caso e saída resumida da execução

## 6. Limpeza e verificação final
- [ ] 6.1 Parar e remover o contêiner e apagar o diretório temporário; confirmar que não há processos nem contêineres restantes
- [ ] 6.2 Verificar `grep -ri "legado\|legacy" tools/` sem resultados e `git status` sem arquivo rastreado alterado
- [ ] 6.3 Registrar a evidência por tarefa e o resumo, e arquivar a spec com `git mv` conforme `como-executar.md`
