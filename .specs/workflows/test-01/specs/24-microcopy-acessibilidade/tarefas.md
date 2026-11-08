# Tarefas — Microcopy e acessibilidade

## 1. Catálogo de mensagens
- [ ] 1.1 Inventariar textos soltos em templates e componentes de todas as telas e no shell (planilha curta na evidência).
- [ ] 1.2 Criar `core/i18n/mensagens.ts` tipado (títulos, ajudas, erros por `code`, vazios, toasts, rótulos de ação).
- [ ] 1.3 Migrar `features/login/mensagens-login.ts` e as demais telas (login, painel, procedimentos, solicitar, autorizações, shell, 404) para o catálogo.
- [ ] 1.4 Teste do catálogo: todo `code` de erro do contrato tem mensagem; código desconhecido usa a genérica.

## 2. Revisão de microcopy
- [ ] 2.1 Revisar voz, clareza e ação em cada mensagem; remover jargão técnico ("API", "perfil test").
- [ ] 2.2 Conferir o texto do estado da API no shell (já genérico) e corrigir grafia ("Nº"), conferindo o grep de termos proibidos.
- [ ] 2.3 Revisar estados vazios e de erro de todas as telas (texto + ação "Tentar novamente"/próximo passo).

## 3. Acessibilidade
- [ ] 3.1 Definir `lang="pt-BR"` em `index.html` e conferir título por rota e landmarks (`header`, `nav`, `main`) no shell.
- [ ] 3.2 Auditar rótulos, `aria-live`, ordem de foco e foco visível por tela (incluindo painel e gráficos com alternativa textual).
- [ ] 3.3 Garantir foco preso e retorno do foco no diálogo; teclado em chips, seletor de sexo e etapas.
- [ ] 3.4 Medir contraste AA dos tokens e corrigir apenas os que falharem.
- [ ] 3.5 Garantir alvos de toque ≥ 44 px em botões, chips e links de ação.

## 4. Responsividade
- [ ] 4.1 Auditar 320/768/1280 px e zoom 200 %; corrigir rolagem horizontal (tabelas, KPIs, formulários).

## 5. Testes e documentação
- [ ] 5.1 Criar teste automatizado de a11y nas 6 rotas (sem nova dependência) e registrar violações restantes com justificativa.
- [ ] 5.2 Testar o fluxo só com teclado (cadastrar procedimento e solicitar autorização).
- [ ] 5.3 Atualizar os `doc.md` dos módulos afetados.
- [ ] 5.4 Rodar `npm run lint`, `npm run build` e `npm test` e corrigir falhas; registrar evidência e o desvio de `.specs/06-execution-plan/`.
