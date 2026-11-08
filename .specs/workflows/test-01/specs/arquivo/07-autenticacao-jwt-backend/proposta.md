# Autenticação JWT no backend

Etapa 7 de 31 · Fase 2 · derivada da `SPEC-22` (`.specs/02-backend/SPEC-22-backend-autenticacao-jwt.md`). Proposta reconferida contra o código entregue pela etapa 6.

## Objetivo
Introduzir login por e-mail e senha com JWT (HS256) no `api-autorizador-processo`: emitir e validar o token e proteger todas as rotas `/api/v1/**`, exceto `POST /api/v1/auth/login`; `actuator/health*` continua público (SEC-13..SEC-18, SEC-20). Feature nova (a versão original não tinha autenticação). Sem papéis/perfis, sem cadastro de usuários, sem recuperação de senha e sem refresh token.

## Ponto de partida (conferido após a entrega da etapa 6)
- `api-autorizador-processo/` tem `Application`, `shared/logging/RequestLoggingFilter`, `shared/package-info.java`, migrações `V1__schema_base.sql` e `V2__auditoria.sql` (+ seed `V1000`), `ArchitectureTest`, `ModularityTests`, `IntegrationTestBase` (Testcontainers) e três contratos de módulo em `openapi/modules/` (`procedimento`, `autorizacao`, `painel`) mais `openapi/common/responses.yaml`.
- Não existem `openapi/modules/auth.yaml` nem `openapi/common/security.yaml`; não há Spring Security no `pom.xml`; não há controllers em `src/main`.
- A etapa 6 entregou `shared.web.GlobalExceptionHandler` (`@ControllerAdvice`, `ProblemDetail` com `code`), `shared.error.BusinessRuleException` e `shared.web.CorsConfig` (`WebMvcConfigurer`, propriedade `app.cors.allowed-origins`, métodos `GET/POST/OPTIONS`, cabeçalhos `Authorization` e `Content-Type`). `responses.yaml` já tem o esquema `ProblemDetail` e as respostas `400/403/404/500`; faltam `401` e `429`. Esta etapa integra o CORS à cadeia do Spring Security (`.cors(...)` reaproveitando a configuração MVC, sem duplicar a allowlist) e os handlers de filtro replicam o formato `problem+json` do handler global.
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
- **Reaproveitar a etapa 6:** a allowlist de CORS continua em `CorsConfig`; o Security apenas ativa CORS para que o pré-voo `OPTIONS` passe sem token. O `403 ACESSO_NEGADO` já existe em `responses.yaml` e é reutilizado.
- **Bootstrap mantido:** é a única forma de existir usuário sem cadastro; as variáveis não têm valor padrão nem entram no repositório.
- **Lote 1 — [REVISAR] ambiente:** `./mvnw verify` não resolveu `spring-boot-starter-oauth2-resource-server:4.1.2` e `spring-boot-starter-security-test:4.1.2` (PKIX ao acessar o Central; só a 4.1.1 está no cache local). Não foi contornado. `SecurityConfig` e `JwtConfig` (em `shared.security`) ainda não foram compilados nem testados; refazer `./mvnw verify` com rede/certificado funcionando.
- **Lote 1 — contrato:** `security.yaml` guarda o esquema `bearerAuth`; `auth.yaml` o referencia, aplica `security` global e `security: []` em `autenticar`. Códigos `CREDENCIAIS_INVALIDAS`, `NAO_AUTENTICADO`, `SESSAO_EXPIRADA`, `ACESSO_NEGADO` e `MUITAS_TENTATIVAS` entraram no enum de `ProblemDetail`.
- **Lote 1 — JWT:** propriedades `app.jwt.secret` (`JWT_SECRET`, sem padrão), `app.jwt.issuer` e `app.jwt.ttl-minutos`; o `JWT_SECRET` de testes fica em `application-test.yml`. Documentação (springdoc) ainda não existe; os caminhos `/swagger-ui/**` e `/v3/api-docs/**` só ficam públicos no perfil `local`.
- `.specs/06-execution-plan/` não existe (desvio registrado).
- **Lote 2 — [REVISAR] ambiente:** `./mvnw test-compile` segue falhando por PKIX ao baixar `spring-boot-starter-oauth2-resource-server:4.1.2` e `spring-boot-starter-security-test:4.1.2`; nada do lote 2 foi compilado nem testado. Não contornado. Rever erros de compilação assim que a rede/certificado voltar.
- **Lote 2 — emissor de token:** como `shared` não pode importar features, `shared.security.EmissorJwt` é genérico (sub, e-mail) e a porta `EmissorToken` é implementada por `autenticacao.infrastructure.security.EmissorTokenAdapter`; o mesmo vale para `HashSenhaAdapter` sobre `PasswordEncoder`.
- **Lote 2 — erros 401/429:** `BusinessRuleException.Categoria` ganhou `NAO_AUTENTICADO` (401, com `WWW-Authenticate: Bearer`) e `LIMITE_EXCEDIDO` (429, via `LimiteExcedidoException` com `Retry-After`); o `GlobalExceptionHandler` os traduz, sem `shared` importar a feature. `ObterUsuarioAtualUseCase` devolve `NAO_AUTENTICADO` se o usuário sumiu ou está inativo.
- **Lote 2 — DTOs dos casos de uso:** o controller não pode depender de `domain.model` (ArchUnit); por isso cada caso de uso expõe `Comando`/`Resultado` aninhados. O controller usa o prefixo `@RequestMapping("/api/v1")` porque o gerador não inclui o `servers.url` nos caminhos.
- **Lote 2 — logs mascarados:** o e-mail é mascarado (`MascaraEmail`, em `shared.logging`) nos logs dos casos de uso e do bootstrap, não no controller, que não registra nada. Limitador: chave `email|IP` (`remoteAddr`, sem `X-Forwarded-For`); bloqueia a 6ª tentativa e `Retry-After` = tempo até a falha mais antiga sair da janela.
- **Lote 2 — risco para o lote 3:** `@WebMvcTest` do `GlobalExceptionHandlerTest` usa o perfil `local`, sem `JWT_SECRET`; se `JwtConfig`/`SecurityConfig` entrarem no slice, ajustar o teste (ex.: `@ActiveProfiles` com segredo de teste).
- **Lote 3 — [REVISAR] ambiente:** `./mvnw test-compile` (online e offline) segue falhando por PKIX ao baixar `spring-boot-starter-oauth2-resource-server:4.1.2` e `spring-boot-starter-security-test:4.1.2`. Nenhum teste dos lotes 1–3 foi compilado ou executado; não foi contornado. A spec **não foi arquivada** (7.2/7.3 pendentes): refazer `./mvnw verify` com rede/certificado corrigidos, ajustar eventuais erros de compilação (imports de Boot 4 como `AutoConfigureMockMvc` em `org.springframework.boot.webmvc.test.autoconfigure`) e só então arquivar com `git mv`.
- **Lote 3 — testes existentes ajustados:** `GlobalExceptionHandlerTest` ganhou `@AutoConfigureMockMvc(addFilters = false)` (o slice `@WebMvcTest` passaria a aplicar o Security) e `SeedLocalTest` (perfil `local`) recebeu `app.jwt.secret` de teste via `@TestPropertySource`, pois sem segredo a aplicação não inicia.
- **Lote 3 — escolhas dos testes:** a API é testada com `MockMvc` sobre o contexto completo e PostgreSQL real; o limitador em memória é isolado usando e-mail e IP únicos por teste. O teste de varredura exclui apenas `POST /api/v1/auth/login` e usa os mapeamentos MVC (health do Actuator é público e fica fora do `RequestMappingHandlerMapping`).
- **Lote 3 — documentação:** `JWT_SECRET`, `JWT_TTL_MINUTOS`, `AUTH_BOOTSTRAP_EMAIL` e `AUTH_BOOTSTRAP_SENHA` no `README.md` do backend e no `.env.example`, sem valores. O `compose.yaml` não foi alterado (fica para a etapa de Compose; a API em contêiner precisará receber `JWT_SECRET`).
- **Evidência resumida:** 1.1–5.5 conforme lotes 1–2; 6.2–6.6 e 7.1 por arquivos de teste e documentação listados em `tarefas.md`; `git grep -in "jwt_secret\s*[:=]\s*[A-Za-z0-9]"` sem ocorrências.
- **Lote 5 — [REVISAR] ambiente:** `./mvnw -B verify` reexecutado (2026-11-08) com o mesmo erro PKIX ao baixar `spring-boot-starter-oauth2-resource-server:4.1.2` e `spring-boot-starter-security-test:4.1.2`; `git grep` de segredo literal sem ocorrências. Não contornado (sem desativar validação TLS nem trocar de versão). 7.2 continua pendente e 7.3 não foi executada: arquivar uma spec cujo código de segurança nunca foi compilado ou testado esconderia o risco. Quando o certificado do Central for corrigido: rodar `./mvnw verify`, corrigir erros de compilação/testes e então fazer `git mv` da spec para `.specs/archive/`.


## Decisão humana — APROVADO (2026-11-08T15:30:14.933Z, por Dowglas Maia)

Pode executar a ação que estava aguardando aprovação.

- **Recuperação — [REVISAR] ambiente resolvido:** o erro PKIX era do truststore do JDK. Usar o repositório do Windows (`MAVEN_OPTS="-Djavax.net.ssl.trustStoreType=Windows-ROOT"`) resolveu sem desligar a validação TLS. Com isso `./mvnw -B clean verify` passou (78 testes) após ajustar testes defasados (V3, EntityScan, qualifier, regra ArchUnit para `generated`). Sem essa opção, a máquina do dev continua com PKIX no Central.
