## 1. Preparação
- [ ] 1.1 Ler `SPEC-31`, `tools/parity/README.md`, `BASELINE_COMPORTAMENTO_ORIGINAL.md` e `API_CONTRACTS.md` §4; registrar a ausência de `.specs/06-execution-plan/`.
- [ ] 1.2 Conferir o estado do repositório (etapa 25 entregue e `docker-compose` disponível, `cases.json` íntegro, Docker disponível) e revisar esta proposta contra o que mudou.

## 2. Execução contra a v1
- [ ] 2.1 Subir `postgres` e `api` pelo Compose com `.env` descartável (usuário bootstrap, `JWT_SECRET`) e esperar `healthy`.
- [ ] 2.2 Confirmar a seed (1234, 4567, 6789) via `GET /api/v1/procedimentos` autenticado.
- [ ] 2.3 Executar `node tools/parity/replay.mjs --target v1 --base-url http://localhost:8080 --email … --senha …` e guardar a saída resumida.
- [ ] 2.4 Executar também os grupos `--only procedimento`, `autorizacao` e `infra` se o resultado completo falhar, para isolar a causa.

## 3. Relatório
- [ ] 3.1 Criar `tools/parity/RELATORIO.md` com tabela caso × original × v1 × status (IDÊNTICO / DIV-xx / FALHA), incluindo A01/A02 e N01–N04.
- [ ] 3.2 Conferir que cada DIV citada (01..08) existe em `BASELINE_COMPORTAMENTO_ORIGINAL.md` e registrar a justificativa de cada uma.
- [ ] 3.3 Registrar no relatório o comando de reprodução e o desvio de leitura do aceite (DIV nos casos B01, B02 etc.).

## 4. Tratamento de falhas
- [ ] 4.1 Para cada FALHA, identificar a causa (API, seed ou caso) e corrigir a implementação da API, com teste, sem alterar o contrato.
- [ ] 4.2 Se a causa for decisão de produto, criar `aprovacao.md` para nova DIV e não executar a mudança; não ajustar `cases.json` para esconder falha.
- [ ] 4.3 Repetir 2.3 em ambiente limpo até zero FALHA e atualizar o relatório.

## 5. Verificação e encerramento
- [ ] 5.1 Rodar `./mvnw verify` em `api-autorizador-processo` caso haja correção de código.
- [ ] 5.2 Derrubar o ambiente (`docker compose down -v`) e confirmar que não há processos nem contêineres restantes.
- [ ] 5.3 Documentar no `tools/parity/README.md` o uso contra a v1 e a nota de que o job `paridade` fica para a SPEC-40.
- [ ] 5.4 Registrar evidência por tarefa, resumo e `git mv` da spec para `.specs/archive/`.
