# Autenticação JWT no backend

Etapa 7 de 31 · Fase 2 · derivada da `SPEC-22` (`.specs/02-backend/SPEC-22-backend-autenticacao-jwt.md`). Proposta antecipada, escrita enquanto a etapa 6 era implementada; será reconferida contra o que a etapa 6 entregar.

## Objetivo
Introduzir login por e-mail e senha com JWT (HS256) no `api-autorizador-processo`: emitir e validar o token e proteger todas as rotas `/api/v1/**`, exceto `POST /api/v1/auth/login`; `actuator/health*` continua público (SEC-13..SEC-18, SEC-20). Feature nova (a versão original não tinha autenticação). Sem papéis/perfis, sem cadastro de usuários, sem recuperação de senha e sem refresh token.

## Ponto de partida (conferido antes da entrega da etapa 6)
- `api-autorizador-processo/` tem `Application`, `shared/logging/RequestLoggingFilter`, `shared/package-info.java`, migrações `V1__schema_base.sql` e `V2__auditoria.sql` (+ seed `V1000`), `ArchitectureTest`, `ModularityTests`, `IntegrationTestBase` (Testcontainers) e três contratos de módulo em `openapi/modules/` (`procedimento`, `autorizacao`, `painel`) mais `openapi/common/responses.yaml`.
- Não existem `openapi/modules/auth.yaml` nem `openapi/common/security.yaml`; não há Spring Security no `pom.xml`; não há controllers em `src/main`.
- A etapa 6 (em andamento) cria `GlobalExceptionHandler`, `BusinessRuleException` e `CorsConfig` (`WebMvcConfigurer`). Esta etapa deve integrar o CORS à cadeia do Spring Security e reaproveitar o formato `problem+json`; a forma exata será conferida na revisão.
- `.specs/06-execution-plan/` não existe (desvio a registrar na evidência).
- A migração nova será `V3__usuario.sql` (V1 e V2 já existem).

## Escopo
1. Contrato (aprovado pela SPEC-22 e `API_CONTRACTS.md` §6): `auth.yaml` com `autenticar` (`POST /api/v1/auth/login`) e `obterUsuarioAtual` (`GET /api/v1/auth/me`); `security.yaml` com `bearerAuth` global e `login` como exceção; respostas `401/429` em `responses.yaml`; nova execução do gerador para `auth`.
2. `V3__usuario.sql`: tabela `usuario` com índice único em `lower(email)` e `CHECK (email = lower(email))`.
3. Módulo `autenticacao` (Hexagonal Pragmática): domínio `Usuario`, casos de uso `AutenticarUsuarioUseCase` e `ObterUsuarioAtualUseCase`, portas `UsuarioRepository`, `EmissorToken`, `HashSenha` e `LimitadorTentativas`, adaptador JPA e `AuthController`.
4. `shared.security`: `SecurityConfig` stateless (CSRF desabilitado, CORS integrado), `JwtEncoder/JwtDecoder` HS256 validando `iss` e `exp`, `PasswordEncoder` BCrypt (força ≥ 10), `AuthenticationEntryPoint`/`AccessDeniedHandler` em `problem+json` (`NAO_AUTENTICADO`, `SESSAO_EXPIRADA`, `ACESSO_NEGADO`) com `WWW-Authenticate: Bearer`; falha de inicialização sem `JWT_SECRET` (≥ 32 bytes, sem padrão) fora de testes. Sem importar features.
5. Bootstrap do primeiro usuário por `AUTH_BOOTSTRAP_EMAIL`/`AUTH_BOOTSTRAP_SENHA` (só com tabela vazia; hash BCrypt; nunca registra a senha).
6. Proteções: mensagem única `CREDENCIAIS_INVALIDAS`, tempo equalizado (hash fictício), limitador em memória de 5 falhas por (e-mail, IP) em 15 min → `429` + `Retry-After`, e-mail normalizado, logs com e-mail mascarado e sem senha/token.
7. Testes (incluindo utilitário que emite JWT para as etapas seguintes e o teste que varre todos os mapeamentos), `ArchitectureTest` estendido e documentação das variáveis no `README.md` do backend e no `.env.example`.

## Fora do escopo (fica para outras etapas)
- Controllers e regras de `procedimento`, `autorizacao` e `painel`: etapas seguintes (SPEC-07 em diante); o teste de varredura passa a cobri-los quando existirem.
- Telas de login, guard e interceptor do frontend: SPEC-23.
- Papéis/perfis (PEN-11), cadastro/gestão de usuários (PEN-09), refresh token (PEN-10), limitador distribuído (PEN-12).
- Endurecimento e revisão final de segurança: SPEC-41. Dockerfile, CI e Compose: etapas próprias.

## Critérios de aceitação
- *Dado* usuário bootstrap `a@b.com`, *quando* `POST /auth/login` com senha correta, *então* `200` com JWT cujo `exp` = `iat` + TTL (`JWT_TTL_MINUTOS`, padrão 60).
- Senha errada, e-mail inexistente ou usuário inativo → `401 CREDENCIAIS_INVALIDAS` idêntico e com trabalho equivalente.
- Rota protegida sem token → `401 NAO_AUTENTICADO`; com token válido → sucesso; token expirado → `401 SESSAO_EXPIRADA`; token adulterado, `alg=none` ou `iss` errado → `401`.
- 6ª falha em 15 min para o mesmo (e-mail, IP) → `429` com `Retry-After`.
- Sem `JWT_SECRET` (ou curto) a aplicação não inicia.
- O teste de varredura falha se algum endpoint não público responder sem `401`; nenhuma senha/token em logs ou corpos de erro.
- `./mvnw verify` verde (com `ArchitectureTest` e `ModularityTests`; Testcontainers exige Docker, senão registrar bloqueio); `git grep -in "jwt_secret\s*[:=]\s*[A-Za-z0-9]" -- api-autorizador-processo` sem segredo literal; nenhum nome "legado/legacy".

## Riscos
- Boot 4 / Spring Security 7: nomes de starters e de testes de segurança (`spring-security-test`) podem diferir; conferir no `pom.xml` e adicionar só o necessário.
- `shared` é módulo Modulith aberto; `shared.security` não pode depender de features (o emissor de token é porta consumida por `autenticacao` e implementada em `shared.security`).
- Exceções lançadas em filtros não passam pelo `GlobalExceptionHandler` da etapa 6: os handlers do Security devem produzir `problem+json` no mesmo formato.
- Equalização de tempo e limitador são difíceis de testar de forma determinística: usar relógio injetável e verificar o trabalho feito (hash fictício), não milissegundos.
- Docker indisponível bloqueia os testes de persistência e do bootstrap; registrar sem marcar como concluídos.

## Decisões tomadas sem consulta
- **Aprovação do contrato:** `API_CONTRACTS.md` §6 e a SPEC-22 valem como aprovação; nenhum `code` além dos descritos nelas.
- **Dependências:** o Spring Security (resource server) é exigido pela spec e entra no `pom.xml` apenas com o necessário.
- **[REVISAR] Rota `/auth/me`:** mantida por constar na SPEC-22 (o frontend a usa), embora o plano cite só login, JWT e proteção.
- **[REVISAR] Documentação no perfil `local`:** a UI de documentação fica pública somente no perfil `local`, conforme SEC-07.
- **Reaproveitar a etapa 6:** CORS e formato de erro serão adaptados ao que a etapa 6 entregar (decisão final na revisão da proposta).
- **Bootstrap mantido:** é a única forma de existir usuário sem cadastro; as variáveis não têm valor padrão nem entram no repositório.
