# Autorizador de Procedimentos Médicos

Monorepo do **Autorizador de Procedimentos Médicos**: cadastro de procedimentos, solicitação e consulta de autorizações (com critérios de idade e sexo do paciente) e painel com indicadores.

> 🚧 **Em construção.** O repositório está sendo modernizado por etapas, seguindo as especificações em [`.specs/`](.specs/INDEX.md). Os projetos novos (`api-autorizador-processo` e `app-autorizador-processo`) ainda não existem; o que está marcado como "em construção" abaixo nasce nas próximas etapas.

## Visão

- **Backend** (`api-autorizador-processo`): Spring Boot 4.1.2, Java 25, Maven Wrapper, PostgreSQL 16, Flyway, contrato OpenAPI 3.1, erros RFC 9457 e autenticação JWT por e-mail e senha. 🚧 em construção
- **Frontend** (`app-autorizador-processo`): Angular 21, TypeScript estrito, Tailwind CSS, componentes standalone, Signals e formulários reativos tipados, consumindo tipos gerados do OpenAPI. 🚧 em construção
- **Integração local**: Docker Compose com PostgreSQL, API e frontend. Cada projeto terá seu próprio Dockerfile e seu próprio CI.

## Estrutura de destino

```text
.
├─ api-autorizador-processo/   backend Spring Boot 4 · Java 25 · Maven            🚧 em construção
├─ app-autorizador-processo/   frontend Angular 21 · Tailwind                     🚧 em construção
├─ tools/parity/               casos e replay de paridade com a versão original   🚧 em construção
├─ .github/workflows/          CI independente por projeto                        🚧 em construção
├─ compose.yaml · .env.example integração local                                   (existentes; ajustados nas próximas etapas)
├─ README.md                   este arquivo
└─ .specs/                     especificações, memória do projeto e design
```

Hoje a raiz ainda contém o código da **versão original** (`pom.xml`, `mvnw`, `.mvn/`, `src/`: Spring Boot 2.2.6 com banco H2). Ele fica preservado pela tag `v0-original` e será removido da raiz depois do baseline de comportamento. O protótipo de design está em `.specs/design/prototype/`.

## Como subir com Compose

> 🚧 Em construção: o `compose.yaml` já existe, mas as imagens da API e do frontend dependem de projetos e Dockerfiles que ainda não foram criados. Por enquanto só o banco pode ser iniciado.

```bash
cp .env.example .env              # opcional; todos os valores têm padrão de desenvolvimento
docker compose up -d postgres     # somente o PostgreSQL 16 (disponível hoje)
docker compose up -d --build      # ambiente completo: banco + API + front (em construção)
docker compose down               # parar (acrescente -v para apagar o volume do banco)
```

Quando completo, o frontend ficará em <http://localhost:4200> e a API em <http://localhost:8080>. Os testes de integração do backend usarão Testcontainers e não dependem do Compose. O arquivo `.env` não é versionado; só o `.env.example`.

## Versão original

A versão original da API (tag `v0-original`) oferece:

1. Cadastrar novos procedimentos.
2. Consultar procedimentos cadastrados.
3. Autorizar a realização de procedimentos com critérios de validação por idade e sexo do paciente.
4. Consultar procedimentos autorizados.

Para executá-la, use o código da raiz (`./mvnw spring-boot:run`) e acesse `http://localhost:8080/swagger-ui.html`. Com `spring.profiles.active=test`, o console do H2 fica em `http://localhost:8080/h2-console`.

## Especificações

- [Índice](.specs/INDEX.md)
- [Produto](.specs/memory/produto.md) e [contexto técnico](.specs/memory/contexto-tecnico.md)
- [Estrutura do monorepo](.specs/01-architecture/MONOREPO_STRUCTURE.md)
- [Como executar uma spec](.specs/shared/como-executar.md)
- [Design system](.specs/design/DESIGN_SYSTEM_DISCOVERY.md) e protótipo em `.specs/design/prototype/`

## Créditos

- **Linkedin**
- www.linkedin.com/in/dowglasmaia
