# Autorizador de Procedimentos Médicos

Monorepo do **Autorizador de Procedimentos Médicos**: cadastro de procedimentos, solicitação e consulta de autorizações (com critérios de idade e sexo do paciente) e painel com indicadores.

> 🚧 **Em construção.** O repositório está sendo modernizado por etapas, seguindo as especificações em [`.specs/`](.specs/INDEX.md). O backend (`api-autorizador-processo`) já tem a fundação pronta (esqueleto, ArchUnit e Modulith); o frontend (`app-autorizador-processo`) tem a fundação pronta (shell Angular 21, testes e lint). O que está marcado como "em construção" abaixo nasce nas próximas etapas.

## Visão

- **Backend** (`api-autorizador-processo`): Spring Boot 4.1.2, Java 25, Maven Wrapper, PostgreSQL 16, Flyway, contrato OpenAPI 3.1, erros RFC 9457 e autenticação JWT por e-mail e senha. Fundação pronta; funcionalidades em construção (veja [`api-autorizador-processo/README.md`](api-autorizador-processo/README.md))
- **Frontend** (`app-autorizador-processo`): Angular 21, TypeScript estrito, Tailwind CSS, componentes standalone, Signals e formulários reativos tipados, consumindo tipos gerados do OpenAPI. 🚧 em construção
- **Integração local**: Docker Compose com PostgreSQL, API e frontend. Cada projeto terá seu próprio Dockerfile e seu próprio CI.

## Estrutura de destino

```text
.
├─ api-autorizador-processo/   backend Spring Boot 4 · Java 25 · Maven            fundação pronta
├─ app-autorizador-processo/   frontend Angular 21 · Tailwind                     fundação pronta
├─ tools/parity/               casos e replay de paridade com a versão original   🚧 em construção
├─ .github/workflows/          CI independente por projeto                        🚧 em construção
├─ compose.yaml · .env.example integração local                                   (existentes; ajustados nas próximas etapas)
├─ README.md                   este arquivo
└─ .specs/                     especificações, memória do projeto e design
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

## Executar o backend novo

```bash
cd api-autorizador-processo
./mvnw clean verify
./mvnw spring-boot:run   # http://localhost:8080/actuator/health
```

## Versão original

A versão original da API (tag `v0-original`) oferece:

1. Cadastrar novos procedimentos.
2. Consultar procedimentos cadastrados.
3. Autorizar a realização de procedimentos com critérios de validação por idade e sexo do paciente.
4. Consultar procedimentos autorizados.

Para executá-la, faça checkout da tag `v0-original` (`git checkout v0-original`), rode `./mvnw spring-boot:run` e acesse `http://localhost:8080/swagger-ui.html`. Com `spring.profiles.active=test`, o console do H2 fica em `http://localhost:8080/h2-console`.

## Especificações

- [Índice](.specs/INDEX.md)
- [Produto](.specs/memory/produto.md) e [contexto técnico](.specs/memory/contexto-tecnico.md)
- [Estrutura do monorepo](.specs/01-architecture/MONOREPO_STRUCTURE.md)
- [Como executar uma spec](.specs/shared/como-executar.md)
- [Design system](.specs/design/DESIGN_SYSTEM_DISCOVERY.md) e protótipo em `.specs/design/prototype/`

## Créditos

- **Linkedin**
- www.linkedin.com/in/dowglasmaia
