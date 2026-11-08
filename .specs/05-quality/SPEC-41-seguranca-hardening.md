# SPEC-41 — Revisão de segurança e endurecimento

| Campo | Valor |
|---|---|
| Domínio / projeto | Qualidade · transversal |
| Prioridade | P2 |
| Depende de | SPEC-06, SPEC-11, SPEC-13, SPEC-22, SPEC-23, SPEC-30, SPEC-40 |
| Sucessoras | SPEC-43 |
| Paralelismo | junto com SPEC-42 |

## 1. Objetivo
Verificar cada requisito de `SECURITY_REQUIREMENTS.md` com **evidência executada** e corrigir lacunas dentro do escopo, **incluindo a revisão da autenticação JWT** (algoritmo, segredo, expiração, `iss`, adulteração, limitador de tentativas, armazenamento no frontend, enumeração de usuários). Registrar os riscos aceitos (sem refresh token, limitador em memória, sem papéis).
**Restrições:** correções só de endurecimento (cabeçalhos, configuração, mensagens); qualquer mudança de comportamento volta ao fluxo de autorização; não incluir dados reais nos testes.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `SECURITY_REQUIREMENTS.md`, `DECISIONS.md` (ADR-16, PEN-09..PEN-12)

## 3. Tarefas
- [ ] **T1.** Checklist SEC-01..SEC-12: para cada item, comando/teste executado e resultado.
- [ ] **T2.** Sondagens ativas no Compose: token adulterado, expirado, `alg=none` e `iss` incorreto → `401`; login com e-mail inexistente × senha errada → resposta e tempo equivalentes; `GET /actuator/env|beans|heappump` → 404; erro forçado → sem stack trace; `Origin` não permitida → bloqueada; `-I /` → cabeçalhos do Nginx; corpo com `id` → ignorado/rejeitado; SQL/HTML em campos de texto (`nome`) → armazenado e exibido escapado.
- [ ] **T3.** Varredura de segredos no histórico atual (`git grep`/ferramenta) e confirmação de que a senha MySQL da versão original não migrou.
- [ ] **T4.** Varredura de dependências e de imagens (Maven, npm, imagens base) com registro de resultados; atualizar dependências com vulnerabilidade crítica quando compatível.
- [ ] **T5.** Documentar o modelo de ameaça resumido e os riscos aceitos (sem refresh token, limitador em memória, sem papéis — PEN-10/11/12) no `README.md` raiz, com a recomendação de usar HTTPS na frente do Nginx.

**Aceite:** *Given* o Compose · *When* as sondagens de T2 · *Then* todas passam; nenhum SEC sem evidência.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T5 | `security-review` (revisão), `java-modern-engineering`, `angular-architect`; curl, Docker |

## 5. Verificação obrigatória
- Execução das sondagens de T2 com resultados registrados
- `./mvnw verify` e `npm test` seguem verdes após correções
- Definition of Done: tabela SEC × evidência completa; risco aceito documentado.

## 6. Resultado
_Preencher ao concluir._
