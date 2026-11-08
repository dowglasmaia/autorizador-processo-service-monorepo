# Módulo painel

Etapa 11 de 31 · Fase 2 · derivada da `SPEC-09` (`.specs/02-backend/SPEC-09-modulo-painel.md`). Proposta escrita antecipadamente e **revisada contra o código da etapa 10 já entregue** (`autorizacao`).

## Objetivo
Criar `GET /api/v1/painel/resumo` (NEW-02): totais, idades cobertas, cobertura por idade, distribuição por sexo e as 4 últimas autorizações, calculados **no servidor** por SQL agregado. Somente leitura; nada novo é persistido. O KPI "Aprovação" não é calculado (negativas não são gravadas, PEN-01) e não se inventam métricas além das do protótipo.

## Ponto de partida (conferido no código atual, após a etapa 10)
- Não existe o pacote `painel` em `src/main/java` (existem `autenticacao`, `procedimento`, `autorizacao` e `shared`).
- Contrato `openapi/modules/painel.yaml` já pronto: `obterResumoPainel` (200/500), `ResumoPainel`, `CoberturaIdade`, `DistribuicaoSexos`; `ultimasAutorizacoes` (máx. 4) referencia `autorizacao.yaml#Autorizacao`. **Nenhuma mudança de contrato.**
- Banco (V1): `procedimento`, `procedimento_sexo(procedimento_id, sexo)`, `procedimento_idade(procedimento_id, idade)` e `autorizacao(id, procedimento_id, permitido, criada_em nullable)` com índice em `procedimento_id`. Não há índice em `criada_em`; a V4 só tocou o catálogo. Seed local `V1000__seed_local.sql` (3 procedimentos).
- O `ArchitectureTest` já cita `.painel..` na lista de módulos (linha ~124); falta a regra específica do módulo. Não há `JdbcClient` em uso ainda; `spring-boot-starter-data-jpa` está no `pom.xml`.
- A etapa 10 entregou em `autorizacao`: `ListarAutorizacoesUseCase.executar(pagina, tamanho)` (público, `@Transactional(readOnly)`, ordem mais recente primeiro com desempate por id, procedimentos em lote via `ObterProcedimentoUseCase.porIds`) e o `AutorizacaoController`, cujo mapeamento `Resultado` → DTO gerado é `private static` (não reutilizável). O mesmo padrão de dependência entre módulos (`autorizacao` usa o caso de uso de `procedimento`) já passa no `ArchitectureTest`/`ModularityTests`.
- `check-architecture.mjs` e `.specs/06-execution-plan/` não existem (desvios; vale o `ArchitectureTest`).

## Escopo
1. Scaffold do módulo `painel` (skill `java-spring-create-module`, estilo de leitura, sem entidade própria): `ObterResumoPainelUseCase`, porta `PainelConsulta` e adaptador de infraestrutura.
2. Consultas agregadas (skill `java-persistence-performance`): totais; `idade, count(*)` agrupado e ordenado; `sexo, count(distinct procedimento_id)`; últimas 4 autorizações com seus procedimentos, em número constante de consultas.
3. `PainelController` sobre a interface gerada; transação `readOnly`.
4. Testes: PostgreSQL (Testcontainers) com dados conhecidos, banco vazio, `@WebMvcTest`, JWT exigido (`VarreduraSegurancaTest`) e regra de `ArchitectureTest` para `painel`.
5. Medição de tempo e de SQL com 1.000 procedimentos / 10.000 autorizações, registrada como referência para a SPEC-42.

## Fora do escopo (fica para outras etapas)
- KPI "Aprovação", séries temporais e qualquer métrica fora do protótipo.
- Tela do painel (SPEC-20), configuração/observabilidade (SPEC-11), paridade final (SPEC-31), performance formal (SPEC-42).
- Novas migrações, cache e alteração dos módulos `procedimento`/`autorizacao`.

## Critérios de aceitação
- *Dado* o seed local e nenhuma autorização, *quando* `GET /api/v1/painel/resumo`, *então* `totalProcedimentos=3`, `totalAutorizacoes=0`, `idadesCobertas=3`, `coberturaPorIdade=[{10,2},{20,2},{30,1}]`, `distribuicaoSexos={masculino:2,feminino:3}`, `ultimasAutorizacoes=[]`.
- Banco vazio: zeros e listas vazias; `ultimasAutorizacoes` tem no máximo 4 itens, mais recentes primeiro.
- Procedimento com M e F conta nos dois sexos; `idadesCobertas` = idades distintas.
- Sem token → `401`. Consulta `readOnly`, sem escrita.
- `./mvnw verify` verde (incluindo `ArchitectureTest`, `ModularityTests` e regressão das etapas anteriores); sem "legado/legacy"; contagem de SQL e tempo do cenário grande registrados. Docker é necessário para Testcontainers; senão, registrar bloqueio.

## Riscos
- `painel` não pode depender da infraestrutura de `procedimento`/`autorizacao`; consultas SQL próprias sobre as tabelas ou portas públicas, validado por ArchUnit/Modulith.
- Ordenar "mais recentes" com `criada_em` nulo e sem índice: definir ordenação estável (`criada_em desc nulls last, id desc`) e medir com 10.000 linhas.
- Montar o `Procedimento` completo das últimas 4 sem N+1.
- O mapeamento para o DTO gerado de `Autorizacao` fica privado no controller de `autorizacao`; `painel` precisa de um mapeamento próprio (duplicação pequena, aceita) sem alterar `autorizacao`.
- Docker indisponível bloqueia os testes de persistência.

## Decisões tomadas sem consulta
- **Contrato aprovado:** a SPEC-09 e `API_CONTRACTS.md` valem como aprovação; `painel.yaml` não muda.
- **Sem migração e sem índice novo:** só se acrescenta índice em `criada_em` se a medição do T5 mostrar necessidade; nesse caso, **[REVISAR]** a migração aditiva antes de aplicar.
- **SQL agregado via `JdbcClient`** na infraestrutura de `painel` (somente leitura, sem entidade própria), em vez de JPQL, por ser a forma mais direta de agregar sem carregar entidades.
- **Últimas autorizações:** `painel` chama `ListarAutorizacoesUseCase.executar(0, 4)` (API pública da etapa 10; ordem e lote de procedimentos já resolvidos, sem N+1) e monta o DTO gerado num mapeador próprio no `api` de `painel`. Custo: uma consulta de contagem extra feita pela paginação, aceita; se a medição do T5.2 mostrar problema, **[REVISAR]** uma consulta própria. Alternativa descartada: SQL próprio duplicando a montagem do procedimento.
- **Ordenação:** a definida pela etapa 10 (mais recente primeiro, desempate por id decrescente), limite 4; os testes confirmam o tratamento de `criada_em` nulo.
- **Contagens como `int`:** o contrato usa `int32`; valores acima do limite estão fora do escopo.
- **Verificação de arquitetura:** `check-architecture.mjs` não existe; vale o `ArchitectureTest`. Desvio: `.specs/06-execution-plan/` não existe.
