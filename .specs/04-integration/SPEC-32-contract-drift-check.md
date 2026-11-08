# SPEC-32 — Verificação de desvio de contrato (backend × frontend)

| Campo | Valor |
|---|---|
| Domínio / projeto | Integração · contrato |
| Prioridade | P2 |
| Depende de | SPEC-05, SPEC-16 (e SPEC-12 para o contrato final) |
| Sucessoras | SPEC-40 |
| Paralelismo | junto com SPEC-07 |

## 1. Objetivo
Garantir que o frontend nunca fique defasado em relação ao OpenAPI: tipos regenerados devem ser idênticos aos versionados, e as respostas reais da API devem respeitar o contrato.
**Restrições:** o OpenAPI é a fonte; falha aqui bloqueia o merge; sem novas ferramentas além das já adotadas (`openapi-typescript`) e, para validar respostas, uma biblioteca de validação de esquema justificada em `doc.md` (ou validação via testes do backend).

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `API_CONTRACTS.md` §5, `core/http/doc.md` (SPEC-16)

## 3. Tarefas
- [ ] **T1.** Script `scripts/check-contract.mjs` (raiz): regenera tipos em diretório temporário e compara com `shared/api/api-types.ts`; exit ≠ 0 se diferirem.
- [ ] **T2.** Verificação de respostas: no backend, teste que chama cada operação com a aplicação real e valida status e corpo contra o esquema do OpenAPI; no mínimo as 6 operações e os 5 `code` de erro.
- [ ] **T3.** Verificação de compatibilidade: comparar contrato atual com o da tag/branch base e **falhar** em mudança incompatível (remoção/renomeação/obrigatoriedade nova) dentro de `/api/v1`; mudanças aditivas passam.
- [ ] **T4.** Documentar no `README.md` raiz como regenerar tipos e interpretar falhas.

**Aceite:** *Given* um campo removido do YAML sem regenerar tipos · *When* `node scripts/check-contract.mjs` · *Then* falha apontando o arquivo. Campo opcional adicionado + tipos regenerados → passa.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T4 | `openapi-rest-contract` (validate), `angular-module-scaffold` (regras contract-first) |

## 5. Verificação obrigatória
- `node scripts/check-contract.mjs` → exit 0 no estado atual; exit ≠ 0 em adulteração intencional (demonstrar e reverter)
- `./mvnw verify` → testes de conformidade verdes
- Definition of Done: detecção demonstrada nos dois sentidos; integrada ao CI na SPEC-40.

## 6. Resultado
_Preencher ao concluir._
