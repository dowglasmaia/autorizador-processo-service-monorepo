import { Injectable, computed, signal } from '@angular/core';

/**
 * Estado de carregamento.
 * - Ações do usuário (salvar, autorizar): `run()` mostra o overlay (`busy`).
 * - Páginas: cada página usa o próprio sinal e mostra `Skeleton`; o overlay não é usado.
 */
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly pendentes = signal(0);
  readonly busy = computed(() => this.pendentes() > 0);

  begin(): void {
    this.pendentes.update((n) => n + 1);
  }

  end(): void {
    this.pendentes.update((n) => Math.max(0, n - 1));
  }

  /** Executa uma ação assíncrona do usuário exibindo o overlay até terminar. */
  async run<T>(action: () => Promise<T>): Promise<T> {
    this.begin();
    try {
      return await action();
    } finally {
      this.end();
    }
  }
}
