# MONOREPO_STRUCTURE

## Estado atual (2026-11-08)
Raiz contém o projeto original (`pom.xml`, `mvnw`, `.mvn/`, `src/`), `README.md`, `.gitignore`, e **não rastreados**: `.env.example`, `.specs/`, `compose.yaml`, `Sistema Autorizador com Dashboard/` (protótipo).

## Estrutura de destino
```text
autorizador-processo/                      (raiz do repositório)
├─ api-autorizador-processo/               Spring Boot 4 · Java 25 · Maven (gerado por java-spring-scaffold)
│  ├─ pom.xml · mvnw · mvnw.cmd · .mvn/
│  ├─ Dockerfile  (SPEC-10)
│  └─ src/main/{java,resources/{openapi/{modules,common},db/{migration,seed},application*.yml}} · src/test
├─ app-autorizador-processo/               Angular 21 · Tailwind (gerado por angular-frontend-scaffold)
│  ├─ package.json · angular.json · Dockerfile · nginx/default.conf.template  (SPEC-13)
│  └─ src/app/{core,layout,shared,features}
├─ .specs/workflows/ferramentas/parity/                           cases.json + replay.mjs (SPEC-02/31) — executável contra qualquer base URL
├─ compose.yaml · .env.example             integração local (existentes; ajustados em SPEC-10/13)
├─ .github/workflows/                      CI independente por projeto (SPEC-40): backend-ci.yml · frontend-ci.yml · integracao-ci.yml (cada um só dispara por mudança no seu caminho)
├─ README.md                               visão do monorepo, como subir, links
└─ .specs/                                 especificações (este conjunto)
   ├─ INDEX.md · memory/ · shared/ · design/ (+ prototype/) · 00..06
```

## Regras
1. **Nomes preservados:** `api-autorizador-processo` e `app-autorizador-processo`.
2. **Código da versão original não vira pasta.** Fica no histórico git, preservado pela tag `v0-original` (SPEC-01). O novo backend é **gerado limpo** (a skill de scaffold nunca escreve em diretório existente) e o código antigo é removido da raiz depois do baseline (SPEC-03). Nenhum nome com "legado/legacy" no backend.
3. **Protótipo** move para `.specs/design/prototype/` (insumo de design, não é parte do build).
4. **Sem build agregador** na raiz (Maven reactor/Nx): dois projetos independentes, integração por Compose e CI. Revisitar só se surgir código compartilhado.
5. `.gitignore` raiz cobre `target/`, `node_modules/`, `dist/`, `.angular/`, `.env`, IDEs.
6. **Cada projeto tem seu Dockerfile e seu CI** (instrução do responsável): `api-autorizador-processo/Dockerfile` + `backend-ci.yml`; `app-autorizador-processo/Dockerfile` + `frontend-ci.yml`. O CI de um projeto **não roda** quando só o outro mudou. A verificação combinada (Compose + e2e + contrato) é um terceiro fluxo, `integracao-ci.yml`.
7. Contrato compartilhado: o OpenAPI do backend é a fonte; o frontend consome **tipos gerados**, sem duplicar DTOs (SPEC-16/32).
