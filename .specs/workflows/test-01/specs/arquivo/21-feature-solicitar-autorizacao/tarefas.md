# Tarefas — Tela solicitar autorização

## 1. Base do módulo
- [x] 1.1 Ler `INDEX.md`, memória, `como-executar.md`, SPEC-18 e o `angular-module-scaffold`; conferir o que a etapa 20 entregou (`ProcedimentosService`, link `?numero=`) e seguir a estrutura de `features/login`.
- [x] 1.2 Ajustar `solicitar.routes.ts` (rota lazy e título) e criar `doc.md` do módulo.

## 2. Acesso a dados e sessão
- [x] 2.1 Criar `data-access/autorizacao.service.ts` com `solicitarAutorizacao` usando `resultadoNegocio()` e retornando `{resultado, autorizacao?, detail?}`.
- [x] 2.2 Obter a lista do seletor (`size=200`, `sort=numProcedimento,asc`) com método próprio em `autorizacao.service.ts`, sem alterar o estado de `ProcedimentosService`.
- [x] 2.3 Criar `core/sessao/sessao-solicitacoes.ts` com signals `concedidas` e `negadas` e método de registro.
- [x] 2.4 Testar serviço e sessão com `HttpTestingController` (201, 403, 404, 400, rede).

## 3. Formulário
- [x] 3.1 Usar o `SegmentedChoice` de `shared/ui` para M/F, garantindo `aria-pressed` e teclado (ajustar com teste se faltar).
- [x] 3.2 Criar o `FormGroup` tipado: procedimento obrigatório, idade inteira 0..150, sexo obrigatório.
- [x] 3.3 Montar o seletor "Nº n" (+ nome quando existir), com estados de carga e erro.
- [x] 3.4 Pré-selecionar o procedimento por `?numero=` e implementar o botão **Limpar**.
- [x] 3.5 Exibir mensagens de validação por campo.

## 4. Resultado
- [x] 4.1 Criar o painel de resultado com os 3 estados, ícones e `aria-live="polite"`.
- [x] 4.2 Texto "Procedimento n liberado para paciente masculino/feminino de N anos" e link "Ver autorizações concedidas".
- [x] 4.3 Cartão Negado com a mensagem do backend e a nota "A regra não informa qual critério falhou", sem toast.
- [x] 4.4 Animação curta respeitando `prefers-reduced-motion`.
- [x] 4.5 Atualizar os contadores de sessão a cada resultado.

## 5. Erros
- [x] 5.1 Tratar `PROCEDIMENTO_NAO_ENCONTRADO`: erro no seletor e recarga da lista.
- [x] 5.2 Mapear `VALIDACAO_INVALIDA` aos campos.
- [x] 5.3 Modal "Tentar novamente" para rede/servidor preservando o formulário.

## 6. Testes e fechamento
- [x] 6.1 Testar o componente: autorizado, negado, inexistente, validação, rede, `?numero=`, Limpar e contadores.
- [x] 6.2 Completar o `doc.md` com as operações consumidas (`solicitarAutorizacao`, `listarProcedimentos`).
- [x] 6.3 Rodar `npm run lint`, `npm run build` e `npm test` e corrigir falhas.
- [x] 6.4 Registrar evidência, desvio de `06-execution-plan` e `git mv` da SPEC-18 para `.specs/archive/`.
