import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApiClient } from './api-client';
import { API_BASE_URL } from './api-base-url.token';
import type { ProblemDetail } from '../../shared/api';

describe('ApiClient', () => {
  let api: ApiClient;
  let ctrl: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: API_BASE_URL, useValue: 'http://api.test/' },
      ],
    });
    api = TestBed.inject(ApiClient);
    ctrl = TestBed.inject(HttpTestingController);
  });
  afterEach(() => ctrl.verify());

  it('concatena a URL base ao caminho e ignora query indefinida', () => {
    let corpo: ProblemDetail | undefined;
    api
      .get<ProblemDetail>('/procedimentos', { page: 1, q: undefined })
      .subscribe((b) => (corpo = b));
    const req = ctrl.expectOne('http://api.test/procedimentos?page=1');
    expect(req.request.method).toBe('GET');
    req.flush({ code: 'ERRO_INTERNO' });
    expect(corpo?.code).toBe('ERRO_INTERNO');
  });

  it('post envia o corpo para a URL base', () => {
    api.post<{ id: string }, { codigo: string }>('procedimentos', { codigo: 'A' }).subscribe();
    const req = ctrl.expectOne('http://api.test/procedimentos');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ codigo: 'A' });
    req.flush({ id: '1' });
  });
});
