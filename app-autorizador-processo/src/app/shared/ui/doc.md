# Design system — tokens e componentes base

Fonte: valores efetivos do protótipo `.specs/design/prototype/Autorizador.dc.html` (prevalecem sobre o Broadsheet, ADR-09). Tokens definidos em `src/styles.scss` (`@theme` do Tailwind 4); **templates usam só utilitários semânticos, nunca cores literais**.

## Tokens
| Token (utilitário) | Valor | Uso |
|---|---|---|
| `canvas` | `#f4fafa` | fundo da página |
| `surface` | `#ffffff` | cartões, campos, diálogos |
| `content` / `content-muted` | `#0b2a2f` / `#4a6666` | texto principal / secundário |
| `divider` | `rgba(7,70,66,.14)` | bordas e separadores |
| `accent` (+100…900) | `#0fa88c` | ação primária, destaques |
| `accent-2` (+100…800) | `#e5604a` | rampa decorativa/alerta |
| `danger` / `danger-soft` | `#aa3a2a` / `#fff1ee` | erro, negação (accent-2-700) |
| `neutral-100…900` | `#f1f7f7` … `#14292d` | superfícies e textos auxiliares |
| `sidebar-from`/`sidebar-to`/`sidebar-content` | `#03242b` → `#021a20` / `#c6f0e5` | barra lateral |
| `scrim` | `rgba(2,26,32,.55)` | fundo de diálogo/gaveta |
| `rounded-card` / `-input` / `-dialog` / `-pill` | 20 / 10 / 22 / 999 px | raios |
| `shadow-card` | — | elevação de cartão |
| `font-sans` | Manrope 400–800 | fonte (via `@fontsource/manrope`, sem CDN) |

Ícones: Phosphor duotone (`@phosphor-icons/web`, sem CDN) pelo componente `<app-icon name="house" />`; com `label` vira `role="img"`, sem `label` fica `aria-hidden`.

## Contraste (WCAG AA — mínimo 4,5:1 texto normal, 3:1 texto grande/ícones)
| Par | Razão | Resultado / decisão |
|---|---|---|
| `content` sobre `canvas` / `surface` | 14,35 / 15,15 | OK |
| `content-muted` sobre `surface` | 6,21 | OK |
| `content` sobre `accent` | 5,05 | OK — **botão primário usa texto escuro** |
| branco sobre `accent` | 3,00 | **reprovado** para texto normal; não usar |
| branco sobre `accent-700` / `accent-800` | 5,25 / 7,69 | OK |
| `accent-700` sobre `surface` / `accent-100` | 5,25 / 4,75 | OK (texto de links, ghost, kicker) |
| `accent-800` sobre `accent-100` | > 7 | OK (Tag accent) |
| branco sobre `accent-2` (`#e5604a`) | 3,44 | **reprovado**; por isso erro/negação usam `danger` |
| `danger` sobre `surface` / `danger-soft` | 6,27 / 5,70 | OK |
| `neutral-800` sobre `neutral-200` | > 7 | OK (Tag neutral) |
| `neutral-600` sobre `surface` | 4,25 | **reprovado** para texto; só decorativo — texto auxiliar usa `content-muted` |
| `sidebar-content` / branco sobre `sidebar-to` | 14,51 / 16,27 | OK |

## Componentes (`shared/ui`, importar de `shared/ui/index.ts`)
| Componente | Uso |
|---|---|
| `Button` | `<button app-button variant="primary｜secondary｜ghost｜icon" [loading]>`; alvo mínimo 44 px; `icon` exige `aria-label` |
| `Tag` | `<app-tag tone="accent｜accent-2｜neutral">` |
| `Card` | contêiner com borda, raio 20 e sombra |
| `KpiCard` | `label`, `value`, `hint`, `icon`, `loading` (mostra `Skeleton`) |
| `PageHeader` | `kicker`, `title`, `description`; ação projetada em `[actions]` |
| `EmptyState` | `title`, `description`, `icon`; ação como conteúdo |
| `Skeleton` | dimensionar por classes; respeita `prefers-reduced-motion` |

| `Field` + `appFieldControl` | `<app-field label help error required><input appFieldControl formControlName="x"/></app-field>`; liga `id`, `aria-describedby`, `aria-invalid` |
| `Chip` / `SegmentedChoice` | `<button app-chip [(pressed)]>`; `<app-segmented-choice [options] [(value)]>`; ambos com `aria-pressed` |
| `DataTable` | `columns`, `rows`, `caption`; tabela ≥ `md`, cartões rotulados (`dl`) abaixo; célula custom com `<ng-template appCell let-row let-column="column">` |
| `Dialog` | `<app-dialog [(open)] title>`; ações em `[dialog-actions]`; foco preso, Esc fecha, foco volta ao gatilho |

## Núcleo e shell (lote 2)
- `core/feedback`: `NotificationService` (toast `role="status"`, 3,2 s, pausa com foco/hover), `LoadingService` (`run()` → overlay só para ações do usuário; páginas usam `Skeleton`), `ErrorDialogService` + `ErrorDialog` ("Não foi possível carregar", Fechar/Tentar novamente).
- `core/api-status`: `ApiStatusService.check()` consulta `GET /actuator/health`; falha ou status ≠ UP → "indisponível".
- `layout/shell`: sidebar fixa ≥ `lg`; abaixo, barra superior + gaveta (Esc fecha); `<main id="conteudo">`, item ativo com `aria-current="page"`, link "Ir para o conteúdo". Ligação às rotas na tarefa 4.5.
