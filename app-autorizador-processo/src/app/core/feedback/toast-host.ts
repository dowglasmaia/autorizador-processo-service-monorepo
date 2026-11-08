import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { NotificationService } from './notification.service';

/** Região viva dos toasts (`role="status"`); pausa a contagem com foco ou ponteiro. */
@Component({
  selector: 'app-toast-host',
  template: `
    <div
      role="status"
      aria-live="polite"
      class="pointer-events-none fixed inset-x-0 bottom-4 z-50 flex flex-col items-center gap-2 px-4"
      (mouseenter)="notifications.pause()"
      (mouseleave)="notifications.resume()"
      (focusin)="notifications.pause()"
      (focusout)="notifications.resume()"
    >
      @for (item of notifications.items(); track item.id) {
        <div
          class="pointer-events-auto rounded-pill px-5 py-3 text-sm font-semibold shadow-card"
          [class]="item.tone === 'error' ? 'bg-danger text-white' : 'bg-sidebar-to text-white'"
        >
          {{ item.message }}
        </div>
      }
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ToastHost {
  protected readonly notifications = inject(NotificationService);
}
