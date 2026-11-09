import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import {
  NavigationCancel,
  NavigationEnd,
  NavigationError,
  NavigationStart,
  Router,
} from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

/**
 * Barra fina no topo durante a navegação entre telas (carregamento de rota lazy).
 * Aparece só após 150ms para não piscar em navegações rápidas; anima apenas transform.
 */
@Component({
  selector: 'app-barra-navegacao',
  template: `
    @if (visivel()) {
      <div
        role="progressbar"
        aria-label="Carregando tela"
        class="pointer-events-none fixed inset-x-0 top-0 z-[70] h-0.5 overflow-hidden"
      >
        <div class="h-full w-full animate-barra-rota bg-accent-700 motion-reduce:animate-none"></div>
      </div>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BarraNavegacao {
  protected readonly visivel = signal(false);
  private timer: ReturnType<typeof setTimeout> | undefined;

  constructor() {
    inject(Router)
      .events.pipe(takeUntilDestroyed())
      .subscribe((e) => {
        if (e instanceof NavigationStart) {
          clearTimeout(this.timer);
          this.timer = setTimeout(() => this.visivel.set(true), 150);
        } else if (
          e instanceof NavigationEnd ||
          e instanceof NavigationCancel ||
          e instanceof NavigationError
        ) {
          clearTimeout(this.timer);
          this.visivel.set(false);
        }
      });
  }
}
