import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import type { AutorizacaoDto } from '../../../shared/api';
import { BarraCobertura } from './barra-cobertura';
import { RoscaSexos } from './rosca-sexos';
import { UltimasAutorizacoes } from './ultimas-autorizacoes';

const aut = (id: number): AutorizacaoDto =>
  ({
    id,
    procedimento: {
      id: 1,
      numProcedimento: 4567,
      sexos: ['M', 'F'],
      faixasEtarias: ['ADOLESCENTE', 'ADULTO'],
      criadoEm: '2026-01-01T00:00:00Z',
    },
    permitido: true,
    criadaEm: '2026-01-02T10:00:00Z',
  }) as AutorizacaoDto;

describe('componentes do painel', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [provideZonelessChangeDetection(), provideRouter([])],
    }),
  );

  it('BarraCobertura: larguras proporcionais ao maior valor e tabela textual', async () => {
    const f = TestBed.createComponent(BarraCobertura);
    f.componentRef.setInput('dados', [
      { faixa: 'CRIANCA', quantidade: 2 },
      { faixa: 'ADOLESCENTE', quantidade: 2 },
      { faixa: 'ADULTO', quantidade: 1 },
    ]);
    await f.whenStable();
    const el = f.nativeElement as HTMLElement;
    const barras = Array.from(el.querySelectorAll<HTMLElement>('[data-testid="barra"]'));
    expect(barras.map((b) => b.style.width)).toEqual(['100%', '100%', '50%']);
    expect(el.querySelectorAll('table tbody tr').length).toBe(3);
    expect(el.textContent).toContain('Adulto');
  });

  it('RoscaSexos: M=2 F=3, total 5 e role=img', async () => {
    const f = TestBed.createComponent(RoscaSexos);
    f.componentRef.setInput('dados', { masculino: 2, feminino: 3 });
    await f.whenStable();
    const el = f.nativeElement as HTMLElement;
    expect(el.querySelector('svg')?.getAttribute('role')).toBe('img');
    expect(el.querySelector('svg')?.getAttribute('aria-label')).toContain(
      '2 masculino e 3 feminino',
    );
    expect(el.textContent).toContain('Total: 5');
    expect(el.querySelector('[data-testid="arco-masculino"]')).not.toBeNull();
  });

  it('RoscaSexos: total zero não desenha arcos', async () => {
    const f = TestBed.createComponent(RoscaSexos);
    f.componentRef.setInput('dados', { masculino: 0, feminino: 0 });
    await f.whenStable();
    expect(
      (f.nativeElement as HTMLElement).querySelector('[data-testid="arco-masculino"]'),
    ).toBeNull();
  });

  it('UltimasAutorizacoes: vazio mostra a mensagem', async () => {
    const f = TestBed.createComponent(UltimasAutorizacoes);
    f.componentRef.setInput('autorizacoes', []);
    await f.whenStable();
    expect((f.nativeElement as HTMLElement).textContent).toContain(
      'Nenhuma autorização concedida ainda.',
    );
  });

  it('UltimasAutorizacoes: limita a 4 itens e mostra o link', async () => {
    const f = TestBed.createComponent(UltimasAutorizacoes);
    f.componentRef.setInput('autorizacoes', [1, 2, 3, 4, 5].map(aut));
    await f.whenStable();
    const el = f.nativeElement as HTMLElement;
    expect(el.querySelectorAll('li').length).toBe(4);
    expect(el.textContent).toContain('Ver todas →');
  });
});
