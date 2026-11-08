# Migração de dados condicional

Etapa 14 de 31 · Fase 2 · derivada da `SPEC-50` (`.specs/02-backend/SPEC-50-migracao-dados-original.md`). Proposta antecipada na etapa 13 e revisada nesta etapa contra o código atual (etapas 1–13 entregues).

## Objetivo
Executar a SPEC-50 **somente se existir base de origem acessível** (MySQL/H2 da versão original com dados a preservar). Caso contrário, registrar a migração como **não aplicável**, com evidência verificável, e arquivar a spec. Nunca versionar dados reais.

## Ponto de partida (conferido no repositório)
- A SPEC-50 é P3 e condicional (PRM-02); o discovery não achou evidência de dados de produção (dev usava `create-drop`; teste usava H2 semeado).
- Nenhum arquivo de origem no repositório: sem `*.mv.db`, `*.dump`, `*.bak` ou SQL de origem. Os únicos `.sql` são as migrations Flyway V1–V4 e o seed local `V1000__seed_local.sql` do backend novo.
- Não existe `tools/importacao/`; só `tools/parity/` (replay da paridade). A tag `v0-original` preserva o código antigo.
- O PostgreSQL novo já tem o esquema (V1–V4), o que seria o destino de uma importação.
- Nenhuma variável de conexão com a origem (URL/usuário) aparece em `.env.example` nem no `compose.yaml` (busca por origem/mysql/h2 sem ocorrências); nenhuma variável de ambiente da sessão com esses nomes.
- O `INDEX.md` cita a SPEC-50 só na linha da pasta `02-backend/` (não há tabela de status por spec); o ajuste da tarefa 2.2 é nessa linha.
- Docker está disponível (relevante só para o ramo B).

## Escopo
1. **Verificar a condição de início** de forma reproduzível: procurar base/dump/credenciais de origem no repositório, nos `.env.example`, no `compose.yaml` e nas variáveis de ambiente da sessão; registrar os comandos e resultados.
2. **Ramo A — sem base acessível (esperado):** registrar "não aplicável / cancelada sem necessidade" na SPEC-50, com motivo, evidência e data; atualizar `.specs/INDEX.md` e a seção "Resultado" da spec; arquivar a SPEC-50 com `git mv` em `.specs/archive/`.
3. **Ramo B — base acessível (só se a verificação achar uma):** executar T2–T5 da SPEC-50 (`tools/importacao/importar.mjs`, regras de `sexos` 1→`M`/2→`F` com rejeição dos demais, idempotência, relatório de contagens, teste com origem sintética). Antes de tocar qualquer base real, pedir aprovação (`aprovacao.md`), pois é ação com dados reais.
4. Em ambos os ramos, não gravar dados reais, credenciais nem dumps no repositório; usar o termo `origem`/`importacao`, nunca "legado/legacy".

## Fora do escopo (fica para outras etapas)
- Qualquer alteração de código Java, contrato OpenAPI ou migrations.
- Execução contra produção; criação de base de origem real; migração de usuários (não há cadastro de usuários).
- Frontend (a partir da etapa 15) e CI/segurança (SPEC-40/41).

## Critérios de aceitação
- Existe registro objetivo da verificação da condição de início (comandos e resultado), sem credenciais.
- Ramo A: SPEC-50 marcada como não aplicável com motivo, `INDEX.md` coerente e spec arquivada em `.specs/archive/` via `git mv`.
- Ramo B: *Given* origem sintética com 3 procedimentos e 2 autorizações · *When* importar duas vezes · *Then* 3 e 2 linhas no destino e relatório sem divergência; rejeições listadas; origem intocada.
- Nenhum dado real versionado; `./mvnw verify` do backend continua verde (sem mudança de código).
- Ramo B sem Docker: registrar bloqueio e não marcar a tarefa.

## Decisões tomadas sem consulta
- **Condição de início sem consulta humana:** a SPEC-50 T1 pede confirmar com o responsável; em modo automático, a ausência de qualquer origem no repositório e no ambiente é tratada como "sem base acessível" (ramo A). Se o responsável tiver uma base fora do repositório, a decisão deve ser revista. [REVISAR]
- **Ramo B condicionado a aprovação:** se a verificação inesperadamente achar uma base, não ler nem importar sem `aprovacao.md`, por envolver dados reais.
- **Arquivamento da SPEC-50 sem implementação:** tratada como cancelada sem necessidade, conforme `como-executar.md` §2 (cancelamento com motivo). [REVISAR]
- **Sem script placeholder:** não criar `tools/importacao/` vazio no ramo A, para não deixar código sem uso.
