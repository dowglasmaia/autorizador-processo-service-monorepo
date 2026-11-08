# SPEC-02 — Baseline executável de comportamento (golden cases)

| Campo | Valor |
|---|---|
| Domínio / projeto | Qualidade · `tools/parity/` (raiz) |
| Prioridade | P0 |
| Depende de | SPEC-01 |
| Sucessoras | SPEC-03, SPEC-31 |
| Paralelismo | pode rodar junto com SPEC-14 |

## 1. Objetivo
Transformar `BASELINE_COMPORTAMENTO_ORIGINAL.md` em casos **executáveis e reutilizáveis**, obtidos rodando a versão original (tag `v0-original`) em contêiner JDK 8, para servirem de rede de segurança: o mesmo conjunto é reexecutado contra a nova API (SPEC-31). Prioridade absoluta do produto: preservar comportamento. **Fora de escopo:** alterar o código original; o contêiner e as cópias ficam fora do repositório.
**Restrições:** nenhum "legado/legacy" em nomes de arquivos, casos ou scripts (usar `baseline`/`original`); não versionar o código original em pasta.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `00-product-discovery/BASELINE_COMPORTAMENTO_ORIGINAL.md`, `01-architecture/API_CONTRACTS.md` §4

## 3. Tarefas
- [ ] **T1.** Extrair a tag `v0-original` para um diretório **temporário fora do repo** (`git archive`) e iniciá-la com `maven:3.6.3-jdk-8` (`mvn spring-boot:run`, porta 18080, perfil `test`).
- [ ] **T2.** Criar `tools/parity/cases.json`: cada caso B01–B17 com `id`, `descricao`, requisição original, **resposta original observada** (status, campos estáveis) e `mapeamentoV1` (requisição e resultado esperado da v1 conforme `API_CONTRACTS.md` §4), marcando `divergencia: DIV-xx` quando aplicável. Campos voláteis (`timestamp`, `id` de linha, ordem de conjuntos) são ignorados ou normalizados.
- [ ] **T3.** Criar `tools/parity/replay.mjs` (Node ≥ 20, **sem dependências**, `fetch` nativo): `--target original|v1 --base-url <url> [--only procedimento|autorizacao|infra] [--email <e> --senha <s>]` (para `v1`, faz login em `/api/v1/auth/login` e usa o Bearer; para `original`, não autentica); executa os casos na ordem, com setup/limpeza de dados próprios, e imprime tabela `caso · esperado · obtido · OK/FALHA`; código de saída ≠ 0 se houver falha.
- [ ] **T4.** Executar `replay.mjs --target original` contra o contêiner e confirmar 100 % de aderência com o baseline (valida os próprios casos). Registrar também a saída bruta resumida em `tools/parity/README.md`.
- [ ] **T5.** Documentar em `tools/parity/README.md`: como subir a versão original, como replicar, política de divergências (DIV) e como adicionar caso.
- [ ] **T6.** Parar e remover o contêiner e o diretório temporário.

**Aceite (Given/When/Then):** *Given* a versão original em execução · *When* `node tools/parity/replay.mjs --target original --base-url http://localhost:18080` · *Then* todos os casos passam e o relatório lista B01–B17.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1, T4, T6 | Docker |
| T2–T3, T5 | `nw-bdd-methodology` (estilo Given/When/Then dos casos); nenhuma outra |

## 5. Verificação obrigatória
- `node tools/parity/replay.mjs --target original --base-url http://localhost:18080` → exit 0, 17 casos OK
- `grep -ri "legado\|legacy" tools/` → sem resultados
- Definition of Done: `cases.json` cobre todos os B01–B17 e todas as DIV; `replay.mjs` sem dependências; evidência da execução registrada.

## 6. Resultado
_Preencher ao concluir._
