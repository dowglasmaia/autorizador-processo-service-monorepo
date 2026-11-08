import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  afterRenderEffect,
  effect,
  input,
  model,
  output,
  untracked,
  viewChild,
} from '@angular/core';

let proximoId = 0;

const FOCAVEIS =
  'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), ' +
  'textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

/**
 * Diálogo modal acessível: `role="dialog"`/`aria-modal`, foco preso, Esc fecha e o foco volta
 * ao elemento que o abriu. Uso: `<app-dialog [(open)]="aberto" title="Confirmar">…</app-dialog>`.
 */
@Component({
  selector: 'app-dialog',
  template: `
    @if (open()) {
      <div class="fixed inset-0 z-50 flex items-center justify-center bg-scrim p-4">
        <div
          #painel
          role="dialog"
          aria-modal="true"
          tabindex="-1"
          [attr.aria-labelledby]="titleId"
          class="max-h-full w-full max-w-lg overflow-y-auto rounded-dialog bg-surface p-6 shadow-card"
          (keydown)="onKeydown($event)"
        >
          <h2 [id]="titleId" class="text-xl font-bold text-content">{{ title() }}</h2>
          <div class="mt-3 text-content"><ng-content /></div>
          <div class="mt-6 flex flex-wrap justify-end gap-3"><ng-content select="[dialog-actions]" /></div>
        </div>
      </div>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Dialog {
  readonly open = model(false);
  readonly title = input.required<string>();
  readonly closed = output<void>();

  protected readonly titleId = `app-dialog-${++proximoId}-title`;
  private readonly painel = viewChild<ElementRef<HTMLElement>>('painel');
  private anterior: HTMLElement | null = null;

  constructor() {
    effect(() => {
      if (this.open()) {
        untracked(() => (this.anterior = document.activeElement as HTMLElement | null));
      }
    });
    afterRenderEffect(() => {
      const painel = this.painel()?.nativeElement;
      if (painel) {
        const primeiro = painel.querySelector<HTMLElement>(FOCAVEIS);
        (primeiro ?? painel).focus();
      } else if (this.anterior) {
        this.anterior.focus();
        this.anterior = null;
      }
    });
  }

  close(): void {
    this.open.set(false);
    this.closed.emit();
  }

  protected onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      event.preventDefault();
      this.close();
      return;
    }
    if (event.key !== 'Tab') {
      return;
    }
    const painel = this.painel()?.nativeElement;
    if (!painel) {
      return;
    }
    const itens = Array.from(painel.querySelectorAll<HTMLElement>(FOCAVEIS));
    if (itens.length === 0) {
      event.preventDefault();
      painel.focus();
      return;
    }
    const primeiro = itens[0];
    const ultimo = itens[itens.length - 1];
    const ativo = document.activeElement;
    if (event.shiftKey && (ativo === primeiro || ativo === painel)) {
      event.preventDefault();
      ultimo.focus();
    } else if (!event.shiftKey && ativo === ultimo) {
      event.preventDefault();
      primeiro.focus();
    }
  }
}
