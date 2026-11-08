# SPEC-19 — Funcionalidade Autorizações (histórico)

| Campo | Valor |
|---|---|
| Domínio / projeto | Frontend · `features/autorizacoes` |
| Prioridade | P2 |
| Depende de | SPEC-15, SPEC-16, SPEC-23, SPEC-08 |
| Sucessoras | SPEC-21, SPEC-30 |
| Paralelismo | junto com SPEC-17, SPEC-18 |

## 1. Objetivo
Tela "Autorizações concedidas" do protótipo: cabeçalho (kicker "Histórico", nota "Somente aprovações são registradas — negativas não deixam rastro"), botão "Nova solicitação", tabela **ID · Procedimento (+nome) · Regras do procedimento · Status "Autorizado" · Data** (a coluna Data usa `criadaEm`; `null` → "—"), paginação, estado vazio "Nada por aqui ainda" + CTA "Solicitar a primeira", erro com nova tentativa.
**Restrições:** somente leitura (sem editar/cancelar); texto de regras construído a partir de `sexos` e `idades` ("Masculino e Feminino · 20, 30 anos"); mais recentes primeiro.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** protótipo (tela Autorizações), `API_CONTRACTS.md` (`listarAutorizacoes`), `FUNCTIONAL_TRACEABILITY.md` F11; skill `angular-module-scaffold`

## 3. Tarefas
- [ ] **T1.** Rotas lazy, `doc.md`, `autorizacoes.service.ts` (`listarAutorizacoes`, Signals `itens/carregando/erro/pagina`).
- [ ] **T2.** Função pura `descreverRegras(procedimento)` e testes (um sexo, dois sexos, sem idades — RN9 "Sem idades cadastradas").
- [ ] **T3.** Página com estados carregando/vazio/erro/sucesso; `DataTable` responsiva; paginação por teclado.
- [ ] **T4.** Link "Nova solicitação" e retorno vindo de SPEC-18.
- [ ] **T5.** Testes de componente e serviço.

**Aceite:** *Given* 2 autorizações para 4567 · *When* abrir `/autorizacoes` · *Then* 2 linhas, mais recente primeiro. *Given* nenhuma · *Then* estado vazio com CTA.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T5 | `angular-module-scaffold`, `angular-architect` |
| T3 | `angular-tailwind-ui-specialist` |

## 5. Verificação obrigatória
- `npm run build && npm test && npm run lint` → verdes
- Verificação contra Compose com dados criados por SPEC-18
- Definition of Done: operação `listarAutorizacoes` coberta; 4 estados verificados; `doc.md` atualizado.

## 6. Resultado
_Preencher ao concluir._
