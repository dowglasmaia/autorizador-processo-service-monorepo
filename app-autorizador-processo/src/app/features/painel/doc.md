# Painel

Tela `/painel` (rota padrão, dentro do `Shell`, protegida por `authGuard`): resumo do catálogo e das autorizações.

## Operações consumidas

| Operação (OpenAPI)  | Uso                                                 | Onde                            |
| ------------------- | --------------------------------------------------- | ------------------------------- |
| `obterResumoPainel` | `GET painel/resumo`; valores calculados no servidor | `data-access/painel.service.ts` |

Tipos de `shared/api` (gerados). `Autorizacao` do contrato do painel é idêntico a `AutorizacaoDto`.

## Peças

- `painel.routes.ts`: rota lazy `''` com título.
- `data-access/painel.service.ts`: signals `resumo`, `carregando`, `erro`; `carregar()` (sem cache).
- `calculos.ts`: `calcularAprovacao` (0/0 → "—", 1/3 → "33%") e `formatarDataExtenso` (`Intl` pt-BR).
- `components/`: `BarraCobertura`, `RoscaSexos` (SVG, `role="img"`), `UltimasAutorizacoes` (usa `descreverRegras` da feature de autorizações).
- `pages/painel-page.ts`: cabeçalho com data, hero e "Nova solicitação" → `/solicitar`.

## Estados e testes

A página carrega o resumo ao entrar (`ngOnInit`), mostra 4 KPIs (Aprovação vinda de `SessaoSolicitacoes`), esqueleto, erro com "Tentar novamente" e vazio por seção. Testes: serviço (`HttpTestingController`), componentes e página.

## Acessibilidade (etapa 24)

Textos vêm do catálogo `core/i18n/mensagens.ts`; a página é coberta por `src/app/a11y.spec.ts` (rótulos, nomes acessíveis, alvos ≥ 44 px). Detalhes e limitações em `shared/ui/doc.md`.
