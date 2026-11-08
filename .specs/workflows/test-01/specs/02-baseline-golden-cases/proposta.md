# Baseline golden cases

Deriva da SPEC-02 (`.specs/05-quality/SPEC-02-baseline-golden-cases.md`). Fontes: `.specs/00-product-discovery/BASELINE_COMPORTAMENTO_ORIGINAL.md` (casos B01–B17 e DIV) e `.specs/01-architecture/API_CONTRACTS.md` §4 (mapeamento para a v1).

## Por quê
O projeto novo precisa preservar o comportamento da versão original. O baseline atual é um documento; falta torná-lo **executável**, para que o mesmo conjunto de casos rode contra a versão original (prova de que os casos estão certos) e depois contra a nova API (SPEC-31). Tem de ser feito antes de remover o código original da raiz (SPEC-03).

## O que muda
- Nasce `tools/parity/` na raiz com:
  - `cases.json`: casos B01–B17, cada um com `id`, `descricao` (Given/When/Then), requisição original, resposta original observada (status e campos estáveis), `mapeamentoV1` (requisição e resultado esperado, conforme `API_CONTRACTS.md` §4) e `divergencia: DIV-xx` quando houver. Campos voláteis (`timestamp`, `id` de linha, ordem de conjuntos, `trace`, host do `Location`) são ignorados ou normalizados.
  - `replay.mjs`: Node ≥ 20, sem dependências, `fetch` nativo. Uso: `--target original|v1 --base-url <url> [--only procedimento|autorizacao|infra] [--email <e> --senha <s>]`. Em `v1` faz login em `/api/v1/auth/login` e usa o Bearer; em `original` não autentica. Imprime a tabela `caso · esperado · obtido · OK/FALHA` e sai com código ≠ 0 se houver falha.
  - `README.md`: como subir a versão original, como replicar, política de divergências (DIV), como adicionar caso e a saída resumida da execução contra a original.
- A versão original é extraída da tag `v0-original` com `git archive` para um diretório temporário **fora do repositório** e iniciada em contêiner `maven:3.6.3-jdk-8` (`mvn spring-boot:run`, porta 18080, perfil `test`). Depois o contêiner e o diretório temporário são removidos.
- Casos adicionais da v1 sem equivalente na original (`sexo:"Masculino"` → 400, DIV-08; rota sem token → 401) ficam numa seção `adicionais` do `cases.json`, marcados `apenasV1`. Não entram na execução contra a original, que continua com os 17 casos.

## O que fica de fora
- Alterar a API/código original (somente leitura; a tag não é mexida).
- Remover o código original da raiz: SPEC-03.
- Executar os casos contra a v1: SPEC-31 (a v1 ainda nem existe).
- Versionar o código original em pasta ou o contêiner no repositório.
- Dockerfile/CI de `tools/parity/` (CI é a SPEC-40).

## Decisões de design
- Nomes de arquivos, casos e scripts usam `baseline`/`original`; nenhum "legado/legacy" em `tools/`.
- A original usa H2 em memória e não tem rota de remoção; logo os casos **não são idempotentes** (B06 envenena a listagem, B07). Cada execução contra a original exige contêiner novo; o `replay.mjs` verifica a semente (B01) e avisa se a base não estiver limpa. Contra a v1, os dados do replay usam números de procedimento próprios e a limpeza é feita pelo que a API permitir.
- B06/B07 e B12 são executados no fim da sequência dentro de cada grupo, para não afetar os demais, sem mudar a numeração.
- Comparação por campos estáveis: status, `message`/`code`, conjuntos comparados sem ordem (`idades` não tem ordem na original; na v1 é ordenada, então o mapeamento v1 compara ordenado).
- Execução no Windows/Git Bash: usar `MSYS_NO_PATHCONV=1` nos comandos `docker` com volumes.
- Se o Docker não estiver disponível, T1/T4/T6 ficam bloqueadas e a etapa não é marcada como concluída.

## Critérios de aceite
- Com a original rodando, `node tools/parity/replay.mjs --target original --base-url http://localhost:18080` sai com 0 e o relatório lista B01–B17, todos OK.
- `cases.json` cobre B01–B17 e todas as DIV (01–08).
- `replay.mjs` não tem dependências (sem `package.json` de runtime).
- `grep -ri "legado\|legacy" tools/` sem resultados.
- Contêiner e diretório temporário removidos; nenhum processo ativo; nenhum arquivo da original alterado.

## Decisões tomadas sem consulta
- A pasta `.specs/06-execution-plan/` não existe; a evidência fica em `tarefas.md` e no resumo da spec arquivada. Sem necessidade de revisão.
- Casos adicionais (DIV-08, 401) foram incluídos como `apenasV1`, pois o API_CONTRACTS §4 os lista e o aceite pede cobertura de todas as DIV; a execução contra a original continua com 17 casos. **[REVISAR]** confirmar que esse acréscimo está dentro do pretendido.
- A spec original numera T1–T6; foi reorganizada em grupos em `tarefas.md` (sem mudar escopo), com a verificação do aceite e da ausência de "legado" como tarefas próprias.
- Depende de a etapa 1 ter criado a tag `v0-original`; se não existir ao executar, a tarefa 1.1 registra bloqueio em vez de criar a tag.
