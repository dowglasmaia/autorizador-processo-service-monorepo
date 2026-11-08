## 1. Preparação
- [x] 1.1 Ler `SPEC-31`, `tools/parity/README.md`, `BASELINE_COMPORTAMENTO_ORIGINAL.md` e `API_CONTRACTS.md` §4; registrar a ausência de `.specs/06-execution-plan/`.
- [x] 1.2 Conferir o estado do repositório (etapa 25 entregue e `docker-compose` disponível, `cases.json` íntegro, Docker disponível) e revisar esta proposta contra o que mudou.

## 2. Execução contra a v1
- [x] 2.1 Subir `postgres` e `api` pelo Compose com `.env` descartável (usuário bootstrap, `JWT_SECRET`) e esperar `healthy`.
- [x] 2.2 Confirmar a seed (1234, 4567, 6789) via `GET /api/v1/procedimentos` autenticado.
- [x] 2.3 Executar `node tools/parity/replay.mjs --target v1 --base-url http://localhost:8080 --email … --senha …` e guardar a saída resumida.
- [x] 2.4 Executar também os grupos `--only procedimento`, `autorizacao` e `infra` se o resultado completo falhar, para isolar a causa.

## 3. Relatório
- [x] 3.1 Criar `tools/parity/RELATORIO.md` com tabela caso × original × v1 × status (IDÊNTICO / DIV-xx / FALHA), incluindo A01/A02 e N01–N04.
- [x] 3.2 Conferir que cada DIV citada (01..08) existe em `BASELINE_COMPORTAMENTO_ORIGINAL.md` e registrar a justificativa de cada uma.
- [x] 3.3 Registrar no relatório o comando de reprodução e o desvio de leitura do aceite (DIV nos casos B01, B02 etc.).

## 4. Tratamento de falhas
- [x] 4.1 (causa: caso B03 desatualizado, corrigido em `cases.json`; `replay.mjs` passou a limpar 9997; API intacta, sem teste novo) Para cada FALHA, identificar a causa (API, seed ou caso) e corrigir a implementação da API, com teste, sem alterar o contrato.
- [x] 4.2 (não aplicável: nenhuma decisão de produto envolvida) Se a causa for decisão de produto, criar `aprovacao.md` para nova DIV e não executar a mudança; não ajustar `cases.json` para esconder falha.
- [x] 4.3 Repetir 2.3 em ambiente limpo até zero FALHA e atualizar o relatório.

## 5. Verificação e encerramento
- [x] 5.1 (dispensado: nenhum código da API foi alterado) Rodar `./mvnw verify` em `api-autorizador-processo` caso haja correção de código.
- [x] 5.2 Derrubar o ambiente (`docker compose down -v`) e confirmar que não há processos nem contêineres restantes.
- [x] 5.3 Documentar no `tools/parity/README.md` o uso contra a v1 e a nota de que o job `paridade` fica para a SPEC-40.
- [x] 5.4 Evidência por tarefa e resumo registrados; o arquivamento (`git mv` para `.specs/archive/`) fica a cargo do passo de arquivar do devkit.
