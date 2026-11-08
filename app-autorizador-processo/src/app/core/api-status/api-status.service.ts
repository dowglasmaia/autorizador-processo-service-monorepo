import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';

export type ApiStatus = 'verificando' | 'disponivel' | 'indisponivel';

export const HEALTH_PATH = '/actuator/health';

/** Consulta `GET /actuator/health`; qualquer falha ou status diferente de UP vira "indisponível". */
@Injectable({ providedIn: 'root' })
export class ApiStatusService {
  private readonly http = inject(HttpClient);
  private readonly _status = signal<ApiStatus>('verificando');
  readonly status = this._status.asReadonly();

  check(): void {
    this._status.set('verificando');
    this.http.get<{ status?: string }>(HEALTH_PATH).subscribe({
      next: (body) => this._status.set(body?.status === 'UP' ? 'disponivel' : 'indisponivel'),
      error: () => this._status.set('indisponivel'),
    });
  }
}
