# TARGET_ARCHITECTURE — arquitetura de destino

Rótulos: **[DECISÃO]** (ver `DECISIONS.md`), **[PREMISSA]**. Proporcional ao domínio: 2 agregados, 1 regra central, 0 integrações externas — por isso **sem** CQRS, eventos, DDD tático pesado ou microsserviços.

## 1. Visão de implantação
```text
Navegador ──HTTP──▶ Nginx (porta 80 → host 4200)
                      ├─ /            → arquivos estáticos do Angular 21 (SPA, fallback para index.html)
                      └─ /api/*       → proxy_pass http://api:8080  (mesma origem: sem CORS em produção/compose)
                                             │
                              Spring Boot 4 / Java 25  (porta 8080, health em /actuator/health)
                                             │ JDBC
                                       PostgreSQL 16  (volume nomeado)
```
O `compose.yaml` da raiz (já existente, não rastreado) é a base: serviços `postgres`, `api`, `app`; `app` depende de `api` saudável. Ajustes necessários estão em SPEC-10 e SPEC-13 (o `api` do compose ainda não declara `healthcheck`, mas `app` exige `service_healthy`).

## 2. Backend `api-autorizador-processo`
Pacote base `org.maia.autorizador`. Hexagonal Pragmática (skill `java-spring-create-module`), pacote por funcionalidade, que coincide com módulos Spring Modulith do scaffold:

```text
org.maia.autorizador
├─ AutorizadorApplication
├─ procedimento/            rich
│  ├─ api/                  ProcedimentoController (implementa interface gerada), mapeadores web
│  ├─ application/{port,usecase}   CadastrarProcedimentoUseCase, ListarProcedimentosUseCase, ObterProcedimentoUseCase ; port ProcedimentoRepository
│  ├─ domain/               Procedimento (regra avaliar(idade, sexo)), Sexo, TipoCobertura, Categoria..., exceções
│  └─ infrastructure/persistence   ProcedimentoEntity, JpaRepository, ProcedimentoRepositoryAdapter ; seed/ (perfil local)
├─ autorizacao/             rich
│  ├─ api/  application/{port,usecase}  SolicitarAutorizacaoUseCase, ListarAutorizacoesUseCase
│  ├─ domain/ Autorizacao, AutorizacaoNegadaException
│  └─ infrastructure/persistence
├─ painel/                  crud-leitura: ObterResumoPainelUseCase ; port PainelConsulta (SQL agregado)
├─ autenticacao/            rich: AutenticarUsuarioUseCase, ObterUsuarioAtualUseCase ; ports UsuarioRepository, EmissorToken, HashSenha, LimitadorTentativas ; infrastructure/{persistence,security}
└─ shared/ {error, web (GlobalExceptionHandler, ProblemDetail), config (Clock, CORS, OpenAPI UI), security (SecurityConfig, JWT encoder/decoder, entry points — sem importar features)}
```
- Domínio **sem** Spring/JPA/Jackson/Lombok; `Procedimento.avaliar(idade, sexo)` concentra RN5–RN7 (testes de domínio puros).
- `autorizacao` usa o caso de uso `ObterProcedimentoPorNumero` de `procedimento` (decisão D6 da skill; nunca `infrastructure`).
- **Transações:** `@Transactional` no caso de uso. Cadastro captura violação de UNIQUE → `PROCEDIMENTO_DUPLICADO` (elimina a corrida da versão original). Consultas `readOnly`.
- **Persistência:** JPA/Hibernate + Flyway (`ddl-auto=none`), `open-in-view=false`, coleções `sexos`/`idades` com `@BatchSize` (sem N+1), consultas de painel em SQL agregado. Envers para RN10 (ver `DATABASE_DESIGN.md`).
- **Contrato:** controllers implementam interfaces geradas (`interfaceOnly`) por módulo; DTOs gerados ficam em `api`.
- **Erros:** `GlobalExceptionHandler` único → RFC 9457 + `code`; sem stack trace; validação → `errors[]`.
- **CORS:** lista de origens por variável (`CORS_ALLOWED_ORIGINS`), ativa no perfil `local` para `ng serve` (4200→8080); em compose a mesma origem dispensa CORS.
- **Configuração:** perfis `local` (seed, swagger UI, logs legíveis), `docker`/padrão (JSON ECS). Segredos só por variável de ambiente (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `APP_ENV`, `LOG_LEVEL_APP`).
- **Observabilidade:** logs JSON estruturados com correlação, `traceId/spanId` (OpenTelemetry nativo do Boot, export desligado), actuator: `health` (liveness/readiness) e `info`; nada além exposto. Log de eventos de negócio nos casos de uso (procedimento cadastrado, autorização concedida/negada) **sem** dados do paciente além de idade/sexo agregados — não há identificação de paciente.
- **Segurança:** Spring Security stateless; JWT HS256 (`JWT_SECRET`); `/api/v1/**` exige Bearer exceto `POST /auth/login`; `actuator/health*` público; entry points devolvem `problem+json` (SPEC-22).
- **Testes:** ver `05-quality/TEST_STRATEGY.md`. Arquitetura imposta por **ArchUnit** (`ArchitectureTest`), complementada por `ApplicationModules.verify()`.

### 2.1 Regras ArchUnit obrigatórias (`ArchitectureTest`)
1. `..domain..` não depende de `org.springframework..`, `jakarta.persistence..`, `com.fasterxml.jackson..`, `lombok..`, `org.slf4j..`.
2. Dependências só para dentro: `api → application → domain` e `infrastructure → application → domain`; `domain` não importa `application`, `api` nem `infrastructure`; `application` não importa `api` nem `infrastructure`.
3. `api` (controllers) depende de `application.usecase` e dos tipos gerados, **nunca** de `infrastructure`.
4. `@Entity`/`@Table`/Spring Data só em `..infrastructure.persistence..`.
5. Casos de uso: classes `*UseCase` ficam em `..application.usecase..`, concretas, `@Service`, com `@Transactional`; sem `*Service`/`*Impl` em `..application..`; portas em `..application.port..` são interfaces.
6. Fatias por funcionalidade sem ciclos (`slices().matching("org.maia.autorizador.(*)..").should().beFreeOfCycles()`); uma funcionalidade só usa outra por `application.usecase`/`domain` (nunca `infrastructure`/`api`).
7. `shared` não importa nenhuma funcionalidade; `shared.security` isolado.
8. Controllers não usam `Repository` e não retornam entidade JPA nem objeto de domínio.
9. **Nenhum pacote ou classe com nome contendo "legado" ou "legacy"** (instrução do responsável).
10. Nomenclatura: adaptadores terminam em `Adapter`, casos de uso em `UseCase`, portas sem sufixo `Impl`.
Falha de regra falha o `verify` (e o CI do backend).

## 3. Frontend `app-autorizador-processo`
Angular 21, standalone, zoneless, Signals, Reactive Forms tipados, `@if/@for`, `inject()`, `OnPush`, Tailwind (via PostCSS, tokens do protótipo).

```text
src/app
├─ app.config.ts / app.routes.ts          rotas lazy por funcionalidade
├─ core/        http (api-base-url, error interceptor funcional), auth (sessão JWT, interceptor, guards), config (environment), errors (AppError, mapeamento de ProblemDetail), notificações (toast), carregamento
├─ layout/      shell (sidebar recolhível, topo mobile), status da API
├─ shared/ui/   botões, tags, cartões, tabela responsiva, campos, diálogo, estado vazio, skeleton, chips
├─ shared/api/  tipos gerados do OpenAPI (somente tipos)
└─ features/
   ├─ login/         tela de entrada (fora do shell)
   ├─ painel/        painel.routes.ts, pages/, ui/, data-access/, doc.md
   ├─ procedimentos/ lista + cadastro em 4 etapas
   ├─ solicitar/     solicitação e resultado
   └─ autorizacoes/  histórico
```
- Rotas: `/login`, `/painel` (padrão), `/procedimentos`, `/procedimentos/novo`, `/solicitar` (aceita `?numero=`), `/autorizacoes`; `**` → página "não encontrado".
- Estado: Signals no serviço de dados de cada funcionalidade (um proprietário por dado); RxJS só para HTTP/cancelamento. Sem store global.
- Interceptor funcional converte falhas HTTP em `AppError` tipado; **403 com `code=AUTORIZACAO_NEGADA` é resultado de negócio**, não erro (UX do protótipo).
- Guards: `authGuard` (todas as rotas de funcionalidade) e `guestGuard` (login); interceptor de autenticação anexa Bearer só à API e trata `401`; token em `sessionStorage` (SPEC-23).
- Regras oficiais permanecem no backend; o frontend só valida formato e conveniência (ex.: expansão de faixa de idade em idades exatas).
- Acessibilidade WCAG 2.2 AA, `prefers-reduced-motion`, responsivo de 320 px a desktop.

## 4. Nginx
`nginx:alpine` multi-stage com build Node. `try_files` para SPA; `/api/` proxy; compressão gzip; cache longo para arquivos com hash e `no-cache` para `index.html`; cabeçalhos `X-Content-Type-Options`, `Referrer-Policy`, `X-Frame-Options`, CSP compatível com fontes Google e unpkg usados pelo protótipo **ou**, preferencialmente, com fontes/ícones empacotados (decisão em SPEC-15); `/healthz` para healthcheck.

## 5. Monorepo
Ver `MONOREPO_STRUCTURE.md`. Raiz sem build agregador: cada projeto constrói sozinho; o `compose.yaml` integra.

## 6. Premissas
PRM-01 a PRM-06 em `DECISIONS.md`. Principais riscos: compatibilidade Envers com o Hibernate do Boot 4.1.2 (mitigação: spike em SPEC-04, plano B por gatilhos SQL) e exposição indevida do `JWT_SECRET` (SPEC-22/41). JDK 25.0.2 disponível em `C:\jdk-25.0.2`.
