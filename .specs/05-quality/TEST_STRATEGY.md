# TEST_STRATEGY

Princípio: **teste incremental por spec**; nenhuma spec fecha sem executar suas verificações (`shared/como-executar.md` §3–4). Distinguir sempre *teste executado*, *teste só escrito* e *não executado*.

## 1. Pirâmide por projeto
| Camada | Backend (`api-autorizador-processo`) | Frontend (`app-autorizador-processo`) |
|---|---|---|
| Unidade | Domínio puro JUnit5+AssertJ (`Procedimento.avaliar`, validações, `Sexo`); casos de uso com fakes/Mockito e `Clock` fixo | Funções puras (expansão de faixa de idade, mapeamento de erro), serviços de dados com `HttpTestingController`, componentes de apresentação |
| Integração | `@DataJpaTest`/`@SpringBootTest` com **PostgreSQL 16 via Testcontainers** (Flyway real); `@WebMvcTest` com `GlobalExceptionHandler` | Páginas com serviços simulados: carregando/vazio/erro/sucesso |
| Contrato | Aplicação sobe e responde conforme o OpenAPI (validação de resposta); interfaces geradas compilam | Tipos gerados = contrato (SPEC-32) |
| Arquitetura | **ArchUnit obrigatório** (`ArchitectureTest`, 10 regras de `TARGET_ARCHITECTURE.md` §2.1) + `ApplicationModules.verify()` | Limites de importação entre `features` (lint/ts paths) |
| Funcional / e2e | Paridade com baseline (SPEC-31) | Playwright sobre `docker compose` (SPEC-30): cadastrar → solicitar (autorizado/negado/inexistente) → histórico → painel |
| Regressão | Suíte completa a cada spec + `.specs/workflows/ferramentas/parity` | `ng build` produção + testes + e2e em CI |
| Não funcional | Smoke de desempenho (SPEC-42), health, logs sem vazamento | Lighthouse a11y/performance smoke, responsividade 320/768/1280 |

## 2. Cenários de aceitação de negócio (Given/When/Then) — fonte única
1. **RN5/RN6:** *Given* procedimento 4567 aceita M,F e idades [20,30] · *When* solicita idade 20 sexo F · *Then* `201` e uma autorização persistida. Idade 25 → `403 AUTORIZACAO_NEGADA` e nada persistido.
2. **RN7:** sexo `m` autoriza como `M`; `Masculino` → `400`.
3. **RN2:** cadastrar 9999 duas vezes → segundo `403 PROCEDIMENTO_DUPLICADO`; duas requisições concorrentes → exatamente um `201`.
4. **RN9:** procedimento com `sexos=[]` nunca autoriza.
5. **RN11:** duas solicitações autorizadas para 4567 → duas linhas no histórico.
6. **DIV-01:** procedimento inexistente → `404`, nunca `500`.
7. **DIV-02:** `sexos:["X"]` → `400` e `GET /procedimentos` segue `200`.
8. **RN10:** cadastrar procedimento e autorização cria linhas `*_audit`.
9. **NEW-01:** cadastro com nome, categoria etc. retorna-os; sem `nome` → `400`; `quantidadeMaxima` sem `periodo` → `400`.
10. **NEW-02:** painel com seed (3 procedimentos) → `totalProcedimentos=3`, `idadesCobertas=3` (10,20,30), `coberturaPorIdade` 10→2, 20→2, 30→1, `distribuicaoSexos` M=2, F=3.
11. **NEW-04 (auth):** login válido → JWT; senha errada e e-mail inexistente → mesmo `401 CREDENCIAIS_INVALIDAS`; rota sem token → `401 NAO_AUTENTICADO`; token expirado → `401 SESSAO_EXPIRADA`; 6ª falha em 15 min → `429`; **nenhum endpoint de negócio responde sem token** (teste que varre os mapeamentos).
12. **Frontend auth:** guard redireciona ao login preservando `retorno` seguro; `401` limpa a sessão; logout limpa contadores da sessão.

## 3. Dados de teste
Backend: fixtures criadas por teste (builders); seed local só para verificação manual e e2e. Frontend: respostas simuladas tipadas a partir do contrato.

## 4. Critérios de qualidade
Compilação sem warnings novos; cobertura de domínio e casos de uso alta (meta ≥ 90 % de linhas nesses pacotes; sem exigir percentual global); zero `any`/`@ts-ignore` sem justificativa; nenhuma regressão de baseline; `ArchitectureTest` com 0 violações.

## 5. Ambiente
JDK 25 (instalado ou `maven:3.9-eclipse-temurin-25`), Docker (Testcontainers/Compose), Node ≥ 20.19. CI em SPEC-40 roda tudo.
