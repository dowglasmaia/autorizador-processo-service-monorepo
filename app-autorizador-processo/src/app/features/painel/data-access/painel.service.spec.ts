import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { errorInterceptor } from '../../../core/http/error.interceptor';
import { environment } from '../../../../environments/environment';
import type { ResumoPainel } from '../../../shared/api';
import { PainelService } from './painel.service';

const URL = `${environment.apiBaseUrl.replace(/\/+$/, '')}/painel/resumo`;

const resumo: ResumoPainel = {
  totalProcedimentos: 3,
  totalAutorizacoes: 0,
  faixasCobertas: 3,
  coberturaPorFaixa: [
    { faixa: 'CRIANCA', quantidade: 2 },
    { faixa: 'ADOLESCENTE', quantidade: 2 },
    { faixa: 'ADULTO', quantidade: 1 },
  ],
  distribuicaoSexos: { masculino: 2, feminino: 3 },
  ultimasAutorizacoes: [],
};

describe('PainelService', () => {
  let service: PainelService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(PainelService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('carrega o resumo com sucesso', () => {
    service.carregar();
    expect(service.carregando()).toBe(true);
    http.expectOne(URL).flush(resumo);
    expect(service.resumo()).toEqual(resumo);
    expect(service.carregando()).toBe(false);
    expect(service.erro()).toBeNull();
  });

  it('expõe erro de rede e permite nova tentativa', () => {
    service.carregar();
    http.expectOne(URL).error(new ProgressEvent('error'));
    expect(service.erro()).not.toBeNull();
    expect(service.resumo()).toBeNull();
    expect(service.carregando()).toBe(false);

    service.carregar();
    expect(service.erro()).toBeNull();
    http.expectOne(URL).flush(resumo);
    expect(service.resumo()).toEqual(resumo);
  });
});
