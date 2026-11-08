import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Icon } from '../../shared/ui';
import { LoadingService } from './loading.service';

/** Overlay de ações do usuário; bloqueia cliques e anuncia "Processando…". */
@Component({
  selector: 'app-loading-overlay',
  imports: [Icon],
  template: `
    @if (loading.busy()) {
      <div
        role="alert"
        aria-busy="true"
        class="fixed inset-0 z-[60] flex items-center justify-center bg-scrim"
      >
        <div
          class="flex items-center gap-3 rounded-pill bg-surface px-6 py-3 font-semibold text-content shadow-card"
        >
          <app-icon name="circle-notch" class="animate-spin motion-reduce:animate-none" />
          Processando…
        </div>
      </div>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoadingOverlay {
  protected readonly loading = inject(LoadingService);
}
