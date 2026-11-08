# Evidência · Etapa 29 · Segurança e hardening

Atualizada por lote. **Lote 1** cobre as tarefas 1.1, 1.2, 2.1–2.4, 3.1 e 3.2. O **lote 2** cobre 3.3, 3.4, 4.1–4.4, 5.1 e 5.2 (seção "Lote 2").

## 1.1 Leituras e desvios
- Lidos: `.specs/05-quality/SPEC-41-seguranca-hardening.md`, `.specs/05-quality/SECURITY_REQUIREMENTS.md` (SEC-01..SEC-21), `.specs/01-architecture/DECISIONS.md` (ADR-16, PEN-09..PEN-12, PRM-09).
- **Desvio:** a pasta `.specs/06-execution-plan/` citada em `.specs/INDEX.md` **não existe**; ignorada conforme as regras do loop. `SECURITY_REQUIREMENTS.md` está em `.specs/05-quality/` (não na raiz).
- A SPEC-41 cita SEC-01..12; o arquivo de requisitos tem SEC-01..SEC-21 e todos foram tratados.
- Docker 29.9.2 disponível; o plugin `docker compose` **não** está instalado, mas há `docker-compose` v5.6.1 (o script de sondagem usa o que existir).

## 1.2 Tabela SEC-01..SEC-21

Legenda: **Sondagem** = `tools/security/sondagem.sh` (45/45 passaram, execução abaixo). **Teste** = teste existente em `api-autorizador-processo/src/test` ou `app-autorizador-processo/src` (executados na verificação completa da tarefa 6.1/6.2; nesta sessão rodaram `AuthApiTest`, `GlobalExceptionHandlerTest` e `LimiteTamanhoRequisicaoTest`, verdes).

| ID | Requisito (resumo) | Comando / teste | Resultado |
|---|---|---|---|
| SEC-01 | Nenhum segredo versionado | `application.yml` sem padrão para `JWT_SECRET`/`DB_PASSWORD`; `.env` no `.gitignore` (`git check-ignore .env` → `.env`) | OK (ver 4.1) |
| SEC-02 | Erro sem stack trace/SQL/classe | Sondagem `ERRO` (6 casos) + `GlobalExceptionHandlerTest`, `ContratoErroInternoApiTest` | OK. A sondagem achou 405/415 respondendo 500 → **corrigido** (ver achados) |
| SEC-03 | Validação de entrada; `id` no corpo ignorado; limite de corpo | Sondagem `IN-01` (201, id gerado ≠ enviado), `BODY-01..03` (413); `ProcedimentoApiTest.camposInvalidosRetornam400…`; `LimiteTamanhoRequisicaoTest` | OK |
| SEC-04 | Dados do paciente fora da URL e dos logs de acesso | `RequestLoggingFilter` registra só método, caminho, status e duração; `VarreduraSegurancaTest.senhaETokenNaoAparecemEmLogsNemEmErros` | OK por teste existente; revisão do contrato OpenAPI (POST com corpo) mantida da etapa 8 |
| SEC-05 | CORS por allowlist, sem `*` | Sondagem `CORS-01` (Origin `evil.example` → preflight 403, sem `Access-Control-Allow-Origin`), `CORS-02`; `GlobalExceptionHandlerTest.preflight…` | OK |
| SEC-06 | Cabeçalhos de segurança no Nginx | Sondagem `HDR-01` (`curl -I /`: nosniff, Referrer-Policy, X-Frame-Options, CSP), `HDR-02` (`Server: nginx` sem versão) | OK. `Permissions-Policy` e `Cache-Control` na API: ver 3.3 |
| SEC-07 | Actuator só `health`/`info`; Swagger só em `local` | Sondagem `ACT-env/beans/heapdump/metrics/mappings/loggers` (404 pelo Nginx e direto na API, autenticado), `ACT-health` (sem detalhes), `DOC` (404 no perfil `prod`); `ActuatorExposicaoTest`, `DocumentacaoPerfilPadraoTest` | OK. Ver achado sobre o perfil `local` no Compose |
| SEC-08 | Contêiner sem root, imagem mínima | Ver 3.4 | OK |
| SEC-09 | Dependências sem vulnerabilidade crítica | Ver 4.2–4.4 | OK (0 crítica/alta) |
| SEC-10 | SQL só parametrizado | Sondagem `INJ` (`x'); DROP TABLE procedimento;--` gravado literalmente, tabela intacta); JPA/Flyway sem concatenação; `ArchitectureTest` | OK |
| SEC-11 | Frontend sem `innerHTML` com dado externo; sem segredo no bundle | `grep` por `innerHTML`/`bypassSecurity` em `app-autorizador-processo/src` (sem ocorrências no código de produção); sondagem `INJ` confirma resposta `application/json` + `nosniff` | OK (ver 4.1) |
| SEC-12 | Somente atrás de HTTPS | README, seção "Segurança › HTTPS" | Documentado |
| SEC-13 | Todas as rotas `/api/v1/**` exigem JWT (exceto login) | Sondagem `JWT-12`; `VarreduraSegurancaTest.todoEndpointNaoPublicoRetorna401SemToken` | OK |
| SEC-14 | JWT HS256, segredo ≥ 32 bytes, `exp`/`iss`/assinatura, `alg=none`/algoritmo trocado rejeitados | Sondagem `JWT-00..JWT-11` (controle aceito; adulterado ×2, expirado, `alg=none` ×2, HS512, HS384, `iss` errado, outro segredo, sem `exp`, lixo → 401 `problem+json`); `AuthApiTest`; `JwtConfig` falha sem segredo | OK. A sondagem achou token **sem `exp` aceito** → **corrigido** |
| SEC-15 | Senha só como BCrypt (força ≥ 10); nunca em log | `PasswordEncoderConfig` (BCrypt força 12); `VarreduraSegurancaTest` (log); `UsuarioTest` | OK por teste/código existente |
| SEC-16 | Login sem enumeração e com tempo equivalente | Sondagem `LOGIN-01` (mesma resposta `401 CREDENCIAIS_INVALIDAS` para inexistente, senha errada e inativo), `LOGIN-02` (medianas 234 / 232 / 234 ms); `AuthApiTest` | OK |
| SEC-17 | 5 falhas/15 min → 429 + `Retry-After` | Sondagem `LOGIN-04` (5×401, 6ª → 429, `Retry-After: 899`, `problem+json`), `LOGIN-05`; `LimitadorTentativasAdapterTest` | OK |
| SEC-18 | Stateless, sem cookies, TTL 60 min, sem refresh | `SecurityConfig` (`STATELESS`, CSRF off); `AuthApiTest.loginValido…ValidadeConfigurada` | OK |
| SEC-19 | Frontend: token em `sessionStorage`, anti open redirect, 401 limpa sessão | `auth.service.spec.ts`, `auth.guard.spec.ts`, `error.interceptor.spec.ts`, `retorno-seguro.ts`; `grep localStorage` em `src` (verificar na 6.2) | Teste existente; confirmação na tarefa 6.2 |
| SEC-20 | Logs sem senha/token/segredo; 401/403 em `problem+json` | Sondagem `JWT-*` (todos `problem+json`); `VarreduraSegurancaTest`, `AcessoNegadoContratoTest` | OK |
| SEC-21 | Riscos aceitos no README | README, seção "Riscos aceitos" | OK |

## 2.1–2.4 Sondagens ativas (`tools/security/`)
- `tools/security/sondagem.sh`: cria `.env` **descartável** em diretório temporário (segredos aleatórios, portas 18090/14210/15440, projeto Compose `sondagem-seguranca`, perfil `prod`), sobe o Compose com `--build --wait`, cria um usuário inativo por SQL, roda `sondagem.mjs` e **derruba tudo com `down -v`** (trap), mesmo em falha. Nenhum processo ou contêiner fica ativo (conferido com `docker ps -a`).
- `tools/security/sondagem.mjs` (Node ≥ 20, sem dependências) forja os tokens com `crypto` e valida o controle positivo (`JWT-00`) para que os `401` não sejam falsos positivos.
- Execução final: **45/45 passaram**. A rajada de 25 logins pelo Nginx (`NGX-01`) deu 12×`401` e depois `429` com `Retry-After: 60`.
- Forçar um `500` real não é possível sem rota de teste em produção; `ERRO_INTERNO` genérico segue coberto por `ContratoErroInternoApiTest`. A sondagem `ERRO` usa JSON malformado, tipo de parâmetro errado, rota inexistente, método não permitido, `Content-Type` não suportado e `sort` inválido.
- "Exibido escapado" (HTML no `nome`): a API devolve o texto literal como `application/json` com `nosniff`; o escape na tela é o do Angular (interpolação) — sem `innerHTML` no código de produção.

### Achados das sondagens e correções (com teste)
1. **Token sem `exp` era aceito (200)** → `JwtConfig` agora exige `exp` (`exigeExpiracao()`); teste `AuthApiTest.tokenSemExpiracaoRetorna401`.
2. **`405` e `415` respondiam `500 ERRO_INTERNO`** (sem vazamento, mas status errado) → `GlobalExceptionHandler` trata `HttpRequestMethodNotSupportedException` (405 + `Allow`) e `HttpMediaTypeNotSupportedException` (415); testes em `GlobalExceptionHandlerTest`.
3. **Corpo JSON sem limite no Spring** → `LimiteTamanhoRequisicaoFilter` (ver 3.1).
4. **Compose sobe a API no perfil `local` por padrão** (seed, Swagger, `health` com detalhes). É intencional (o e2e depende da seed); passou a ser configurável por `SPRING_PROFILES_ACTIVE` no `compose.yaml` e a sondagem roda em `prod`. Em produção, usar `prod` (documentar na tarefa 5.x).
5. **Observação (não alterada):** atrás do Nginx a API vê o IP do próprio Nginx em `request.getRemoteAddr()` (sem `forward-headers-strategy`), então o limitador por (e-mail, IP) da API é, na prática, só por e-mail no Compose. O `limit_req` por IP do Nginx (3.2) cobre a lacuna. Registrar como risco/observação no README (tarefa 5.2).

## 3.1 Limites de corpo e de cabeçalho
- Spring: `app.http.max-body-bytes` (`HTTP_MAX_BODY_BYTES`, padrão 65536) via `LimiteTamanhoRequisicaoFilter` (413 para `Content-Length` acima do limite, 411 para corpo sem tamanho declarado, ambos `problem+json` com `code: CORPO_INVALIDO`, sem ampliar o enum do contrato); `server.max-http-request-header-size` (`HTTP_MAX_HEADER_BYTES`, padrão 16 KB).
- Nginx: `client_max_body_size` (`NGINX_MAX_BODY`, padrão 64k) e `large_client_header_buffers` (`NGINX_MAX_HEADER_BUFFER`, padrão 8k) em `nginx/limites.conf.template`, renderizado por `envsubst` no início do contêiner (diretório `/etc/nginx/templates` da imagem). `default.conf` virou `default.conf.template` (usa `NGINX_LOGIN_BURST`).
- Regressão: `LimiteTamanhoRequisicaoTest` (413, fluxo normal dentro do limite, 411) e sondagens `BODY-01..03`, `HDR-05` (cabeçalho de 40 KB → 400 na API e no Nginx).
- Variáveis expostas no `compose.yaml` e no `.env.example`.

## 3.2 `limit_req` no login pelo Nginx — **aplicado**
- `limit_req_zone $binary_remote_addr zone=login:10m rate=${NGINX_LOGIN_RATE}` (padrão `30r/m`) e `limit_req … burst=${NGINX_LOGIN_BURST}` (padrão 10, `nodelay`) em `location = /api/v1/auth/login`; resposta `429` `problem+json` com `Retry-After: 60` e `code: MUITAS_TENTATIVAS` (já no contrato). Valores conservadores o bastante para o uso humano e o e2e; sondagem `NGX-01` confirma.
- Limitação: atrás de outro proxy/NAT o `$binary_remote_addr` é o do proxy; documentar junto à recomendação de HTTPS.
- Teste isolado do contêiner Nginx (imagem construída e executada com `--add-host api:127.0.0.1`): `429` com os cabeçalhos de segurança e `Retry-After`, `413` para corpo de 100 KB.

## Lote 2

### 3.3 Cabeçalhos
- `security-headers.inc` ganhou `Permissions-Policy` (câmera, microfone, geolocalização, pagamento, USB e FLoC desligados) e `Cross-Origin-Opener-Policy: same-origin`. A CSP não foi alterada.
- `Cache-Control` na API: o Spring Security já emite `no-cache, no-store, max-age=0, must-revalidate` (cabeçalhos padrão mantidos); o Nginx repassa. Teste novo `AuthApiTest.respostasDaApiNaoSaoArmazenadasEmCache` (AuthApiTest 12/12 verdes) e sondagens `HDR-03`/`HDR-04` em `tools/security/sondagem.mjs` (não reexecutadas neste lote; exigem subir o Compose).

### 3.4 Usuário sem root e imagem mínima (SEC-08)
- API: `USER app` (usuário de sistema, `nologin`, sem home); runtime `eclipse-temurin:25-jre` (sem JDK/Maven); `curl` só para o healthcheck.
- Frontend: runtime `nginxinc/nginx-unprivileged:stable-alpine` (uid 101, porta 8080, sem Node/npm), só com o resultado estático. Nenhuma correção necessária.

### 4.1 Segredos
- Árvore atual: `git grep` por senha/segredo literal → só valores de desenvolvimento em `.env.example` e padrões de `compose.yaml` (`autorizador`); `JWT_SECRET` sem padrão no `application.yml`. Nenhum `.env` versionado (só `.env.example` na raiz e em `api-autorizador-processo`).
- A senha MySQL da versão original (`spring.datasource.password=2522`) aparece **apenas no histórico/tag `v0-original`**; `2522` e `mysql` não existem na árvore atual. O histórico não pode ser alterado (regra do loop): risco residual registrado, trocar a senha se ainda for usada em algum lugar.
- Bundle (`npm run build`, `dist/`): busca por `jwt_secret`, `password=`, chaves privadas e prefixo de JWT (`eyJhbGci`) → nenhuma ocorrência.

### 4.2 Dependências
- npm: `npm audit --omit=dev` → 0 vulnerabilidades. `npm audit` completo → 31 moderadas, todas em dependências de desenvolvimento (0 altas/críticas); o CI já bloqueia `--audit-level=high --omit=dev`.
- Maven: `dependency:list` não resolveu neste ambiente (falha PKIX ao baixar o plugin). Alternativa: 128 jars do `BOOT-INF/lib` do artefato, 127 mapeados para coordenadas Maven (via `~/.m2`) e consultados no OSV.dev → 1 achado: `io.opentelemetry:opentelemetry-api:1.62.0` (GHSA-ccrj-gw8r-854m, severidade **moderada**, consumo quadrático no parse de `tracestate`; corrigido em 1.67.0).

### 4.3 Imagens base e workflows
- Trivy/Grype/Docker Scout não estão instalados: **varredura de imagens não executada** (limitação). Mitigação: imagens oficiais, runtime mínimo e sem root; recomenda-se `trivy image` no CI futuramente.
- Workflows: os três têm `permissions: contents: read` no nível do fluxo; nenhum usa `secrets.*`, `pull_request_target` ou publica imagem. Ações referenciadas por tag (`@v4`/`@v3`), sem fixação por SHA (risco baixo, anotado).

### 4.4 Atualização
- Sem vulnerabilidade crítica/alta; a moderada do OpenTelemetry vem gerida pelo BOM do Spring Boot (não se sobrescreve a versão para não divergir do BOM). Nenhuma dependência alterada.

### 5.1–5.2 README
- Nova seção "Segurança" no README raiz: modelo de ameaça, recomendação de HTTPS (SEC-12) e riscos aceitos (PEN-10/11/12, incluindo a observação do IP do Nginx).

## Lote 3 · Verificação final

### 6.1 Backend
- `./mvnw verify` em `api-autorizador-processo` (JDK 25.0.2): **EXIT=0**, todos os testes verdes (incluindo Testcontainers e `ArchitectureTest`). Os stack traces no console são logs esperados de testes de erro.

### 6.2 Frontend
- `npm run lint`: passou. `npm run build`: passou. `npm test`: 32 arquivos, **226 testes passando**.
- SEC-19: nenhum uso de `localStorage` em código de produção de `src` (confirmado por busca).

### 6.3 Arquivamento
- `SPEC-41-seguranca-hardening.md` movida com `git mv` de `.specs/05-quality/` para `.specs/archive/`.

## Resumo
Todas as tarefas 1.1–6.3 concluídas. SEC-01..SEC-21 verificados ou documentados. Correções feitas a partir das sondagens: `exp` obrigatório no JWT, 405/415 corretos, limite de corpo/cabeçalho (Spring e Nginx), `limit_req` no login, `Permissions-Policy`/COOP. Limitações: varredura de imagens (Trivy/Grype ausentes) não executada; `dependency:list` do Maven indisponível (usado OSV nos jars: 1 moderada, gerida pelo BOM); senha MySQL antiga só no histórico/tag `v0-original`.
