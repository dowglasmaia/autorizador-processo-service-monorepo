# Tarefas — Tela de procedimentos

## 1. Base do módulo
- [x] 1.1 Ler `INDEX.md`, memória, `como-executar.md`, SPEC-17 e o `angular-module-scaffold`; confirmar o front intacto após a etapa 19 e seguir a estrutura de `features/login`.
- [x] 1.2 Criar rotas lazy `''` e `novo` em `procedimentos.routes.ts` com títulos.
- [x] 1.3 Criar mapa de rótulos dos enums (categoria, especialidade, cobertura, período) sobre os tipos gerados.

## 2. Acesso a dados
- [x] 2.1 Criar `data-access/procedimentos.service.ts` com `listarProcedimentos` e `cadastrarProcedimento` via `ApiClient`.
- [x] 2.2 Expor Signals `itens`, `carregando`, `erro`, paginação e busca `q`.
- [x] 2.3 Testar o serviço com `HttpTestingController` (sucesso, vazio, erro).

## 3. Lista
- [x] 3.1 Implementar a página de lista com esqueleto, vazio com CTA, erro e sucesso.
- [x] 3.2 Montar a tabela responsiva (Nº + nome, Categoria, Sexos, Idades, ID) e "Sem nome".
- [x] 3.3 Adicionar "Solicitar →" para `/solicitar?numero=n` e o botão "Novo procedimento".
- [x] 3.4 Testar os quatro estados e a navegação.

## 4. Funções puras
- [x] 4.1 Implementar `faixaParaIdades` (0..150) e `ambosParaSexos`.
- [x] 4.2 Implementar a validação de combinação `quantidadeMaxima`/`periodo`.
- [x] 4.3 Testar as funções, incluindo limites e faixa invertida.

## 5. Cadastro em 4 etapas
- [x] 5.1 Criar a página com `FormGroup` tipado por etapa e chips de navegação.
- [x] 5.2 Etapas 1 e 2: identificação e cobertura, com validação por etapa.
- [x] 5.3 Etapa 3: faixa de idade, sexo, chips removíveis e aviso RN9.
- [x] 5.4 Etapa 4: utilização, flags pré-selecionadas, texto de apoio PEN-02 e resumo antes de salvar.
- [x] 5.5 Montar o `ProcedimentoRequest` e salvar com toast e redirecionamento.

## 6. Erros
- [x] 6.1 Mapear `PROCEDIMENTO_DUPLICADO` ao campo número.
- [x] 6.2 Mapear `VALIDACAO_INVALIDA` aos campos e levar à etapa correspondente.
- [x] 6.3 Modal "Tentar novamente" para rede/servidor preservando o formulário.
- [x] 6.4 Testar cada erro e o fluxo do aceite.

## 7. Fechamento
- [x] 7.1 Escrever o `doc.md` do módulo com as operações consumidas.
- [x] 7.2 Rodar `npm run lint`, `npm run build` e `npm test` e corrigir falhas.
- [x] 7.3 Registrar evidência, desvio de `06-execution-plan` e `git mv` da SPEC-17 para `.specs/archive/`.
  > ✅ 2026-11-08 — Seção "Resultado" da SPEC-17 preenchida (tarefas T1–T7 marcadas, desvio de `06-execution-plan` e adiamento das capturas para a SPEC-30 registrados) e arquivo movido com `git mv` para `.specs/archive/`. Verificação: lint, build e testes (138) verdes no lote anterior; nenhum código alterado neste lote.
