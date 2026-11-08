import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { SessaoSolicitacoes } from './sessao-solicitacoes';

describe('SessaoSolicitacoes', () => {
  it('conta concedidas e negadas separadamente', () => {
    TestBed.configureTestingModule({ providers: [provideZonelessChangeDetection()] });
    const sessao = TestBed.inject(SessaoSolicitacoes);
    expect([sessao.concedidas(), sessao.negadas()]).toEqual([0, 0]);
    sessao.registrar('autorizado');
    sessao.registrar('autorizado');
    sessao.registrar('negado');
    expect([sessao.concedidas(), sessao.negadas()]).toEqual([2, 1]);
  });
});
