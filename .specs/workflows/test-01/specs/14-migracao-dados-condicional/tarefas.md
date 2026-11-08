# Tarefas — Migração de dados condicional

## 1. Verificar a condição de início
- [ ] 1.1 Ler a SPEC-50, `DATABASE_DESIGN.md` §4 e `BASELINE_COMPORTAMENTO_ORIGINAL.md` §2 e confirmar o estado do ponto de partida (registrar desvio da pasta `06-execution-plan/` inexistente).
- [ ] 1.2 Procurar no repositório bases, dumps ou SQL de origem (`*.mv.db`, `*.dump`, `*.bak`, `*.sql` fora de `db/migration` e `db/seed`) e registrar comandos e resultado.
- [ ] 1.3 Procurar configuração de conexão com a origem em `.env.example`, `compose.yaml` e variáveis de ambiente (sem copiar valores) e registrar o resultado.
- [ ] 1.4 Registrar a decisão (ramo A "não aplicável" ou ramo B "executar") com a evidência de 1.2 e 1.3.

## 2. Ramo A — não aplicável
- [ ] 2.1 Preencher a seção "Resultado" da SPEC-50 com o motivo do cancelamento sem necessidade, a evidência e a data.
- [ ] 2.2 Atualizar `.specs/INDEX.md` (linha de `02-backend/`) indicando SPEC-50 não aplicável e arquivada em `.specs/archive/`.
- [ ] 2.3 Confirmar que nenhum dado, credencial ou dump foi adicionado ao repositório e que não há o termo "legado/legacy" no backend.

## 3. Ramo B — somente se houver base acessível
- [ ] 3.1 Criar `aprovacao.md` descrevendo o acesso à base de origem (somente leitura) e encerrar a sessão até a decisão humana.
- [ ] 3.2 Implementar `tools/importacao/importar.mjs` com as regras da SPEC-50 T3 (sexos 1→M, 2→F, rejeição dos demais, de-duplicação, idempotência).
- [ ] 3.3 Gerar o relatório de contagens origem × destino e a lista de rejeições, falhando em divergência não justificada.
- [ ] 3.4 Testar com origem sintética (3 procedimentos, 2 autorizações), importando duas vezes, e conferir via API.

## 4. Verificação e encerramento
- [ ] 4.1 Rodar `./mvnw verify` em `api-autorizador-processo` e confirmar que segue verde (sem mudança de código).
- [ ] 4.2 Escrever o resumo (resultado, validações, limitações) e arquivar a SPEC-50 com `git mv` em `.specs/archive/`.
