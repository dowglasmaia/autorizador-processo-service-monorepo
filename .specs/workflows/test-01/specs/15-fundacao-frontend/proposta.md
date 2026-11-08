# Fundação do frontend

Etapa 15 de 31 · Fase 3 · derivada da `SPEC-14` (`.specs/03-frontend/SPEC-14-angular-foundation.md`). Proposta antecipada na etapa 14 e revisada na etapa 15 contra o estado atual: a etapa 14 (SPEC-50) foi arquivada sem tocar no frontend, `app-autorizador-processo/` continua inexistente e Node é v24.14.0.

## Objetivo
Gerar o projeto `app-autorizador-processo` (Angular 21, TypeScript estrito, SCSS, Tailwind via PostCSS, standalone, zoneless, porta 4200, shell mínimo) com o gerador oficial da skill `angular-frontend-scaffold`, e acrescentar o que o gerador deliberadamente não traz: executor de testes com um teste do shell, script `lint`, `environment` com `apiBaseUrl`, `README.md` e regra `OnPush`/estrito documentada e verificada.

## Ponto de partida (conferido no repositório)
- `app-autorizador-processo/` **não existe** (raiz tem só `api-autorizador-processo/`, `tools/`, `compose.yaml`, `README.md`); logo o scaffold pode rodar sem conflito.
- Node v24.14.0 (≥ 20.19 exigido). A skill está em `~/.claude/skills/angular-frontend-scaffold` (`scripts/scaffold.mjs` e `validate.mjs`); ela fixa CLI `@angular/cli@21`, `--skip-tests`, zoneless, sem SSR, `npm start` na 4200, e inclui `doc.md` de feature e fronteira HTTP tipada.
- `.env.example` já tem `APP_PORT=4200` e `CORS_ALLOWED_ORIGINS=http://localhost:4200`; `compose.yaml` tem o serviço `app` reservado para a etapa de Nginx (SPEC-13 do front), que **não** entra aqui.
- Backend responde em `http://localhost:8080/api/v1` (perfil `local` libera a origem 4200), por isso não há proxy no `ng serve`.
- Pasta `06-execution-plan/` citada no `INDEX.md` não existe; desvio a registrar na evidência.
- A skill proíbe `npm test`/`ng serve` dentro do fluxo do gerador, mas a SPEC-14 T4 exige testes: eles são adicionados **depois** do scaffold e validação, como passo próprio.

## Escopo
1. Confirmar pré-requisitos e gerar o projeto com `scaffold.mjs --output <raiz> --project-name app-autorizador-processo`; validar com `validate.mjs` (build de produção).
2. Adicionar executor de testes padrão do CLI 21 (verificar na documentação do CLI qual é suportado) com um teste mínimo do shell; scripts `test` (execução única, sem watch) e `lint` no `package.json`.
3. `src/environments/environment.ts` (`apiBaseUrl: '/api/v1'`) e `environment.development.ts` (`http://localhost:8080/api/v1`), com `fileReplacements` na configuração de desenvolvimento; a fronteira HTTP do shell passa a ler o valor.
4. `README.md` do frontend: comandos, estrutura, convenções (`doc.md` por módulo, `OnPush`, Signals).
5. Garantir `strict`, `strictTemplates` e `OnPush` como padrão (schematic `changeDetection: OnPush` no `angular.json` e regra de lint), documentado.
6. Adicionar `app-autorizador-processo/` ao `.gitignore`/`README.md` do monorepo apenas se faltar (`node_modules`, `dist`).

## Fora do escopo (fica para outras etapas)
- Dockerfile/Nginx e serviço `app` do Compose (SPEC-13 do front); CI do frontend (SPEC-40).
- Design system/tokens do protótipo (SPEC-15 do front), tipos gerados do OpenAPI e camada HTTP real (SPEC-16), telas e login (SPEC-17 em diante).
- SSR, biblioteca de estado, UI kit, proxy no `ng serve`, qualquer chamada à API.

## Critérios de aceitação
- *Given* checkout limpo · *When* `npm ci && npm run lint && npm run build && npm test` em `app-autorizador-processo/` · *Then* tudo passa, incluindo o teste do shell.
- `validate.mjs` retorna sucesso; `npm start` serve na porta 4200 (verificado e o processo encerrado em seguida).
- `tsconfig.json` com `strict` e `angularCompilerOptions.strictTemplates` ativos; `OnPush` como padrão.
- Nenhuma dependência além do previsto (Angular, Tailwind/PostCSS, executor de testes e lint); sem proxy, SSR ou biblioteca de estado.
- Backend inalterado; `./mvnw verify` não precisa ser reexecutado salvo mudança fora do frontend.

## Decisões tomadas sem consulta
- **Executor de testes:** usar o padrão do CLI 21 (Vitest via `@angular/build:unit-test`), por ser o suportado pelo CLI; confirmar na documentação na hora e registrar a escolha. [REVISAR]
- **Lint:** adotar `angular-eslint` (o CLI 21 não traz lint por padrão), com regras para `OnPush` e `inject()`; é a opção mais conservadora e coerente com o `npm run lint` exigido pelo loop. [REVISAR]
- **Testes após o scaffold:** a regra da skill de não rodar testes vale para o gerador; os testes da SPEC-14 T4 rodam depois, como etapa separada.
- **Sem proxy:** mantido conforme a SPEC-14; CORS fica a cargo do backend (perfil `local`).
- **Revisão da proposta antecipada:** conferida contra o repositório; escopo e tarefas mantidos. A SPEC-14 indica `angular-architect` para T4–T7; será usada junto com `angular-frontend-scaffold`. O `.gitignore` da raiz já ignora `node_modules/` e `build/`; falta conferir `dist` do frontend (tarefa 4.4).
