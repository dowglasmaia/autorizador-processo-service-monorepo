# Tarefas — Autenticação JWT no backend

## 1. Preparação
- [ ] 1.1 Reconferir o que a etapa 6 entregou (`GlobalExceptionHandler`, `CorsConfig`, `ProblemDetail`) e o `pom.xml`; ler `API_CONTRACTS.md` §6 e SEC-13..SEC-20; registrar a ausência de `.specs/06-execution-plan/`.
- [ ] 1.2 Adicionar ao `pom.xml` os starters do Spring Security (resource server) e de testes de segurança, só o que faltar.

## 2. Contrato
- [ ] 2.1 Criar `openapi/modules/auth.yaml` (`autenticar`, `obterUsuarioAtual`, `LoginRequest`, `TokenResponse`, `Usuario`).
- [ ] 2.2 Criar `openapi/common/security.yaml` (`bearerAuth` global, `login` como exceção) e as respostas `401/429` em `responses.yaml`.
- [ ] 2.3 Configurar a execução do gerador para `auth`, regenerar e validar o contrato.

## 3. Banco
- [ ] 3.1 Criar `V3__usuario.sql` com a tabela `usuario`, `ux_usuario_email_lower` e `CHECK (email = lower(email))`.

## 4. Infraestrutura de segurança
- [ ] 4.1 Criar `shared.security.SecurityConfig` stateless (CSRF off, CORS integrado, `permitAll` só em login, health e documentação do perfil `local`).
- [ ] 4.2 Configurar `JwtEncoder/JwtDecoder` HS256 com validação de `iss` e `exp` e falha de inicialização se `JWT_SECRET` faltar ou tiver menos de 32 bytes.
- [ ] 4.3 Criar `AuthenticationEntryPoint` e `AccessDeniedHandler` em `problem+json` (`NAO_AUTENTICADO`, `SESSAO_EXPIRADA`, `ACESSO_NEGADO`) com `WWW-Authenticate: Bearer`.
- [ ] 4.4 Expor `PasswordEncoder` BCrypt (força ≥ 10).

## 5. Módulo autenticacao
- [ ] 5.1 Criar o domínio `Usuario` (e-mail normalizado) e as portas `UsuarioRepository`, `EmissorToken`, `HashSenha` e `LimitadorTentativas`.
- [ ] 5.2 Implementar `AutenticarUsuarioUseCase` (mensagem única, hash fictício, limitador, emissão do token) e `ObterUsuarioAtualUseCase`.
- [ ] 5.3 Implementar o adaptador JPA do repositório e o limitador em memória com relógio injetável.
- [ ] 5.4 Implementar `AuthController` sobre a interface gerada, com e-mail mascarado nos logs e sem senha/token.
- [ ] 5.5 Implementar o bootstrap do primeiro usuário (`ApplicationRunner`, só com tabela vazia e as duas variáveis presentes).

## 6. Testes
- [ ] 6.1 Criar o utilitário de testes que emite JWT válido e definir `JWT_SECRET` próprio nos perfis de teste.
- [ ] 6.2 Testar domínio e casos de uso (credencial válida/inválida, usuário inativo, caixa do e-mail, limitador).
- [ ] 6.3 Testar a persistência em PostgreSQL (unicidade sem caixa, bootstrap idempotente).
- [ ] 6.4 Testar a API (login `200`, `401` sem token, expirado, adulterado, `alg=none`, `iss` errado, `429` com `Retry-After`).
- [ ] 6.5 Criar o teste que varre os mapeamentos e falha se endpoint não público responder sem `401`; testar ausência de senha/token em logs e erros.
- [ ] 6.6 Estender o `ArchitectureTest` (`shared.security` sem features; `autenticacao.domain` sem Spring/JPA/Jackson).

## 7. Documentação e fechamento
- [ ] 7.1 Documentar `JWT_SECRET`, `JWT_TTL_MINUTOS`, `AUTH_BOOTSTRAP_EMAIL` e `AUTH_BOOTSTRAP_SENHA` no `README.md` do backend e no `.env.example`, sem valores reais.
- [ ] 7.2 Rodar `./mvnw verify` e o `git grep` de segredo literal; corrigir falhas e registrar bloqueio se o Docker faltar.
- [ ] 7.3 Registrar a evidência por tarefa, o resumo e arquivar a spec com `git mv` para `.specs/archive/`.
