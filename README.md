# Autorizador de Procedimentos Médicos

![Backend CI](../../actions/workflows/backend-ci.yml/badge.svg) ![Frontend CI](../../actions/workflows/frontend-ci.yml/badge.svg) ![Integração CI](../../actions/workflows/integracao-ci.yml/badge.svg)

Monorepo do **Autorizador de Procedimentos Médicos**: cadastro de procedimentos, solicitação e consulta de autorizações (com critérios de idade e sexo do paciente) e painel com indicadores.

> ✅ **Modernização concluída** (aceite em [`RELATORIO_FINAL.md`](.specs/05-quality/RELATORIO_FINAL.md)). Backend, frontend, Compose, CI, e2e, paridade com a versão original, segurança e desempenho foram entregues e verificados. As especificações estão em [`.specs/`](.specs/INDEX.md).

## Visão

- **Backend** (`api-autorizador-processo`): Spring Boot 4.1.2, Java 25, Maven Wrapper, PostgreSQL 16, Flyway, contrato OpenAPI 3.1, erros RFC 9457 e autenticação JWT por e-mail e senha. Veja [`api-autorizador-processo/README.md`](api-autorizador-processo/README.md).
- **Frontend** (`app-autorizador-processo`): Angular 21, TypeScript estrito, Tailwind CSS, componentes standalone, zoneless, Signals e formulários reativos tipados, consumindo tipos gerados do OpenAPI. Veja [`app-autorizador-processo/README.md`](app-autorizador-processo/README.md).
- **Integração local**: Docker Compose com PostgreSQL, API e frontend (Nginx). Cada projeto tem seu próprio Dockerfile e seu próprio CI.

## Pré-requisitos

| Ferramenta | Versão | Uso |
|---|---|---|
| JDK | 25 (`JAVA_HOME`) | backend |
| Node.js / npm | 24 (mínimo 20.19) | frontend, `scripts/` e `tools/` |
| Docker com Compose | 29+ | Compose, Testcontainers, e2e, paridade e desempenho |

## Comandos validados

| Onde | Comando | Resultado verificado na finalização |
|---|---|---|
| `api-autorizador-processo` | `./mvnw verify` | 247 testes, `ArchitectureTest` 23/23 |
| `app-autorizador-processo` | `npm ci && npm run lint && npm run build && npm test` | lint limpo, build ok, 226 testes |
| `app-autorizador-processo` | `npm run e2e` | 30 cenários (desktop e móvel) |
| raiz | `node tools/parity/replay.mjs --target v1` | 23/23 casos iguais ao baseline |
| raiz | `node scripts/check-contract.mjs` e `node scripts/check-contract-compat.mjs` | contrato, tipos e compatibilidade ok |

## Ferramentas (`tools/`)

| Pasta | Para quê |
|---|---|
| `tools/parity/` | casos e replay de paridade com a versão original (`replay.mjs`, `RELATORIO.md`) |
| `tools/contract/` | instantâneo base do contrato v1 usado pelo `check-contract-compat.mjs` |
| `tools/perf/` | smoke de desempenho (`perf.sh`; ver `PERFORMANCE_REPORT.md`) |
| `tools/security/` | sondagens ativas de segurança (`sondagem.sh`) |

## Estrutura do monorepo

```text
.
├─ api-autorizador-processo/   backend Spring Boot 4 · Java 25 · Maven · Dockerfile próprio
├─ app-autorizador-processo/   frontend Angular 21 · Tailwind · Nginx · Dockerfile próprio
├─ scripts/                    checks de contrato (tipos e compatibilidade)
├─ tools/                      parity · contract · perf · security
├─ .github/workflows/          CI independente por projeto (backend, frontend, integração)
├─ compose.yaml · .env.example integração local (só o .env.example é versionado)
├─ README.md                   este arquivo
└─ .specs/                     especificações, memória do projeto, relatórios e design
```

O código da **versão original** (Spring Boot 2.2.6 com banco H2) foi removido da raiz e fica preservado pela tag `v0-original`. O protótipo de design está em `.specs/design/prototype/`.

## Como subir com Compose

O `compose.yaml` sobe o PostgreSQL 16, a API e o frontend (Nginx). O `app` só inicia depois que a API fica `healthy`.

```bash
cp .env.example .env                                # valores de desenvolvimento; JWT_SECRET é obrigatório para a API
docker compose up -d postgres                       # somente o PostgreSQL (para rodar a API fora do Docker)
docker compose up -d --build postgres api           # banco + API (aguarda o banco ficar saudável)
docker compose up -d --build                        # banco + API + frontend (http://localhost:4200)
docker compose ps                                   # os 3 serviços ficam "healthy" em até ~90 s
curl http://localhost:8080/actuator/health          # {"status":"UP"}
docker compose down                                 # para e remove os contêineres; o volume postgres-data é preservado
docker compose down -v                              # também apaga o volume (APAGA os dados do banco)
```

| Serviço    | Porta padrão | Variável     |
|------------|--------------|--------------|
| PostgreSQL | 5432         | `POSTGRES_PORT` |
| API        | 8080         | `API_PORT`   |
| Frontend (`app`) | 4200   | `APP_PORT` (porta 8080 do contêiner) |

O Nginx do frontend serve a SPA com fallback para `index.html`, repassa `/api/` para a API (mesma origem, sem CORS), expõe `/healthz` (healthcheck do contêiner) e somente `/actuator/health` do actuator (`/actuator/env` e demais retornam 404). Detalhes em [`app-autorizador-processo/README.md`](app-autorizador-processo/README.md#contêiner-nginx).

Sem `JWT_SECRET` (mínimo de 32 bytes) o Compose recusa subir a API. O arquivo `.env` não é versionado; só o `.env.example`. Os testes de integração do backend usam Testcontainers e não dependem do Compose.

## Executar o backend

```bash
cd api-autorizador-processo
./mvnw clean verify      # exige Docker (Testcontainers)
./mvnw spring-boot:run   # http://localhost:8080/actuator/health (suba o postgres antes)
```

## Contrato OpenAPI: tipos, conformidade e compatibilidade

O contrato fica em `api-autorizador-processo/src/main/resources/openapi/`. Três verificações impedem o desvio entre contrato, backend e frontend.

**1. Tipos do frontend em dia** (requer `npm ci` em `app-autorizador-processo`):

```bash
node scripts/check-contract.mjs      # exit 0 = api-types.ts corresponde ao contrato
cd app-autorizador-processo && npm run api:types   # regenera src/app/shared/api/api-types.ts
```

Se falhar com "`api-types.ts` está defasado", o YAML mudou sem regenerar os tipos: rode `npm run api:types` e versione o resultado. Nunca edite `api-types.ts` à mão.

**2. Respostas reais conformes ao contrato** (requer Docker, pois usa Testcontainers): `./mvnw verify` em `api-autorizador-processo` executa `ContratoRespostasApiTest`, `ContratoErroInternoApiTest` e `AcessoNegadoContratoTest`, que chamam as 8 operações com a aplicação real e validam status, cabeçalhos e corpo contra o YAML (validador mínimo em `contrato/ContratoOpenApi`, sem dependência nova). Uma falha informa a operação e o campo divergente; corrija o código da API ou, se o contrato for o que mudou, o YAML (com a verificação 3).

**3. Compatibilidade com o instantâneo base** do contrato v1 (`tools/contract/baseline/`):

```bash
node scripts/check-contract-compat.mjs                    # falha em remoção, renomeação, mudança de tipo ou nova obrigatoriedade
node scripts/check-contract-compat.mjs --atualizar-base   # após mudança aditiva aprovada: atualiza o instantâneo
```

Mudanças aditivas (nova operação, propriedade opcional, novo valor de enum, nova resposta) passam. Uma quebra de compatibilidade exige nova versão da API (`/api/v2`), não a atualização do instantâneo. Depois de atualizar o instantâneo, regenere os tipos e versione tudo junto. No CI, as verificações 1 e 3 rodam no job `check-contract` do workflow de integração e a 2 roda dentro do `./mvnw verify` do Backend CI.

## CI por projeto

Cada projeto tem seu workflow em `.github/workflows/`, disparado só por mudança no próprio caminho (`push` e `pull_request`; todos aceitam também `workflow_dispatch`). Nada é publicado nem implantado, e nenhum segredo real é versionado: `JWT_SECRET` e senhas de e2e/paridade são gerados em tempo de execução.

| Workflow | Dispara quando muda | Jobs |
|----------|---------------------|------|
| `backend-ci.yml` | `api-autorizador-processo/**`, `tools/parity/**` | `./mvnw verify` (inclui `ArchitectureTest` e geração do contrato), build da imagem Docker, auditoria Maven (não bloqueante, só lista a árvore), paridade com o baseline via Compose |
| `frontend-ci.yml` | `app-autorizador-processo/**` | `npm ci`, lint, testes, build de produção, build da imagem Docker, `npm audit --audit-level=high --omit=dev` |
| `integracao-ci.yml` | `compose.yaml`, `.env.example`, `scripts/check-contract*.mjs`, `tools/contract/**` e os dois projetos | `check-contract` (tipos e compatibilidade do contrato), `e2e` (Compose completo + Playwright; relatório como artefato só em falha) |

O que roda quando: mudar só o frontend dispara Frontend CI e Integração CI; só o backend, Backend CI e Integração CI; só `compose.yaml`, apenas Integração CI.

**Branch protection.** Um check de workflow com filtro de `paths` que não dispara fica "pendente" e bloqueia o merge se for marcado como obrigatório. Marque como obrigatórios só checks que sempre rodam, ou crie um job final agregador sem filtro de caminho; na dúvida, não marque os checks por projeto como obrigatórios.

## Segurança

### Modelo de ameaça resumido

| Ativo | Ameaça | Mitigação |
|-------|--------|-----------|
| Dados do paciente e das autorizações | Acesso sem autenticação, token forjado, adulterado ou expirado | JWT HS256 com `exp`/`iss` obrigatórios, segredo ≥ 32 bytes vindo do ambiente; todas as rotas `/api/v1/**` exigem token, exceto o login |
| Contas de usuário | Força bruta, enumeração de e-mails | Resposta idêntica para e-mail inexistente, senha errada e usuário inativo; 5 falhas em 15 min → `429` com `Retry-After`; `limit_req` por IP no Nginx; senha só como BCrypt |
| API | Injeção (SQL/HTML), corpo ou cabeçalho gigantes, vazamento por erro | Consultas parametrizadas, validação de entrada, limites de corpo e de cabeçalho (Nginx e Spring), erros RFC 9457 sem stack trace, Actuator só com `health` |
| Navegador | XSS, clickjacking, sniffing, open redirect | Angular sem `innerHTML` com dado externo, CSP restritiva, `X-Frame-Options`, `nosniff`, `Referrer-Policy`, `Permissions-Policy`, token em `sessionStorage` e retorno pós-login validado |
| Segredos | Vazamento em repositório, imagem ou log | Só `.env.example` versionado; contêineres sem root; logs sem senha/token |

As sondagens ativas estão em `tools/security/` (`sondagem.sh` sobe o Compose com um `.env` descartável e derruba tudo ao final).

### HTTPS (SEC-12)

O Nginx do Compose escuta em HTTP puro (porta 8080). **Em qualquer ambiente real, coloque um terminador TLS (proxy reverso, balanceador ou ingress) com HTTPS na frente do Nginx**: o token trafega no cabeçalho `Authorization` e a senha no login, e sem TLS ficam legíveis na rede. Ao fazer isso: redirecione HTTP para HTTPS, ative `Strict-Transport-Security` no terminador, rode a API no perfil `prod` (`SPRING_PROFILES_ACTIVE=prod`, sem seed nem Swagger) e repasse o IP real do cliente (`X-Forwarded-For`); atrás de um proxy/NAT o `limit_req` do Nginx enxerga o IP do proxy.

### Riscos aceitos

- **Sem refresh token (PEN-10):** o token dura 60 minutos e, ao expirar, o usuário entra de novo. Não há revogação antes do vencimento.
- **Limitador de login em memória (PEN-11):** vale para uma única instância da API; com várias réplicas cada uma conta separado e o contador zera no reinício. Atrás do Nginx a API vê o IP do próprio Nginx, então, no Compose, o limite da API é na prática por e-mail; o `limit_req` do Nginx cobre o IP.
- **Sem papéis/perfis (PEN-12):** todo usuário autenticado acessa todas as funções. Não há cadastro de usuários nem recuperação de senha.

## Versão original

A versão original da API (tag `v0-original`) oferece:

1. Cadastrar novos procedimentos.
2. Consultar procedimentos cadastrados.
3. Autorizar a realização de procedimentos com critérios de validação por idade e sexo do paciente.
4. Consultar procedimentos autorizados.

Para executá-la, faça checkout da tag `v0-original` (`git checkout v0-original`), rode `./mvnw spring-boot:run` e acesse `http://localhost:8080/swagger-ui.html`. Com `spring.profiles.active=test`, o console do H2 fica em `http://localhost:8080/h2-console`.

## Especificações

- [Índice](.specs/INDEX.md) e [relatório final](.specs/05-quality/RELATORIO_FINAL.md)
- [Produto](.specs/memory/produto.md) e [contexto técnico](.specs/memory/contexto-tecnico.md)
- [Estrutura do monorepo](.specs/01-architecture/MONOREPO_STRUCTURE.md)
- [Como executar uma spec](.specs/shared/como-executar.md)
- [Design system](.specs/design/DESIGN_SYSTEM_DISCOVERY.md) e protótipo em `.specs/design/prototype/`

## Créditos

- **Linkedin**
- www.linkedin.com/in/dowglasmaia
