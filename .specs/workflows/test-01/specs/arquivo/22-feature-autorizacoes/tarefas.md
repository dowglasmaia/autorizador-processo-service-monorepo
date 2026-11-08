# Tarefas — Tela de autorizações

## 1. Base do módulo
- [x] 1.1 Ler `INDEX.md`, memória, `como-executar.md`, SPEC-19 e o `angular-module-scaffold`; conferir que a etapa 21 já entregou o link em `/solicitar` e seguir o molde de `features/procedimentos`.
- [x] 1.2 Ajustar `autorizacoes.routes.ts` (rota lazy e título) e criar `doc.md` do módulo.

## 2. Dados e regras
- [x] 2.1 Criar `data-access/autorizacoes.service.ts` com `listarAutorizacoes(pagina)` e signals `itens`, `carregando`, `erro`, `pagina`, `totalPaginas`, `totalItens`.
- [x] 2.2 Criar a função pura `descreverRegras(procedimento)` (um sexo, dois sexos, sem idades).
- [x] 2.3 Testar serviço (200, vazio, erro de rede) e `descreverRegras`.

## 3. Página
- [x] 3.1 Implementar cabeçalho com kicker, nota "negativas não deixam rastro" e botão "Nova solicitação".
- [x] 3.2 Implementar tabela `DataTable` com ID, Procedimento (+nome), Regras, Status "Autorizado" e Data (`null` → "—").
- [x] 3.3 Implementar estados carregando, erro com nova tentativa e vazio com CTA "Solicitar a primeira".
- [x] 3.4 Criar o controle de paginação Anterior/Próxima (botões desabilitados nos limites, operável por teclado) com "Página X de Y" em `aria-live`, e ligá-lo ao serviço.

## 4. Testes e verificação
- [x] 4.1 Testar o componente: 2 linhas em ordem, vazio, erro, `criadaEm` nulo e troca de página.
- [x] 4.2 Rodar `npm run lint`, `npm run build` e `npm test` em `app-autorizador-processo` e corrigir falhas.
- [x] 4.3 Registrar evidência por tarefa, o desvio de `.specs/06-execution-plan/` e arquivar a spec com `git mv`.
