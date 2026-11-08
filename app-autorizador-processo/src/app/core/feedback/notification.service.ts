import { Injectable, signal } from '@angular/core';

export type NotificationTone = 'success' | 'error' | 'info';

export interface Notification {
  readonly id: number;
  readonly message: string;
  readonly tone: NotificationTone;
}

export const NOTIFICATION_DURATION_MS = 3200;

/** Fila de toasts. Cada toast some após 3,2 s; `pause`/`resume` suspendem a contagem (foco/hover). */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly _items = signal<readonly Notification[]>([]);
  readonly items = this._items.asReadonly();

  private proximoId = 0;
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();
  private pausado = false;

  show(message: string, tone: NotificationTone = 'success'): number {
    const id = ++this.proximoId;
    this._items.update((lista) => [...lista, { id, message, tone }]);
    if (!this.pausado) {
      this.agendar(id);
    }
    return id;
  }

  dismiss(id: number): void {
    this.cancelar(id);
    this._items.update((lista) => lista.filter((n) => n.id !== id));
  }

  pause(): void {
    this.pausado = true;
    this.timers.forEach((_, id) => this.cancelar(id));
  }

  resume(): void {
    this.pausado = false;
    this._items().forEach((n) => this.agendar(n.id));
  }

  private agendar(id: number): void {
    this.cancelar(id);
    this.timers.set(
      id,
      setTimeout(() => this.dismiss(id), NOTIFICATION_DURATION_MS),
    );
  }

  private cancelar(id: number): void {
    const timer = this.timers.get(id);
    if (timer !== undefined) {
      clearTimeout(timer);
      this.timers.delete(id);
    }
  }
}
