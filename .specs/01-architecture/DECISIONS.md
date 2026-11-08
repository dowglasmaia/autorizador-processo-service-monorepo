# DECISIONS — decisões, premissas e pendências

Diferencia: **Fato** · **Inferência** · **Decisão** · **Recomendação** · **Desconhecido**. Mudanças de comportamento aprovadas: DIV-01..08 (`00-product-discovery/BASELINE_COMPORTAMENTO_ORIGINAL.md` §3). Instruções do responsável (2026-11-08): (a) specs em `.specs/` seguindo `shared/`; (b) nenhum nome "legado" no código do backend; (c) dados do cadastro do protótipo entram como **nova feature de backend**; (d) backend refatorado para **Java 25** refletindo o protótipo; (e) JDK em `C:\jdk-25.0.2` e `spring-boot-starter-parent` **4.1.2**; (f) tela de login com autenticação **JWT** (e-mail e senha); (g) **ArchUnit** valida a arquitetura; (h) as skills `java-persistence-performance` e `angular-tailwind-ui-specialist` valem para este projeto.

## 1. Decisões arquiteturais (ADR)
| ID | Decisão | Alternativas e motivo |
|---|---|---|
| ADR-01 | **Reconstrução em projeto novo**, não migração in-place. Backend gerado por `java-spring-scaffold` em `api-autorizador-processo/`; original preservado só no histórico git (tag `v0-original`), sem pasta nem nomes "legado". | In-place exigiria saltar Java 8→25, Boot 2.2→4.1, `javax`→`jakarta`, camadas→módulos e MySQL/H2→PostgreSQL de uma vez; o scaffold não escreve em diretório existente. Segurança de regressão vem do baseline executável (SPEC-02) |
| ADR-02 | Java **25.0.2 LTS** (`C:\jdk-25.0.2`) + Spring Boot **4.1.2** (`spring-boot-starter-parent`) + Maven 3.9.x + OpenAPI Generator 7.26.0 | Boot 4.1.2 definido pelo responsável (matriz da skill: 4.1.1); demais versões pela matriz, com compatibilidade validada em SPEC-03 |
| ADR-03 | Hexagonal Pragmática, pacote por funcionalidade (`procedimento`, `autorizacao`, `painel`), Modulith + ArchUnit | Proporcional: 2 agregados, 1 regra. Sem CQRS/eventos |
| ADR-04 | Contract-first OpenAPI 3.1, `/api/v1`, `ProblemDetail` + `code` | Skill `openapi-rest-contract`; UI decide por `code` |
| ADR-05 | PostgreSQL 16, Flyway, auditoria Envers com plano B por gatilhos | RN10 preservada; risco de compatibilidade verificado por spike (SPEC-04) |
| ADR-06 | CORS por allowlist; mesma origem via Nginx (a autenticação está em ADR-16) | Elimina CORS no Compose; `Authorization` permitido no perfil `local` |
| ADR-07 | Nginx serve Angular e faz proxy `/api/` | Elimina CORS em produção/compose; `compose.yaml` existente já separa `api` e `app` |
| ADR-08 | Frontend gera **somente tipos** do OpenAPI (`openapi-typescript`, devDependency); HTTP manual tipado | Evita DTO duplicado sem trazer gerador de clientes; justificativa registrada em SPEC-16 |
| ADR-09 | Tokens visuais = os **efetivos do protótipo** (Manrope, teal `#0fa88c`, coral `#e5604a`, fundo `#f4fafa`), mapeados para Tailwind; fontes/ícones empacotados (sem CDN em produção) | O Broadsheet puro (serifa) contradiz o protótipo; skills citam "Grafite Ciano" de outro produto |
| ADR-10 | NEW-01 é **dado de catálogo** e não afeta a decisão (PEN-02) | Não inventar regra de negócio |
| ADR-11 | Painel agregado no servidor; KPI "Aprovação" fica **por sessão no frontend** | Negativas não persistem (RN8, PEN-01) |
| ADR-12 | Faixa de idade do formulário é expandida no **cliente** em idades exatas | RN6 intacta; protótipo já faz isso |
| ADR-13 | Testes: JUnit 5 + Testcontainers + ArchUnit; frontend: executor de testes padrão do CLI 21 (verificar) + Playwright (e2e) | Sem H2; Playwright é dependência nova justificada em SPEC-30 |
| ADR-16 | **Autenticação JWT** por e-mail e senha: Spring Security resource server, HS256 com `JWT_SECRET` obrigatório (≥ 32 bytes, sem padrão), validade `JWT_TTL_MINUTOS` (60), **sem refresh token**, sem papéis; senha BCrypt; `usuario` em PostgreSQL; primeiro usuário por *bootstrap* via variáveis; limitador de 5 falhas/15 min; frontend guarda o token em `sessionStorage` | Pedido do responsável (2026-11-08). HS256 por simplicidade (um só emissor/validador); RS256 e refresh/cookie `HttpOnly` ficam como evolução (PEN-10). Cadastro e recuperação de senha fora de escopo (PEN-09) |
| ADR-17 | **ArchUnit obrigatório** valida a arquitetura (`ArchitectureTest`, regras em `TARGET_ARCHITECTURE.md` §2.1), complementado por `ApplicationModules.verify()` | Pedido do responsável; é também o mecanismo que impede nomes "legado/legacy" no backend |
| ADR-18 | `java-persistence-performance` e `angular-tailwind-ui-specialist` aplicam-se integralmente a este projeto, substituindo as referências ao produto de origem pelos equivalentes daqui; os arquivos das skills não são alterados | Pedido do responsável |
| ADR-15 | Dockerfile próprio por projeto; **CI por projeto com `paths`** (`backend-ci`, `frontend-ci`) + `integracao-ci` separado | Instrução do responsável (2026-11-08): mudança só no front não dispara CI do back e vice-versa. Compromisso: a combinação só é testada no fluxo de integração |
| ADR-14 | Seed de desenvolvimento só em `db/seed` (perfil `local`); jamais em `src/main/java` | Corrige D12 |

## 2. Premissas
| ID | Premissa | Impacto se falsa |
|---|---|---|
| PRM-01 | Versões vêm da matriz da skill (2026-10-10); não foram confirmadas na web nesta análise | SPEC-03 T1 reconfirma e ajusta `contexto-tecnico.md` |
| PRM-02 | Não há dados de produção a migrar | SPEC-50 passa de condicional a obrigatória |
| PRM-03 | Captura de tela com "Configurações" é variante antiga; vale o HTML | Nova spec de configurações |
| PRM-04 | Docker disponível para Testcontainers e Compose | Testes de integração sem alternativa (sem H2) |
| PRM-05 | Idade máxima técnica 150 | Ajustar CHECK/validação |
| PRM-06 | Listas de categorias e especialidades = opções do protótipo | Ajustar enums (migração aditiva) |
| PRM-07 | Pacote base `org.maia.autorizador`, `groupId org.maia` | Renomeação trivial antes do primeiro código |
| PRM-08 | Executor tem Node ≥ 20.19, Docker e o JDK 25.0.2 em `C:\jdk-25.0.2` | SPEC-03 T0 |
| PRM-09 | Limitador de tentativas de login: 5 falhas por (e-mail, IP) em 15 min, em memória (uma instância) | Ajustar números/armazenamento (PEN-12) |

## 3. Pendências (não bloqueiam; alternativa provisória adotada)
| ID | Pergunta ao produto | Impacto | Provisório |
|---|---|---|---|
| PEN-01 | Registrar negativas (e dados do paciente/data)? | painel "Aprovação" real, auditoria | só concedidas (RN8); KPI por sessão |
| PEN-02 | Cobertura, quantidade/período, prévia e justificativa devem influenciar a decisão? | regra de autorização | apenas dados de catálogo |
| PEN-03 | ~~Haverá login?~~ **Resolvida (2026-11-08):** login por e-mail e senha com JWT (SPEC-22/23). Perfis: PEN-11 | — | — |
| PEN-04 | Duplicidade deve ser `409`? | contrato | mantém `403` + `code` |
| PEN-05 | Editar/excluir procedimento? | nova feature | ausente |
| PEN-06 | Tela "Configurações"? | nova feature | ausente |
| PEN-07 | Listas oficiais de categoria/especialidade | enums | opções do protótipo |
| PEN-08 | Procedimento "Não coberto" pode receber autorização? | regra | sim (como no original, sem efeito) |
| PEN-09 | Como criar/gerir usuários, recuperar senha, política de senha? | operação | só *bootstrap* do primeiro usuário por variáveis |
| PEN-10 | Refresh token / cookie `HttpOnly` / duração da sessão? | UX e segurança | token único de 60 min; novo login ao expirar |
| PEN-11 | Haverá perfis/papéis (ex.: mantenedor × operador)? | autorização por rota | todos os usuários autenticados podem tudo |
| PEN-12 | Limitador de tentativas distribuído (várias instâncias)? | segurança | em memória, uma instância |

## 4. Riscos
| Risco | Mitigação |
|---|---|
| Envers × Hibernate do Boot 4.1.2 | Spike em SPEC-04; plano B gatilhos |
| JDK 25 ausente na máquina | Contêiner Maven Temurin 25 |
| Divergência contrato × frontend | SPEC-32 + CI |
| Perda de regra ao reescrever | Baseline executável (SPEC-02) + paridade (SPEC-31) |
| Abuso de credenciais / vazamento de JWT | SEC-13..SEC-20, `JWT_SECRET` obrigatório, limitador, revisão em SPEC-41; usar HTTPS na frente do Nginx |
