import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApiStatusService, HEALTH_PATH } from './api-status/api-status.service';
import { ErrorDialogService } from './feedback/error-dialog';
import { LoadingService } from './feedback/loading.service';
import { NOTIFICATION_DURATION_MS, NotificationService } from './feedback/notification.service';

describe('core', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
  });
  afterEach(() => vi.useRealTimers());

  describe('NotificationService', () => {
    it('remove o toast após 3,2 s', () => {
      const s = TestBed.inject(NotificationService);
      s.show('Salvo');
      expect(s.items().length).toBe(1);
      vi.advanceTimersByTime(NOTIFICATION_DURATION_MS - 1);
      expect(s.items().length).toBe(1);
      vi.advanceTimersByTime(1);
      expect(s.items().length).toBe(0);
    });

    it('pausa a contagem e retoma', () => {
      const s = TestBed.inject(NotificationService);
      s.show('Salvo');
      s.pause();
      vi.advanceTimersByTime(NOTIFICATION_DURATION_MS * 2);
      expect(s.items().length).toBe(1);
      s.resume();
      vi.advanceTimersByTime(NOTIFICATION_DURATION_MS);
      expect(s.items().length).toBe(0);
    });
  });

  describe('LoadingService', () => {
    it('mantém busy durante a ação e libera ao fim, mesmo com erro', async () => {
      const s = TestBed.inject(LoadingService);
      let resolve!: () => void;
      const p = s.run(() => new Promise<void>((r) => (resolve = r)));
      expect(s.busy()).toBe(true);
      resolve();
      await p;
      expect(s.busy()).toBe(false);
      await expect(s.run(() => Promise.reject(new Error('x')))).rejects.toThrow('x');
      expect(s.busy()).toBe(false);
    });
  });

  describe('ErrorDialogService', () => {
    it('retry fecha e executa a ação', () => {
      const s = TestBed.inject(ErrorDialogService);
      const acao = vi.fn();
      s.show('Falhou', acao);
      expect(s.state()?.message).toBe('Falhou');
      s.retry();
      expect(s.state()).toBeNull();
      expect(acao).toHaveBeenCalledOnce();
    });
  });

  describe('ApiStatusService', () => {
    it('UP → disponível; falha → indisponível', () => {
      const s = TestBed.inject(ApiStatusService);
      const http = TestBed.inject(HttpTestingController);
      expect(s.status()).toBe('verificando');
      s.check();
      http.expectOne(HEALTH_PATH).flush({ status: 'UP' });
      expect(s.status()).toBe('disponivel');
      s.check();
      http.expectOne(HEALTH_PATH).flush('erro', { status: 503, statusText: 'Unavailable' });
      expect(s.status()).toBe('indisponivel');
    });
  });
});
