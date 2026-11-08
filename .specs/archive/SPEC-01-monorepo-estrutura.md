# SPEC-01 — Estrutura do monorepo

| Campo | Valor |
|---|---|
| Domínio / projeto | Arquitetura · raiz do repositório |
| Prioridade | P0 (primeira) |
| Depende de | — |
| Sucessoras | SPEC-02, SPEC-03, SPEC-14 |
| Paralelismo | nenhum (primeira da cadeia) |
| Início quando | execução autorizada pelo responsável (registrar a fonte) |

## 1. Objetivo
Preparar a raiz para o monorepo sem perder o histórico nem o protótipo: marcar a versão original com a tag `v0-original`, mover o protótipo para `.specs/design/prototype/`, alinhar `.gitignore`, criar o `README.md` do monorepo e registrar a estrutura. **Não** remove o código original (isso ocorre em SPEC-03, depois do baseline).
Contexto: a raiz mistura projeto original, protótipo e arquivos novos não rastreados (`compose.yaml`, `.env.example`, `.specs/`). **Fora de escopo:** gerar backend/frontend.
**Restrições:** nenhum diretório ou arquivo com "legado/legacy"; preservar alterações não rastreadas existentes.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `.specs/01-architecture/MONOREPO_STRUCTURE.md`, `.specs/01-architecture/DECISIONS.md` (ADR-01)

## 3. Tarefas
- [ ] **T1.** Confirmar estado: `git status` limpo para arquivos rastreados; registrar a lista de não rastreados.
- [ ] **T2.** Criar a tag anotada `v0-original` no commit atual (`HEAD`) preservando o código original. *Aceite:* `git show v0-original:pom.xml` mostra Spring Boot 2.2.6.
- [ ] **T3.** Mover `Sistema Autorizador com Dashboard/` para `.specs/design/prototype/` (conteúdo intacto, incluindo `uploads/` e `_ds/`). *Aceite:* `Autorizador.dc.html` abre em `.specs/design/prototype/`; pasta antiga não existe.
- [ ] **T4.** Atualizar `.gitignore` da raiz: `target/`, `node_modules/`, `dist/`, `.angular/`, `.env`, `*.log`, IDEs; manter `!.mvn/wrapper/maven-wrapper.jar`.
- [ ] **T5.** Criar `README.md` do monorepo (visão, estrutura, como subir com Compose, links para `.specs/`), **mantendo** os créditos e links do README atual. Sem afirmar o que ainda não existe: marcar "em construção".
- [ ] **T6.** Atualizar `.specs/INDEX.md` (status do conjunto) e registrar a conclusão em `06-execution-plan/STATE.md`.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T4, T6 | git; nenhuma skill |
| T5 | `copywriting` (apenas tom do README; sem marketing) |

## 5. Verificação obrigatória
- `git tag -n1 v0-original` → tag existe com mensagem
- `git ls-files --others --exclude-standard` → não lista `node_modules`/`target`; protótipo em `.specs/design/prototype/`
- `grep -ri "legado\|legacy" --include=* . -l` (excluindo `.specs/` e `.git`) → nenhum resultado
- Definition of Done: tarefas com evidência; nenhum arquivo rastreado removido; README coerente com `MONOREPO_STRUCTURE.md`.

## 6. Resultado
Concluída em 2026-11-08 pela etapa `01-estrutura-monorepo` do loop `test-01` (15 tarefas concluídas, nenhuma pendente). Evidência por tarefa em `.specs/workflows/test-01/specs/arquivo/01-estrutura-monorepo/tarefas.md`; desvios e decisões em `proposta.md` da mesma pasta. Verificação global final (checkout limpo, Compose, e2e, paridade, contrato) em `.specs/05-quality/RELATORIO_FINAL.md`.
