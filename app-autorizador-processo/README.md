# app-autorizador-processo

Frontend do **Autorizador de Procedimentos Médicos**: Angular 21, TypeScript estrito, Tailwind CSS (via PostCSS), componentes standalone, zoneless, Signals e formulários reativos tipados.

## Pré-requisitos
- Node 20.19 ou superior (desenvolvido com Node 24) e npm.

## Comandos
| Comando | O que faz |
|---|---|
| `npm ci` | Instala as dependências a partir do `package-lock.json` |
| `npm start` | Servidor de desenvolvimento em `http://localhost:4200` |
| `npm run build` | Build de produção em `dist/` |
| `npm test` | Testes unitários (Vitest via `@angular/build:unit-test`), execução única |
| `npm run lint` | `angular-eslint` (exige `OnPush` e `inject()`) |

## Ambientes
- `src/environments/environment.ts` (produção): `apiBaseUrl = '/api/v1'` (mesma origem, atrás de proxy reverso).
- `src/environments/environment.development.ts`: `apiBaseUrl = 'http://localhost:8080/api/v1'`, aplicado por `fileReplacements` na configuração `development`.
- O valor é lido pelo token `API_BASE_URL` (`src/app/core/http/api-base-url.token.ts`). Não há proxy no `ng serve`: o CORS da origem 4200 é liberado pelo backend (perfil `local`).

## Estrutura
```
src/app/
├─ core/        infraestrutura transversal (fronteira HTTP, erros da API)
├─ features/    uma pasta por funcionalidade (rotas, páginas, doc.md)
├─ app.config.ts, app.routes.ts, app.ts
src/environments/
```

## Convenções
- **`doc.md` por módulo:** cada pasta em `features/` mantém um `doc.md` junto ao código, descrevendo rotas, estado e decisões; atualize-o ao mudar o módulo.
- **`OnPush`** é o padrão (schematic no `angular.json` e regra de lint); componentes standalone.
- **Signals** para estado local e derivado; sem biblioteca de estado externa.
- **TypeScript estrito** e `strictTemplates` ativos.
- Textos de UI, mensagens e documentação em português.
- Tipos da API virão **gerados** do contrato OpenAPI do backend; não duplique DTOs à mão.
