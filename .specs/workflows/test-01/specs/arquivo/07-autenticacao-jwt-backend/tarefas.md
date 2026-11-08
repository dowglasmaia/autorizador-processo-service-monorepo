# Tarefas — Autenticação JWT no backend

## 1. Preparação
- [x] 1.1 Reconferir o que a etapa 6 entregou (`GlobalExceptionHandler`, `CorsConfig`, `ProblemDetail`, `responses.yaml`) e o `pom.xml`; ler `API_CONTRACTS.md` §6 e SEC-13..SEC-20; registrar a ausência de `.specs/06-execution-plan/`.
- [x] 1.2 Adicionar ao `pom.xml` os starters do Spring Security (resource server) e de testes de segurança, só o que faltar. _Feito: `spring-boot-starter-oauth2-resource-server` e `spring-boot-starter-security-test` (test). Resolução do Maven bloqueada no ambiente (PKIX ao baixar 4.1.2 do Central); compilação do código de segurança ainda não verificada._

## 2. Contrato
- [x] 2.1 Criar `openapi/modules/auth.yaml` (`autenticar`, `obterUsuarioAtual`, `LoginRequest`, `TokenResponse`, `Usuario`).
- [x] 2.2 Criar `openapi/common/security.yaml` (`bearerAuth` global, `login` como exceção) e as respostas `401/429` em `responses.yaml`.
- [x] 2.3 Configurar a execução do gerador para `auth`, regenerar e validar o contrato. _Geração OK (`AuthApi`, `LoginRequest`, `TokenResponse`, `Usuario`) antes de entrarem as novas dependências._

## 3. Banco
- [x] 3.1 Criar `V3__usuario.sql` com a tabela `usuario`, `ux_usuario_email_lower` e `CHECK (email = lower(email))`.

## 4. Infraestrutura de segurança
- [x] 4.1 Criar `shared.security.SecurityConfig` stateless (CSRF off, CORS integrado, `permitAll` só em login, health e documentação do perfil `local`).
- [x] 4.2 Configurar `JwtEncoder/JwtDecoder` HS256 com validação de `iss` e `exp` e falha de inicialização se `JWT_SECRET` faltar ou tiver menos de 32 bytes.
- [x] 4.3 Criar `AuthenticationEntryPoint` e `AccessDeniedHandler` em `problem+json` (`NAO_AUTENTICADO`, `SESSAO_EXPIRADA`, `ACESSO_NEGADO`) com `WWW-Authenticate: Bearer`. _Feito: `ProblemAuthenticationEntryPoint` (`SESSAO_EXPIRADA` se o token expirou, senão `NAO_AUTENTICADO`) e `ProblemAccessDeniedHandler`, ligados na cadeia; não compilado (ver proposta)._
- [x] 4.4 Expor `PasswordEncoder` BCrypt (força ≥ 10). _Feito: `PasswordEncoderConfig` (BCrypt força 12)._

## 5. Módulo autenticacao
- [x] 5.1 Criar o domínio `Usuario` (e-mail normalizado) e as portas `UsuarioRepository`, `EmissorToken`, `HashSenha` e `LimitadorTentativas`. _Feito: `Usuario`, `TokenEmitido` e as quatro portas em `autenticacao`._
- [x] 5.2 Implementar `AutenticarUsuarioUseCase` (mensagem única, hash fictício, limitador, emissão do token) e `ObterUsuarioAtualUseCase`. _Feito: os dois casos de uso, com `Comando`/`Resultado` próprios._
- [x] 5.3 Implementar o adaptador JPA do repositório e o limitador em memória com relógio injetável. _Feito: `UsuarioRepositoryAdapter` (JPA) e `LimitadorTentativasAdapter` (5 falhas/15 min, `Clock`)._
- [x] 5.4 Implementar `AuthController` sobre a interface gerada, com e-mail mascarado nos logs e sem senha/token. _Feito: `AuthController` em `/api/v1`; logs com e-mail mascarado nos casos de uso._
- [x] 5.5 Implementar o bootstrap do primeiro usuário (`ApplicationRunner`, só com tabela vazia e as duas variáveis presentes). _Feito: `UsuarioInicialRunner` + propriedades `app.auth.bootstrap.*`._

## 6. Testes
- [x] 6.1 Criar o utilitário de testes que emite JWT válido e definir `JWT_SECRET` próprio nos perfis de teste. _Feito: `JwtTestSupport` (token válido, expirado, emissor errado, outro segredo); `JWT_SECRET` de testes já em `application-test.yml`._
- [x] 6.2 Testar domínio e casos de uso (credencial válida/inválida, usuário inativo, caixa do e-mail, limitador). _Feito: `UsuarioTest`, `AutenticarUsuarioUseCaseTest`, `ObterUsuarioAtualUseCaseTest`, `LimitadorTentativasAdapterTest` (relógio mutável). Não executados (ver bloqueio em 7.2)._
- [x] 6.3 Testar a persistência em PostgreSQL (unicidade sem caixa, bootstrap idempotente). _Feito: `UsuarioPersistenciaTest` e `UsuarioInicialRunnerTest` (Testcontainers). Não executados (ver 7.2)._
- [x] 6.4 Testar a API (login `200`, `401` sem token, expirado, adulterado, `alg=none`, `iss` errado, `429` com `Retry-After`). _Feito: `AuthApiTest`. Não executado (ver 7.2)._
- [x] 6.5 Criar o teste que varre os mapeamentos e falha se endpoint não público responder sem `401`; testar ausência de senha/token em logs e erros. _Feito: `VarreduraSegurancaTest`. Não executado (ver 7.2)._
- [x] 6.6 Estender o `ArchitectureTest` (`shared.security` sem features; `autenticacao.domain` sem Spring/JPA/Jackson). _Feito: `autenticacao` entrou em `shared_security_isolado` e nova regra `autenticacao_domain_sem_framework`. Não executado (ver 7.2)._

## 7. Documentação e fechamento
- [x] 7.1 Documentar `JWT_SECRET`, `JWT_TTL_MINUTOS`, `AUTH_BOOTSTRAP_EMAIL` e `AUTH_BOOTSTRAP_SENHA` no `README.md` do backend e no `.env.example`, sem valores reais.
- [x] 7.2 Rodar `./mvnw verify` e o `git grep` de segredo literal; corrigir falhas e registrar bloqueio se o Docker faltar. _Feito (2026-11-08): o PKIX vinha do truststore do JDK; com `MAVEN_OPTS="-Djavax.net.ssl.trustStoreType=Windows-ROOT"` as dependências 4.1.2 resolveram. `./mvnw -B clean verify`: 78 testes, 0 falhas, BUILD SUCCESS (Docker/Testcontainers ok). `git grep` de segredo literal e de "legado/legacy": sem ocorrências. Correções nos testes: `MigracaoTest`/`SeedLocalTest` (V3), `AuditoriaTest` (@EntityScan incluindo `autenticacao`), `VarreduraSegurancaTest` (@Qualifier), `ArchitectureTest` (ignora pacote `generated`) e `AutenticarUsuarioUseCaseTest` (falha contada por par e-mail/IP)._
- [x] 7.3 Registrar a evidência por tarefa, o resumo e arquivar a spec com `git mv` para `.specs/archive/`. _Feito: `SPEC-22-backend-autenticacao-jwt.md` movida para `.specs/archive/`; evidência nas tarefas acima._
