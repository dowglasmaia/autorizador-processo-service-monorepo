# Tarefas — Baseline golden cases

## 1. Ponto de partida e ambiente
- [x] 1.1 Confirmar que a tag `v0-original` existe (`git tag -n1 v0-original`) e que o Docker responde (`docker version`); registrar bloqueio se algo faltar
- [x] 1.2 Registrar o desvio: `.specs/06-execution-plan/` não existe
- [x] 1.3 Extrair a tag com `git archive v0-original` para um diretório temporário fora do repositório

## 2. Versão original em contêiner
- [x] 2.1 Subir `maven:3.6.3-jdk-8` com o temporário montado, `mvn spring-boot:run`, porta 18080, perfil `test` (usar `MSYS_NO_PATHCONV=1`)
- [x] 2.2 Aguardar a subida e confirmar `GET /procedimentos` com os 3 itens da semente (1234, 4567, 6789)

## 3. cases.json
- [x] 3.1 Sondar a original com `curl` para conferir B01–B17 contra o baseline e anotar campos estáveis e voláteis
- [x] 3.2 Criar `tools/parity/cases.json` com `id`, `descricao`, requisição original, resposta original observada e normalizações dos casos B01–B17
- [x] 3.3 Acrescentar `mapeamentoV1` conforme `API_CONTRACTS.md` §4 e `divergencia: DIV-xx` (DIV-01 a DIV-07 nos casos B01–B17; DIV-08 entra pela 3.4 em `adicionais`)
- [x] 3.4 Acrescentar a seção `adicionais` (`apenasV1`): `sexo:"Masculino"` → 400 e rota sem token → 401

## 4. replay.mjs
- [x] 4.1 Criar `tools/parity/replay.mjs` (Node ≥ 20, sem dependências) com leitura de `--target`, `--base-url`, `--only`, `--email` e `--senha` e validação dos argumentos
- [x] 4.2 Implementar o login da v1 (`/api/v1/auth/login`, Bearer) e execução sem autenticação para a original
- [x] 4.3 Implementar execução ordenada, comparação por campos estáveis (conjuntos sem ordem), setup/limpeza próprios e aviso se a semente não estiver limpa
- [x] 4.4 Imprimir a tabela `caso · esperado · obtido · OK/FALHA` e sair com código ≠ 0 em falha

## 5. Execução e documentação
- [x] 5.1 Executar `node tools/parity/replay.mjs --target original --base-url http://localhost:18080` e confirmar exit 0 com B01–B17 OK; corrigir os casos se algo falhar
- [x] 5.2 Confirmar a falha esperada: repetir a execução sem reiniciar o contêiner e ver o aviso/falha de base suja
- [x] 5.3 Criar `tools/parity/README.md`: subir a original, replicar, política de divergências (DIV), como adicionar caso e saída resumida da execução

## 6. Limpeza e verificação final
- [x] 6.1 Parar e remover o contêiner e apagar o diretório temporário; confirmar que não há processos nem contêineres restantes
- [x] 6.2 Verificar `grep -ri "legado\|legacy" tools/` sem resultados e `git status` sem arquivo rastreado alterado
- [x] 6.3 Registrar a evidência por tarefa e o resumo, e arquivar a spec com `git mv` conforme `como-executar.md`

## Evidência do lote 1
- 1.1 Tag `v0-original` presente ("Versão original do backend (Spring Boot 2.2.6)..."); Docker 29.9.2 respondendo.
- 1.2 `.specs/06-execution-plan/` não existe (confirmado com `ls`); desvio registrado na proposta.
- 1.3 `git archive v0-original` extraído em diretório temporário do `%TEMP%` (fora do repositório).
- 2.1/2.2 Contêiner `baseline-original` (`maven:3.6.3-jdk-8`, 18080→8080, perfil `test`) subiu; `GET /procedimentos` devolveu 1234, 4567, 6789.
- 3.1 B01–B17 sondados com `curl`; todos coincidem com o baseline (status e mensagens). Voláteis: `timestamp` (ISO ou epoch ms), `trace`, ids de linha, `Location`.
- 3.2/3.3 `tools/parity/cases.json` criado com 17 casos, `mapeamentoV1` e `divergencia` (DIV-01..07). Validado como JSON.
- Contêiner e diretório temporário removidos ao fim do lote (a base ficou suja pelos B02–B06); a 5.1 sobe contêiner novo.

## Evidência do lote 2
- 3.4 Seção `adicionais` com A01 (`sexo:"Masculino"` → 400, DIV-08) e A02 (sem token → 401), ambos `apenasV1`.
- 4.1–4.4 `tools/parity/replay.mjs` criado (Node 24, sem dependências): argumentos validados, login v1 com Bearer, ordem por `ordem`, comparação por campos estáveis/conjuntos, conferência da semente, tabela e exit ≠ 0 em falha.
- 5.1 Contêiner novo: `replay.mjs --target original` → 17/17 OK, exit 0.
- 5.2 Segunda execução sem reiniciar: aviso de base suja, B01/B02/B06 e outros em FALHA, exit 1.
- 5.3 `tools/parity/README.md` criado.
- Contêiner `baseline-original` e diretório temporário removidos; sem "legado/legacy" em `tools/`. A 6.1 só precisa reconfirmar.

## Evidência do lote 3
- 6.1 `docker ps -a` sem contêiner `baseline-original` (restam apenas contêineres de outros projetos, não tocados); `%TEMP%` sem diretório de extração da tag; nenhum processo `spring-boot`/`mvn` ativo.
- 6.2 `grep -ri "legado\|legacy" tools/` sem resultados. `git status`: nenhum arquivo rastreado fora da spec/loop alterado; `tools/` é novo (não rastreado, entregue pelo commit do devkit).
- 6.3 Evidência registrada; spec arquivada em `.specs/workflows/test-01/specs/arquivo/` (mesma convenção da etapa 1).

## Resumo
Resultado: `tools/parity/` com `cases.json` (B01–B17 + A01/A02 `apenasV1`, DIV-01..08), `replay.mjs` (Node, sem dependências) e `README.md`. Validação: `replay.mjs --target original` → 17/17 OK, exit 0; segunda execução sem reiniciar → falha por base suja, exit 1. Limitações aceitas: casos não idempotentes na original (exige contêiner novo); limpeza na v1 é "melhor esforço" até a SPEC-31; execução contra a v1 fica para a SPEC-31.
