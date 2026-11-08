import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-base-url.token';

export type QueryParams = Readonly<Record<string, string | number | boolean | undefined>>;

/** Cliente HTTP tipado: concatena `API_BASE_URL` ao caminho do contrato. */
@Injectable({ providedIn: 'root' })
export class ApiClient {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  get<T>(caminho: string, query?: QueryParams): Observable<T> {
    return this.http.get<T>(this.url(caminho), { params: this.params(query) });
  }

  post<T, B = unknown>(caminho: string, corpo: B): Observable<T> {
    return this.http.post<T>(this.url(caminho), corpo);
  }

  private url(caminho: string): string {
    return `${this.baseUrl.replace(/\/+$/, '')}/${caminho.replace(/^\/+/, '')}`;
  }

  private params(query?: QueryParams): HttpParams {
    let params = new HttpParams();
    for (const [chave, valor] of Object.entries(query ?? {})) {
      if (valor !== undefined) params = params.set(chave, String(valor));
    }
    return params;
  }
}
