# Tarefas — Catálogo estendido

## 1. Preparação
- [ ] 1.1 Reconferir as classes da etapa 8 listadas na proposta (`Procedimento`, `CadastrarProcedimentoUseCase`, `ProcedimentoEntity`, adaptador, controller, testes); ler `API_CONTRACTS.md` §2, `DATABASE_DESIGN.md` V4 e as etapas 1, 2 e 4 do formulário no protótipo; registrar a ausência de `.specs/06-execution-plan/`.

## 2. Contrato
- [ ] 2.1 Atualizar `openapi/modules/procedimento.yaml` (aditivo): campos novos em `ProcedimentoRequest` e `Procedimento`, enums `Categoria`, `Especialidade`, `TipoCobertura`, `Periodo`, `nome`/`categoria` obrigatórios no request.
- [ ] 2.2 Validar o contrato, regenerar as interfaces e confirmar que `autorizacao.yaml` segue compilando.

## 3. Banco
- [ ] 3.1 Criar `V4__catalogo_estendido.sql` com colunas nullable e CHECKs conforme `DATABASE_DESIGN.md`.
- [ ] 3.2 Espelhar as colunas nas tabelas `procedimento_audit` (só ela) na mesma migração.

## 4. Domínio
- [ ] 4.1 Escrever os testes do domínio (nome 1..200 com trim, par `quantidadeMaxima`/`periodo`, enums) antes da implementação.
- [ ] 4.2 Criar `Categoria`, `Especialidade`, `TipoCobertura`, `Periodo` e estender `Procedimento.create` com os novos dados e invariantes.
- [ ] 4.3 Teste de regressão explícito: `avaliar` inalterado, ignorando todos os campos novos.

## 5. Aplicação e persistência
- [ ] 5.1 Estender o caso de uso de cadastro: padrões `tipoCobertura=PLANO` e booleanos `false`.
- [ ] 5.2 Estender `ProcedimentoEntity` (com `@Audited`) e o mapeamento entidade↔domínio; linhas antigas saem com `null`.
- [ ] 5.3 Testar casos de uso com repositório fake para os novos campos.

## 6. API
- [ ] 6.1 Mapear request/response estendidos no `ProcedimentoController`, inclusive na listagem e no `GET` por id.
- [ ] 6.2 Garantir `VALIDACAO_INVALIDA` com `errors[]` por campo para `nome`, `categoria` e a combinação `quantidadeMaxima`/`periodo`.

## 7. Testes
- [ ] 7.1 Teste de migração com PostgreSQL (Testcontainers): dados de V1–V3 recebem V4 sem perda (contagens iguais) e CHECKs rejeitam valores inválidos.
- [ ] 7.2 `@WebMvcTest` para obrigatórios e combinações inválidas.
- [ ] 7.3 Ponta a ponta: POST estendido e GET devolvem os mesmos valores; 1234 com campos nulos e `POST /autorizacoes` inalterado.
- [ ] 7.4 Teste de auditoria dos novos campos.

## 8. Seed e paridade
- [ ] 8.1 Manter o seed local sem valores novos e documentar a decisão.
- [ ] 8.2 Adicionar a `tools/parity/cases.json` os casos NEW-01 marcados `novo` e rodar o replay.

## 9. Verificação
- [ ] 9.1 Rodar `./mvnw verify` e `check-architecture.mjs` (0 violações); confirmar a suíte da etapa 8 sem alteração de expectativa e nenhum nome "legado/legacy".
- [ ] 9.2 Registrar a evidência curta por tarefa e o resumo conforme `como-executar.md`.
