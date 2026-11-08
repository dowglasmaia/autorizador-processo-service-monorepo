# SPEC-21 — Microcopy, acessibilidade e revisão de UX

| Campo | Valor |
|---|---|
| Domínio / projeto | Frontend · transversal |
| Prioridade | P2 |
| Depende de | SPEC-17, SPEC-18, SPEC-19, SPEC-20, SPEC-23 |
| Sucessoras | SPEC-43 |
| Paralelismo | nenhum |

## 1. Objetivo
Passe final de qualidade sobre as telas prontas: catálogo único de mensagens em pt-BR, revisão de acessibilidade WCAG 2.2 AA e consistência visual com o protótipo. Corrige as inconsistências conhecidas de mensagens originais (grafia: "ná Cadastrado", "Integridades", "Nº") nos textos exibidos.
**Restrições:** não mudar regras nem contrato; textos de produto, não de marketing; sem novas dependências; alterações restritas à camada de apresentação.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `NON_FUNCTIONAL_REQUIREMENTS.md` (NFR-05, 06, 09, 12), telas implementadas, `design/DESIGN_SYSTEM_DISCOVERY.md` §9 (mapa resposta → UI); skills `angular-tailwind-ui-specialist` (`ui-review-checklist.md`), `ui-ux-pro-max`, `copywriting`

## 3. Tarefas
- [ ] **T1.** `core/i18n/mensagens.ts`: catálogo tipado (títulos, ajudas, erros por `code`, vazios, toasts) usado por todas as telas; remover textos soltos.
- [ ] **T2.** Revisar cada mensagem: voz consistente, ação clara, sem jargão técnico ("API", "perfil test") em textos ao usuário; o rodapé "localhost:8080 · perfil test" do protótipo vira estado genérico da API.
- [ ] **T3.** Auditoria a11y por tela: rótulos, ordem de foco, `aria-live`, contraste, alvos de toque ≥ 44 px, teclado em chips/etapas/diálogo (foco preso e retorno), landmarks, `lang="pt-BR"`, título de página por rota.
- [ ] **T4.** Auditoria responsiva em 320/768/1280 px e com zoom 200 %; corrigir rolagem horizontal.
- [ ] **T5.** Teste automatizado de a11y (axe ou equivalente já disponível) nas 6 rotas (incluindo `/login`); registrar violações restantes e justificativas.
- [ ] **T6.** Atualizar `doc.md` dos módulos afetados.

**Aceite:** *Given* as 6 rotas (incluindo `/login`) · *When* auditoria automatizada · *Then* 0 violações críticas/sérias; *Given* teclado apenas · *Then* todos os fluxos (cadastrar, solicitar) são concluíveis.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T2 | `copywriting` |
| T3–T6 | `angular-tailwind-ui-specialist`, `ui-ux-pro-max`, `angular-architect` |

## 5. Verificação obrigatória
- `npm run build && npm test && npm run lint` → verdes
- Relatório a11y automatizado anexo à evidência; capturas nos 3 tamanhos
- `grep -rn "perfil test\|ná Cadastrado\|Integridades" app-autorizador-processo/src` → vazio
- Definition of Done: catálogo único em uso, auditoria executada, pendências a11y justificadas.

## 6. Resultado
_Preencher ao concluir._
