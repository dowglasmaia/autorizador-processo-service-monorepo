# Microcopy e acessibilidade

Etapa 24 de 31 · Fase 3 · derivada da `SPEC-21` (`.specs/03-frontend/SPEC-21-microcopy-acessibilidade.md`). Escrita antecipadamente, enquanto a etapa 23 (painel) é implementada; será revisada contra o que a 23 entregar.

## Objetivo
Passe final de qualidade sobre todas as telas do frontend (`/login`, `/painel`, `/procedimentos`, `/procedimentos/novo`, `/solicitar`, `/autorizacoes`): catálogo único de mensagens em pt-BR, revisão de acessibilidade (WCAG, nível AA), navegação por teclado, foco, contraste, estados vazios e de erro. **Sem novas funcionalidades**, sem mudança de regras nem de contrato; alterações restritas à camada de apresentação.

## Ponto de partida (conferido no código antes da etapa 23)
- Não existe `core/i18n/mensagens.ts`. Há só `features/login/mensagens-login.ts`; os demais textos estão soltos nos templates e componentes.
- `src/index.html` está com `<html lang="en">`; precisa ser `pt-BR`.
- Títulos de página por rota já existem (`'… · Autorizador'`) em todas as rotas.
- Já há `aria-live`/`role="alert"` em `toast-host`, `loading-overlay`, login, procedimentos, procedimento-novo, solicitar e autorizações (a 23 deve acrescentar o painel).
- `shared/ui` tem `dialog`, `chip`, `field`, `data-table`, `empty-state` etc.; foco preso/retorno do diálogo e teclado de chips/etapas ainda não foram auditados.
- Nenhuma biblioteca de a11y (axe) está no `package.json`; testes usam Vitest via `ng test`. Os grep proibidos (`perfil test`, `ná Cadastrado`, `Integridades`) já retornam vazio em `src`; o rodapé "localhost:8080 · perfil test" do protótipo precisa ser conferido no shell.
- `.specs/06-execution-plan/` não existe; desvio a registrar na evidência.

## Escopo
1. Catálogo tipado `core/i18n/mensagens.ts` (títulos, ajudas, erros por `code`, vazios, toasts) usado por todas as telas; migrar `mensagens-login.ts` e remover textos soltos.
2. Revisão de cada mensagem: voz consistente, ação clara, sem jargão técnico ("API", "perfil test", códigos internos); grafia correta ("Nº", sem "Integridades"/"ná Cadastrado"). Rodapé do shell mostra estado genérico da API.
3. Auditoria a11y por tela: rótulos, ordem de foco, `aria-live`, contraste, alvos de toque ≥ 44 px, teclado em chips/etapas/diálogo (foco preso e retorno), landmarks, `lang="pt-BR"`, título por rota, incluindo o painel e seus gráficos (alternativa textual).
4. Auditoria responsiva em 320/768/1280 px e zoom 200 %; corrigir rolagem horizontal.
5. Teste automatizado de a11y nas 6 rotas, registrando violações restantes com justificativa.
6. Atualizar `doc.md` dos módulos afetados.

## Fora do escopo
- Novas funcionalidades, regras, endpoints ou alterações de contrato/backend.
- Novas dependências (a SPEC-21 proíbe); e2e e comparação visual contra o Compose (SPEC-30); textos de marketing.
- Conteúdo da SPEC-43 (sucessora).

## Critérios de aceitação
- *Given* as 6 rotas · *When* auditoria automatizada · *Then* 0 violações críticas/sérias.
- *Given* apenas teclado · *Then* cadastrar procedimento e solicitar autorização são concluíveis, com foco visível, diálogo com foco preso e retorno.
- `grep -rn "perfil test\|ná Cadastrado\|Integridades" app-autorizador-processo/src` vazio.
- Nenhum texto de UI solto: telas consomem o catálogo.
- `npm run lint`, `npm run build` e `npm test` verdes; sem `any`.

## Decisões tomadas sem consulta
- **Nível WCAG:** o plano diz 2.1 AA e a SPEC-21 diz 2.2 AA; adotado **2.2 AA** (inclui o 2.1) e alvo de toque ≥ 44 px, como a spec. [REVISAR]
- **Teste automatizado de a11y sem nova dependência:** o `axe-core` não está instalado e este loop não autoriza instalar. Implementar verificações com Vitest/DOM (nomes acessíveis, `lang`, landmarks, rótulos, ordem de foco, `alt`/`aria-label`, contraste dos tokens calculado). Se o `axe-core` for aceito, é decisão humana. [REVISAR]
- **Catálogo:** objeto `as const` simples em TypeScript, sem biblioteca de i18n e sem `LOCALE_ID`; apenas pt-BR.
- **Erros por `code`:** o catálogo mapeia os `code` já existentes do contrato; código desconhecido cai em mensagem genérica acionável.
- **Capturas 320/768/1280:** feitas se houver navegador disponível; caso contrário, auditoria por CSS/classes e testes, registrando a limitação. [REVISAR]
- **Contraste:** corrigido nos tokens do design system só quando a medição falhar, preservando a identidade visual do protótipo.
