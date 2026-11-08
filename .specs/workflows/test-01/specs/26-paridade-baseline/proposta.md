# Paridade com o baseline

Etapa 26 de 31 · Fase 4 · derivada da `SPEC-31` (`.specs/04-integration/SPEC-31-paridade-baseline.md`). Referência: `SPEC-02` (golden cases, já entregue em `tools/parity/`). Revisada contra a base após a etapa 25 (SPEC-30 entregue e arquivada).

## Objetivo
Provar que a nova API (`api-autorizador-processo`) preserva o comportamento da versão original: executar os golden cases de `tools/parity/` contra a v1 com a seed local e classificar cada caso como **IDÊNTICO**, **DIV-xx aprovada** ou **FALHA**. Divergência inesperada vira correção da implementação, não exceção.

## Ponto de partida (conferido após a etapa 25)
- `tools/parity/` tem `README.md`, `cases.json` (B01–B17, adicionais A01/A02 só da v1 e N01–N04) e `replay.mjs` (Node, sem dependências; `--target v1`, `--email`/`--senha`, `--only`).
- A tag `v0-original` existe; `compose.yaml` na raiz sobe `postgres`, `api` e `app`; a seed local (1234, 4567, 6789) chega via perfil `local`, que é o padrão.
- Não existe `tools/parity/RELATORIO.md`. `.specs/06-execution-plan/` não existe (desvio a registrar).
- A SPEC-31 cita "nova DIV" e "B01, B02, B04, B09–B11, B15 IDÊNTICO quanto à decisão de negócio", mas no `cases.json` esses casos já têm DIV (ex.: B01 DIV-07, B02 DIV-03). A classificação deve distinguir decisão de negócio idêntica de forma divergente aprovada.

- Etapa 25 entregue: Playwright em `app-autorizador-processo/e2e` sobe o projeto Compose `e2e` com `.env` descartável em `e2e/.tmp` (ignorado pelo git). A máquina só tem o binário `docker-compose` v5.6.1 (sem o plugin `docker compose`); Docker 29.9.2 está disponível. O perfil `local` é o padrão da API, então a seed já chega pelo Compose.

## Escopo
1. Subir `postgres` e `api` com seed local (projeto Compose descartável, `.env` de teste, segredos fora do git) e rodar `replay.mjs --target v1` autenticado.
2. Gerar `tools/parity/RELATORIO.md` (caso × original × v1 × status), versionado, sem credenciais nem dados reais.
3. Tratar cada FALHA: corrigir a implementação da API e repetir até zero falhas. `cases.json` só muda para refletir DIV já aprovada (DIV-01..08), nunca para esconder falha.
4. Conferir que toda DIV citada existe em `BASELINE_COMPORTAMENTO_ORIGINAL.md`.
5. Derrubar o ambiente (`down -v`) ao final.

## Fora do escopo
- Job `paridade` no pipeline: pertence à SPEC-40 (fica para a etapa de CI); aqui só se deixa o comando documentado. A tarefa T4 da SPEC-31 é adiada com registro.
- Nova DIV (decisão de produto) sem autorização; funcionalidades novas; reexecutar a original além do necessário para o campo "original" do relatório.

## Critérios de aceitação
- `node tools/parity/replay.mjs --target v1 --base-url http://localhost:8080 --email … --senha …` termina com exit 0 e nenhuma linha FALHA.
- `RELATORIO.md` existe, lista todos os casos e cada DIV é de DIV-01..08.
- `./mvnw verify` da API continua verde se houver correção de código.
- Se o Docker estiver indisponível, bloqueio registrado e tarefas dependentes não marcadas.

## Decisões tomadas sem consulta
- **Coluna "original" do relatório** vem do bloco `original` do `cases.json` (já validado na SPEC-02), sem subir a original de novo; só se sobe se houver dúvida sobre um caso. **[REVISAR]**
- **Classificação:** IDÊNTICO = sem `divergencia` ou decisão de negócio igual; DIV-xx = caso com `divergencia` e v1 conforme `mapeamentoV1`; FALHA = qualquer outro resultado. Assim o aceite da SPEC-31 é lido pela decisão de negócio, registrando o desvio acima.
- **Compose:** usar `docker compose` ou, na falta do plugin, `docker-compose`, com as mesmas opções. Porta da API publicada em `API_PORT` (8080).
- **Ambiente:** projeto Compose próprio (`-p paridade`) com credenciais de teste geradas na hora, no mesmo estilo da etapa 25.
- **T4 (job de CI)** adiada para a SPEC-40, por ser fora desta etapa no plano.
