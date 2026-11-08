# SPEC-30 — Testes ponta a ponta no ambiente completo

| Campo | Valor |
|---|---|
| Domínio / projeto | Integração · `app-autorizador-processo/e2e` |
| Prioridade | P1 |
| Depende de | SPEC-10, SPEC-13, SPEC-17, SPEC-18, SPEC-19, SPEC-20, SPEC-23 |
| Sucessoras | SPEC-40, SPEC-41, SPEC-42, SPEC-43 |
| Paralelismo | nenhum |

## 1. Objetivo
Validar os fluxos completos pela interface real: navegador → Nginx → API → PostgreSQL, via `docker compose`. É o primeiro ponto em que frontend e backend são exercitados juntos.
**Ferramenta:** Playwright (devDependency nova, justificada: único modo de validar a UI contra o ambiente real). **Restrições:** banco limpo e semeado antes da suíte (volume descartável); testes independentes entre si (dados com números únicos); sem dependência de ordem; sem dados reais.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `TEST_STRATEGY.md` §2, `compose.yaml`, `FUNCTIONAL_TRACEABILITY.md`

## 3. Tarefas
- [ ] **T1.** Instalar e configurar Playwright em `app-autorizador-processo/` (Chromium; viewport desktop e móvel), URL base `http://localhost:${APP_PORT:-4200}`.
- [ ] **T2.** Fixture de ambiente: `docker compose -p e2e up -d --build` com `.env` de teste (usuário *bootstrap* e `JWT_SECRET` descartáveis, TTL curto para o cenário de expiração) e perfil de seed; espera por `healthy`; derrubada com `down -v`.
- [ ] **T3.** Cenários: (0) login inválido mostra erro, login válido entra, rota protegida sem sessão vai ao login com `retorno`, sair encerra a sessão e sessão expirada (TTL curto no ambiente de teste) volta ao login; (1) painel mostra totais da seed; (2) cadastrar procedimento completo (4 etapas) e vê-lo na lista; (3) duplicidade mostra erro no campo; (4) solicitar autorizado (4567/20/F) → "Autorizado" → aparece no histórico e no painel; (5) solicitar negado (1234/30/M) → "Negado" e histórico inalterado; (6) procedimento inexistente via `?numero=` forjado → erro tratado; (7) API parada → modal "Não foi possível carregar" e retomada ao religar; (8) rota SPA direta (`/autorizacoes`) funciona após reload; (9) viewport 320 px sem rolagem horizontal.
- [ ] **T4.** Relatório HTML/JUnit e capturas em falha, fora do git.
- [ ] **T5.** Documentar execução local (`npm run e2e`).

**Aceite:** todos os cenários passam contra o Compose limpo, em desktop e móvel.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T5 | `angular-architect` (testes), `nw-bdd-methodology` (cenários G/W/T); Playwright; Docker |

## 5. Verificação obrigatória
- `npm run e2e` em `app-autorizador-processo/` → 10 cenários verdes
- Teardown remove contêineres e volume do projeto `e2e`
- Definition of Done: fluxos de F01–F13 exercitados ponta a ponta; relatório anexado à evidência.

## 6. Resultado
_Preencher ao concluir._
