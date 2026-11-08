# PRODUCT_CONTEXT — Autorizador de Procedimentos Médicos

Fonte principal de contexto para qualquer agente. Evidência: **[EXEC]** executado · **[CÓDIGO]** lido · **[PROTÓTIPO]** lido no protótipo · **[INFERÊNCIA]** · **[DECISÃO]** · **[DESCONHECIDO]**. Detalhes em `BASELINE_COMPORTAMENTO_ORIGINAL.md` (comportamento observado) e `CURRENT_STATE_ANALYSIS.md`.

## 1. O que é
API REST (+ nova interface web) que mantém um **catálogo de procedimentos médicos**, cada um com as regras de elegibilidade (sexos e idades permitidos), e **autoriza ou nega** a realização de um procedimento para um paciente conferindo sexo e idade informados contra essas regras. [CÓDIGO][EXEC]

## 2. Problema que resolve
Dar uma resposta imediata e consistente à pergunta "este paciente (sexo + idade) pode realizar este procedimento?", mantendo um histórico das autorizações concedidas. [CÓDIGO; finalidade declarada no README]

## 3. Objetivo da modernização
1. Backend: Java 25, Spring Boot, PostgreSQL 16, arquitetura por módulos de negócio, contrato OpenAPI. **Preservar as regras de negócio** e **refletir o protótipo** (novos dados do cadastro e painel). [DECISÃO do responsável, 2026-11-08]
2. Frontend: Angular 21 + Tailwind, a partir do protótipo `.specs/design/prototype/`.
3. Monorepo `api-autorizador-processo` + `app-autorizador-processo`, executável por Docker Compose, Nginx servindo o Angular.

## 4. Usuários [INFERÊNCIA — o código não define perfis]
- **Mantenedor de regras:** cadastra/consulta procedimentos.
- **Operador de autorização:** solicita autorizações e consulta o histórico.
A versão original não tinha login nem perfis [EXEC]. **Decisão do produto (2026-11-08):** a nova versão terá tela de login com e-mail e senha e autenticação JWT (SPEC-22/23); perfis/papéis seguem fora de escopo (PEN-11).

## 5. Módulos funcionais
| Módulo | Existe no original | Protótipo | Destino |
|---|---|---|---|
| Procedimentos (cadastrar, listar) | sim [EXEC] | sim (lista + cadastro em 4 etapas) | modernizar + **estender** (NEW-01) |
| Autorizações (solicitar, listar) | sim [EXEC] | sim (solicitar + histórico) | modernizar |
| Painel (KPIs, cobertura por idade, sexos, últimas autorizações) | não | sim | **criar** (NEW-02) |
| Autenticação (login por e-mail/senha, JWT) | não | não (tela a desenhar) | **criar** (NEW-04) |
| Tratamento de erros | parcial (3 exceções; `500` em lacunas) | modais/toasts | padronizar (ProblemDetail) |
| Auditoria Envers (sem endpoint) | sim [CÓDIGO] | — | preservar (RN10) |
| Configurações (item de menu na captura de tela) | não | só na imagem, ausente no HTML | **fora de escopo** (PEN-06) |

## 6. Fluxos principais
- **Cadastrar procedimento:** informa dados → `numProcedimento` já existe? `403` (duplicado) : grava → lista atualizada. [EXEC B02/B04]
- **Solicitar autorização:** escolhe procedimento + idade + sexo → procedimento existe? (hoje `500`, novo `404`) → sexo ∈ sexos **E** idade ∈ idades? sim: grava `permitido=true` e informa "Autorizado"; não: `403` "Negado", **nada é gravado**. [EXEC B09–B12]
- **Histórico:** lista autorizações concedidas (várias por procedimento são possíveis). [EXEC B15]
- **Painel:** totais, idades cobertas, distribuição por sexo, últimas 4 autorizações. [PROTÓTIPO]

## 7. Regras de negócio (invariantes — não alterar sem aprovação)
| # | Regra | Evidência |
|---|---|---|
| RN1 | `numProcedimento` obrigatório | [EXEC B05] |
| RN2 | `numProcedimento` único; duplicidade → `403` | [EXEC B04] |
| RN3 | `numProcedimento` imutável | [CÓDIGO `updatable=false`] |
| RN4 | Procedimento define conjunto de sexos (M/F) e de idades **exatas** | [CÓDIGO] |
| RN5 | Autorizar exige sexo **E** idade aderentes | [EXEC B11] |
| RN6 | Idade por igualdade exata; **sem faixas** na decisão | [CÓDIGO] |
| RN7 | Sexo da solicitação comparado a `M`/`F` sem diferenciar caixa; outro valor nunca casa | [EXEC B10] |
| RN8 | Só autorizações concedidas são gravadas; negativas não deixam registro | [CÓDIGO] |
| RN9 | Procedimento sem sexos/idades nunca autoriza; cadastro assim é permitido | [CÓDIGO] |
| RN10 | Alterações de procedimento e autorização são auditadas (Envers) | [CÓDIGO] |
| RN11 | Várias autorizações para o mesmo procedimento são permitidas | [EXEC B15] |

Códigos de sexo: `M` (Masculino, cód. 1), `F` (Feminino, cód. 2).

## 7.1 Dados de exemplo (perfil de desenvolvimento)
`1234` F [10,20] · `4567` M,F [20,30] · `6789` M,F [10]. O protótipo semeia as mesmas três linhas.

## 8. Integrações e dependências
Nenhuma integração externa, fila, agendamento ou serviço de terceiros [CÓDIGO]. Dependências: banco (H2 teste / MySQL dev na versão original; **PostgreSQL 16** no destino), Swagger/springfox (substituído por OpenAPI contract-first).

## 9. Incompleto / inexistente / a modernizar
- Incompleto: cobertura de testes (2 testes, perfil `tests` inexistente) [CÓDIGO]; sem CORS, sem health, sem paginação, erros inconsistentes com stack trace [EXEC].
- Inexistente no backend e presente no protótipo: dados estendidos do procedimento, painel agregado. → **nova feature** (SPEC-12, SPEC-09).
- Inexistente nos dois: login/perfis, edição/exclusão de procedimento, registro de negativas, dados do paciente, data/hora da autorização.
- Modernizar: Java 8→25, Boot 2.2.6→atual, `javax`→`jakarta`, springfox→OpenAPI, MySQL/H2→PostgreSQL 16, layered→módulos, WAR→JAR/contêiner.

## 10. Pendências de produto (não bloqueiam as specs; ver `01-architecture/DECISIONS.md`)
PEN-01 negativas devem ser registradas? · PEN-02 `cobertura`, `quantidadeMaxima/periodo`, `exigeAutorizacaoPrevia`, `exigeJustificativa` devem influenciar a decisão? · PEN-09 gestão de usuários e recuperação de senha · PEN-10 refresh token · PEN-11 perfis/papéis · PEN-12 limitador distribuído · PEN-04 `403` na duplicidade vs `409` · PEN-05 editar/excluir procedimento · PEN-06 tela Configurações · PEN-07 listas de categorias/especialidades (valores do protótipo adotados provisoriamente).
