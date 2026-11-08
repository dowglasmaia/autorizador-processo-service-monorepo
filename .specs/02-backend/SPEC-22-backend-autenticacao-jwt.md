# SPEC-22 — Autenticação JWT (e-mail e senha) no backend — nova feature

| Campo | Valor |
|---|---|
| Domínio / projeto | Backend · módulo `autenticacao` + `shared.security` |
| Prioridade | P1 (antes de qualquer endpoint de negócio) |
| Depende de | SPEC-04, SPEC-05, SPEC-06 |
| Sucessoras | SPEC-07, SPEC-23, SPEC-41 |
| Paralelismo | nenhum com SPEC-07 (todos os endpoints passam a exigir token) |

## 1. Objetivo
Introduzir a autenticação pedida pelo responsável (2026-11-08): **login por e-mail e senha, com JWT**. A versão original não tinha autenticação (EXEC); isto é **feature nova** (PEN-03 resolvida em parte: autenticação sim; perfis/papéis **não** — ver PEN-11). Todos os endpoints de `/api/v1/**` passam a exigir `Authorization: Bearer <jwt>`, exceto `POST /api/v1/auth/login`; `actuator/health*` continua público.

**Contrato (também em `API_CONTRACTS.md` §6):**
| operationId | Rota | Sucesso | Erros |
|---|---|---|---|
| `autenticar` | `POST /api/v1/auth/login` `{email, senha}` | `200 {accessToken, tokenType:"Bearer", expiraEm (segundos), usuario:{id, email}}` | `400 VALIDACAO_INVALIDA`, `401 CREDENCIAIS_INVALIDAS`, `429 MUITAS_TENTATIVAS` (+`Retry-After`) |
| `obterUsuarioAtual` | `GET /api/v1/auth/me` | `200 {id, email}` | `401 NAO_AUTENTICADO` / `SESSAO_EXPIRADA` |
Demais rotas: `401 NAO_AUTENTICADO` (ausente/inválido), `401 SESSAO_EXPIRADA` (expirado), ambos com `WWW-Authenticate: Bearer` e corpo `application/problem+json`; `403 ACESSO_NEGADO` reservado (sem papéis ainda).

**Decisões (ADR-16):** Spring Security (resource server, `spring-boot-starter-oauth2-resource-server`) com **JWT HS256**, segredo `JWT_SECRET` (≥ 32 bytes, **sem valor padrão**; a aplicação **não inicia** sem ele fora de testes); claims `iss=autorizador-processo`, `sub`=id do usuário, `email`, `iat`, `exp`; validade `JWT_TTL_MINUTOS` (padrão 60); **sem refresh token** na v1 (PEN-10): expirou → novo login. Senha com BCrypt (força ≥ 10, via `PasswordEncoder` do Spring Security). Sessão **stateless**, CSRF desabilitado (sem cookies de sessão), CORS da SPEC-06 com `Authorization` permitido.
**Usuários:** tabela `usuario` (`V3__usuario.sql`, Flyway). **Sem cadastro, recuperação de senha nem gestão de usuários na v1** (PEN-09). O primeiro usuário nasce por *bootstrap*: se `usuario` estiver vazia e `AUTH_BOOTSTRAP_EMAIL`/`AUTH_BOOTSTRAP_SENHA` existirem, cria-o (hash BCrypt); nunca grava senha em claro, nunca registra a senha em log.
**Proteções:** mensagem única `CREDENCIAIS_INVALIDAS` para e-mail inexistente, senha errada ou usuário inativo; tempo de resposta equalizado (comparar hash fictício quando o e-mail não existe); limitação de tentativas em memória: 5 falhas por (e-mail, IP) em 15 min → `429` (premissa PRM-09; distribuído = PEN-12); e-mail normalizado (trim + minúsculas) e único sem diferenciar caixa; logs sem senha/token e com e-mail mascarado.
**Restrições:** `autenticacao` segue Hexagonal Pragmática (portas `UsuarioRepository`, `EmissorToken`, `HashSenha`; domínio sem Spring); infraestrutura JWT em `shared.security` **sem importar features**; nenhum endpoint de negócio pode ficar aberto por esquecimento (teste que varre os mapeamentos).

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `API_CONTRACTS.md` §6, `SECURITY_REQUIREMENTS.md` (SEC-13..SEC-20), `DATABASE_DESIGN.md` (V3), `DECISIONS.md` (ADR-16, PEN-09..PEN-12); skills `java-spring-create-module` (decisão D8 de `shared/security`), `java-persistence-performance`, `openapi-rest-contract`

## 3. Tarefas
- [ ] **T1.** Contrato (atualização aprovada por `API_CONTRACTS.md` §6): `openapi/modules/auth.yaml` (`autenticar`, `obterUsuarioAtual`, esquemas `LoginRequest`, `TokenResponse`, `Usuario`) e `openapi/common/security.yaml` (`bearerAuth`, aplicado globalmente com `login` como exceção); erros `401/429` em `common/responses.yaml`. Regenerar e validar.
- [ ] **T2.** `V3__usuario.sql`: `usuario(id bigint generated always as identity PK, email varchar(254) not null, senha_hash varchar(100) not null, ativo boolean not null default true, criado_em timestamptz not null default now())` + `UNIQUE INDEX ux_usuario_email_lower ON usuario (lower(email))`; `CHECK (email = lower(email))`.
- [ ] **T3.** Módulo `autenticacao` (scaffold `rich`): `Usuario` (domínio), `AutenticarUsuarioUseCase` (valida credenciais, equaliza tempo, aplica limitador, emite token via porta), `ObterUsuarioAtualUseCase`; adaptador JPA de `UsuarioRepository`; `AuthController` implementando a interface gerada.
- [ ] **T4.** `shared.security`: `SecurityConfig` (`SecurityFilterChain` stateless; `permitAll` apenas `/api/v1/auth/login`, `/actuator/health/**`, documentação no perfil `local`; `authenticated` no resto), `JwtEncoder/JwtDecoder` HS256 com validação de `iss` e `exp`, `AuthenticationEntryPoint` e `AccessDeniedHandler` gerando `problem+json` com `code`, `PasswordEncoder` BCrypt, **falha de inicialização** se `JWT_SECRET` ausente/curto.
- [ ] **T5.** Bootstrap do primeiro usuário (`ApplicationRunner` na camada de infraestrutura do módulo; só cria quando a tabela está vazia e as duas variáveis existem).
- [ ] **T6.** Limitador de tentativas (porta + adaptador em memória com relógio injetável).
- [ ] **T7.** Ajustar a infraestrutura de testes do backend: utilitário que emite JWT válido para `@WebMvcTest`/`@SpringBootTest` (SPEC-07 em diante o usa); perfis de teste definem `JWT_SECRET` próprio.
- [ ] **T8.** Testes: domínio e casos de uso (credencial válida/ inválida/ inativo/ e-mail com caixa diferente/ limitador), integração PostgreSQL (unicidade case-insensitive, bootstrap idempotente), WebMvc/ponta a ponta (login `200`, `401` sem token, token expirado → `SESSAO_EXPIRADA`, token adulterado, `iss` errado, `429`), **teste que varre todos os mapeamentos e falha se algum endpoint (exceto os públicos) responder sem `401` quando sem token**, ausência de senha/token em logs e corpos de erro.
- [ ] **T9.** `ArchitectureTest` (ArchUnit) estendido ao módulo: `shared.security` não importa features; `autenticacao.domain` sem Spring/JPA/Jackson.
- [ ] **T10.** Documentar variáveis `JWT_SECRET`, `JWT_TTL_MINUTOS`, `AUTH_BOOTSTRAP_EMAIL`, `AUTH_BOOTSTRAP_SENHA` no `README.md` do backend (exemplos **sem valores reais**).

**Aceite (G/W/T):** *Given* usuário bootstrap `a@b.com` · *When* `POST /auth/login` com senha correta · *Then* `200` com JWT cujo `exp` = `iat` + TTL. *When* senha errada **ou** e-mail inexistente · *Then* `401 CREDENCIAIS_INVALIDAS` idêntico e com tempos equivalentes. *When* `GET /procedimentos` sem token · *Then* `401 NAO_AUTENTICADO`; com token → `200`. *When* token expirado · *Then* `401 SESSAO_EXPIRADA`. *When* 6ª tentativa falha em 15 min · *Then* `429`. *Given* `JWT_SECRET` ausente · *Then* a aplicação não inicia.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1 | `openapi-rest-contract` |
| T2, T8 (persistência) | `java-persistence-performance` (migração segura; índice funcional; testes em PostgreSQL real) |
| T3–T10 | `java-spring-create-module` (obrigatória), `java-modern-engineering`; ArchUnit |

## 5. Verificação obrigatória
- `validate.mjs` → sem falhas; `./mvnw verify` (JDK 25) → verde, incluindo o teste de varredura de endpoints protegidos e o `ArchitectureTest`
- Subir a aplicação e executar com `curl`: login, `me`, rota protegida sem token e com token → resultados do aceite
- `git grep -in "jwt_secret\s*[:=]\s*[A-Za-z0-9]" -- api-autorizador-processo` → nenhum segredo literal
- Definition of Done: todos os critérios do aceite verificados por testes executados; nenhum endpoint de negócio acessível sem token; nenhum segredo/senha em log ou repositório.

## 6. Resultado
_Preencher ao concluir._
