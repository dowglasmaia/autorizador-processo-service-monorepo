import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ApiStatusService } from '../../core/api-status/api-status.service';
import { ErrorDialog } from '../../core/feedback/error-dialog';
import { LoadingOverlay } from '../../core/feedback/loading-overlay';
import { ToastHost } from '../../core/feedback/toast-host';
import { Icon } from '../../shared/ui';

interface NavItem {
  readonly path: string;
  readonly label: string;
  readonly icon: string;
}

export const NAV_ITEMS: readonly NavItem[] = [
  { path: '/painel', label: 'Painel', icon: 'squares-four' },
  { path: '/procedimentos', label: 'Procedimentos', icon: 'clipboard-text' },
  { path: '/solicitar', label: 'Solicitar', icon: 'plus-circle' },
  { path: '/autorizacoes', label: 'Autorizações', icon: 'seal-check' },
];

/**
 * Casca da aplicação: sidebar fixa a partir de `lg`; abaixo disso, barra superior com
 * botão que abre a gaveta de navegação. O conteúdo vai em `<main id="conteudo">`.
 */
@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Icon, ToastHost, LoadingOverlay, ErrorDialog],
  template: `
    <a
      href="#conteudo"
      class="sr-only focus:not-sr-only focus:fixed focus:left-2 focus:top-2 focus:z-[70] focus:rounded-pill focus:bg-surface focus:px-4 focus:py-2"
    >
      Ir para o conteúdo
    </a>

    <header class="sticky top-0 z-30 flex items-center gap-3 border-b border-divider bg-surface px-4 py-2 lg:hidden">
      <button
        type="button"
        class="inline-flex size-11 items-center justify-center rounded-pill hover:bg-neutral-100"
        aria-label="Abrir menu"
        aria-controls="navegacao"
        [attr.aria-expanded]="drawerOpen()"
        (click)="drawerOpen.set(true)"
      >
        <app-icon name="list" />
      </button>
      <span class="font-bold text-content">Autorizador</span>
    </header>

    @if (drawerOpen()) {
      <div class="fixed inset-0 z-40 bg-scrim lg:hidden" (click)="drawerOpen.set(false)" aria-hidden="true"></div>
    }

    <aside
      id="navegacao"
      class="fixed inset-y-0 left-0 z-50 flex w-64 flex-col bg-linear-to-b from-sidebar-from to-sidebar-to p-4 text-sidebar-content transition-transform lg:translate-x-0"
      [class]="drawerOpen() ? 'translate-x-0' : '-translate-x-full'"
    >
      <div class="mb-6 flex items-center justify-between px-2">
        <span class="text-lg font-extrabold text-white">Autorizador</span>
        <button
          type="button"
          class="inline-flex size-11 items-center justify-center rounded-pill hover:bg-white/10 lg:hidden"
          aria-label="Fechar menu"
          (click)="drawerOpen.set(false)"
        >
          <app-icon name="x" />
        </button>
      </div>

      <nav aria-label="Principal" class="flex flex-1 flex-col gap-1">
        @for (item of items; track item.path) {
          <a
            [routerLink]="item.path"
            routerLinkActive="bg-white/15 text-white"
            ariaCurrentWhenActive="page"
            class="flex min-h-11 items-center gap-3 rounded-input px-3 py-2 font-semibold hover:bg-white/10 focus-visible:outline-white"
            (click)="drawerOpen.set(false)"
          >
            <app-icon [name]="item.icon" />
            {{ item.label }}
          </a>
        }
      </nav>

      <p class="px-2 text-sm" role="status">
        API:
        @switch (apiStatus.status()) {
          @case ('disponivel') {
            <span>disponível</span>
          }
          @case ('indisponivel') {
            <span>indisponível</span>
          }
          @default {
            <span>verificando…</span>
          }
        }
      </p>
    </aside>

    <main id="conteudo" tabindex="-1" class="min-h-dvh p-4 outline-none sm:p-6 lg:ml-64 lg:p-8">
      <router-outlet />
    </main>

    <app-toast-host />
    <app-loading-overlay />
    <app-error-dialog />
  `,
  host: { '(document:keydown.escape)': 'drawerOpen.set(false)' },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Shell {
  protected readonly items = NAV_ITEMS;
  protected readonly apiStatus = inject(ApiStatusService);
  protected readonly drawerOpen = signal(false);

  constructor() {
    this.apiStatus.check();
    inject(Router)
      .events.pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.drawerOpen.set(false));
  }
}
