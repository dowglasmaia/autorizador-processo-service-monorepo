# SPEC-08 — Módulo `autorizacao` (solicitar e histórico)

| Campo | Valor |
|---|---|
| Domínio / projeto | Backend · módulo `autorizacao` |
| Prioridade | P1 |
| Depende de | SPEC-07 |
| Sucessoras | SPEC-09, SPEC-18, SPEC-19, SPEC-31, SPEC-50 |
| Paralelismo | pode rodar junto com SPEC-12 (arquivos distintos; ambas leem `procedimento` — não alterar `procedimento` aqui) |

## 1. Objetivo
Implementar a solicitação de autorização e o histórico, preservando RN5–RN8, RN10, RN11 e corrigindo D1 (DIV-01, DIV-05, DIV-08).
Fluxo: carregar procedimento por número → `Procedimento.avaliar(idade, sexo)` → se verdadeiro, gravar `Autorizacao(permitido=true)` e devolver `201`; se falso, `403 AUTORIZACAO_NEGADA` **sem gravar** (RN8).
**Restrições:** nada de dados do paciente na URL nem persistidos (PEN-01); `autorizacao` depende de `procedimento` apenas por casos de uso/domínio (D6); `permitido` sempre `true`; não gravar negativas.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `API_CONTRACTS.md` (autorizacao, erros), `PRODUCT_CONTEXT.md` §6–7, baseline B09–B15; skill `java-spring-create-module`

## 3. Tarefas
- [ ] **T1.** Scaffold do módulo `autorizacao` (`rich`), decisões D1–D6 registradas.
- [ ] **T2.** Domínio: `Autorizacao.conceder(procedimento)`; `AutorizacaoNegadaException` (`code=AUTORIZACAO_NEGADA`). Reutilizar `ProcedimentoNaoEncontradoException`.
- [ ] **T3.** `SolicitarAutorizacaoUseCase` (`@Transactional`): usa `ObterProcedimentoUseCase` por número; sexo normalizado (`m`→`M`); negativa lança exceção e **não** persiste; log de evento de negócio (concedida/negada) sem dados sensíveis.
- [ ] **T4.** `ListarAutorizacoesUseCase` (paginado, mais recentes primeiro) com procedimento carregado em lote (sem N+1).
- [ ] **T5.** Porta `AutorizacaoRepository` + adaptador JPA (`AutorizacaoEntity` com FK; auditoria RN10).
- [ ] **T6.** `AutorizacaoController` implementando a interface gerada.
- [ ] **T7.** Testes: domínio e casos de uso (autorizado, negado por sexo, negado por idade, procedimento sem sexos/idades = RN9, inexistente → 404, sexo minúsculo, `Masculino` → 400), integração PostgreSQL (nada gravado na negativa; duas autorizações do mesmo procedimento = RN11), WebMvc, ponta a ponta.

**Aceite (G/W/T):** *Given* seed · *When* `POST /api/v1/autorizacoes {4567,20,"F"}` · *Then* 201 e `GET /autorizacoes` o lista. *When* `{1234,30,"M"}` · *Then* 403 `AUTORIZACAO_NEGADA` e a contagem de autorizações não muda. *When* `{55555,30,"M"}` · *Then* 404.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T6 | `java-spring-create-module` (obrigatória), `java-modern-engineering` |
| T4–T5, T7 | `java-persistence-performance` |
| T6 | `openapi-rest-contract` |

## 5. Verificação obrigatória
- `check-architecture.mjs` → 0 violações; `./mvnw verify` → verde
- `node tools/parity/replay.mjs --target v1 --base-url http://localhost:8080 --only autorizacao --email "$AUTH_BOOTSTRAP_EMAIL" --senha "$AUTH_BOOTSTRAP_SENHA"` → OK
- Contagem de SQL de `GET /autorizacoes` constante (registrar)
- Definition of Done: RN5–RN8, RN11, DIV-01/05/08 cobertas por testes executados.

## 6. Resultado
Concluída em 2026-11-08 pela etapa `10-modulo-autorizacao` do loop `test-01` (20 tarefas concluídas, nenhuma pendente). Evidência por tarefa em `.specs/workflows/test-01/specs/arquivo/10-modulo-autorizacao/tarefas.md`; desvios e decisões em `proposta.md` da mesma pasta. Verificação global final (checkout limpo, Compose, e2e, paridade, contrato) em `.specs/05-quality/RELATORIO_FINAL.md`.
