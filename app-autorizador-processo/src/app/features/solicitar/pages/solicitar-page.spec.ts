import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { ErrorDialogService } from '../../../core/feedback/error-dialog';
import { errorInterceptor } from '../../../core/http/error.interceptor';
import { SessaoSolicitacoes } from '../../../core/sessao/sessao-solicitacoes';
import { SolicitarPage } from './solicitar-page';

const BASE = environment.apiBaseUrl.replace(/\/+$/, '');

const proc = (numProcedimento: number, nome?: string) => ({
  id: numProcedimento,
  numProcedimento,
  ...(nome ? { nome } : {}),
  sexos: ['F', 'M'],
  faixasEtarias: ['ADOLESCENTE'],
  criadoEm: '2026-01-01T00:00:00Z',
});

const pagina = (itens: unknown[]) => ({
  itens,
  pagina: 0,
  tamanho: 200,
  totalItens: itens.length,
  totalPaginas: 1,
});

describe('SolicitarPage', () => {
  async function montar(numero?: string) {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: { get: (k: string) => (k === 'numero' ? (numero ?? null) : null) },
            },
          },
        },
      ],
    });
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(SolicitarPage);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const carregarLista = async (itens: unknown[] = [proc(4567, 'Exame A'), proc(1234)]) => {
      http.expectOne((r) => r.url === `${BASE}/procedimentos`).flush(pagina(itens));
      await fixture.whenStable();
      fixture.detectChanges();
    };
    const botao = (texto: string): HTMLButtonElement =>
      Array.from(el.querySelectorAll('button')).find((b) =>
        b.textContent?.trim().includes(texto),
      ) as HTMLButtonElement;
    const preencher = async (numero: string, idade: string, sexo: 'M' | 'F') => {
      const sel = el.querySelector('select') as HTMLSelectElement;
      sel.value =
        sel.options[
          Array.from(sel.options).findIndex((o) => o.textContent?.includes(`Nº ${numero}`))
        ].value;
      sel.dispatchEvent(new Event('change'));
      const input = el.querySelector('input[type=number]') as HTMLInputElement;
      input.value = idade;
      input.dispatchEvent(new Event('input'));
      const alvo = sexo === 'M' ? 'Masculino' : 'Feminino';
      (
        Array.from(el.querySelectorAll('app-segmented-choice button')).find((b) =>
          b.textContent?.includes(alvo),
        ) as HTMLButtonElement
      ).click();
      await fixture.whenStable();
      fixture.detectChanges();
    };
    const enviar = async () => {
      botao('Solicitar autorização').click();
      await fixture.whenStable();
    };
    const resultado = (): string | null =>
      el.querySelector('[data-resultado]')?.getAttribute('data-resultado') ?? null;
    return { fixture, el, http, carregarLista, botao, preencher, enviar, resultado };
  }

  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('lista procedimentos como "Nº n" com nome quando existir', async () => {
    const t = await montar();
    await t.carregarLista();
    const textos = Array.from(t.el.querySelectorAll('option')).map((o) => o.textContent?.trim());
    expect(textos).toContain('Nº 4567 · Exame A');
    expect(textos).toContain('Nº 1234');
    expect(t.resultado()).toBe('inicial');
  });

  it('autoriza, mostra o cartão e atualiza o contador', async () => {
    const t = await montar();
    await t.carregarLista();
    await t.preencher('4567', '20', 'F');
    await t.enviar();
    const req = t.http.expectOne(`${BASE}/autorizacoes`);
    expect(req.request.body).toEqual({ numProcedimento: 4567, idade: 20, sexo: 'F' });
    req.flush(
      { permitido: true, procedimento: 4567, criadaEm: '2026-01-01T00:00:00Z' },
      { status: 201, statusText: 'Created' },
    );
    await t.fixture.whenStable();
    t.fixture.detectChanges();
    expect(t.resultado()).toBe('autorizado');
    expect(t.el.textContent).toContain(
      'Procedimento 4567 liberado para paciente feminino de 20 anos',
    );
    expect(t.el.textContent).toContain('Ver autorizações concedidas');
    expect(TestBed.inject(SessaoSolicitacoes).concedidas()).toBe(1);
  });

  it('mostra negado sem toast e conta a negativa', async () => {
    const t = await montar();
    await t.carregarLista();
    await t.preencher('1234', '30', 'M');
    await t.enviar();
    t.http
      .expectOne(`${BASE}/autorizacoes`)
      .flush(
        { code: 'AUTORIZACAO_NEGADA', title: 'Negada', detail: 'Solicitação negada.', status: 403 },
        { status: 403, statusText: 'Forbidden' },
      );
    await t.fixture.whenStable();
    t.fixture.detectChanges();
    expect(t.resultado()).toBe('negado');
    expect(t.el.textContent).toContain('A regra não informa qual critério falhou');
    expect(TestBed.inject(SessaoSolicitacoes).negadas()).toBe(1);
  });

  it('trata procedimento inexistente: erro no seletor e recarga da lista', async () => {
    const t = await montar();
    await t.carregarLista();
    await t.preencher('4567', '20', 'F');
    await t.enviar();
    t.http
      .expectOne(`${BASE}/autorizacoes`)
      .flush(
        {
          code: 'PROCEDIMENTO_NAO_ENCONTRADO',
          title: 'Não encontrado',
          detail: 'Procedimento não encontrado.',
          status: 404,
        },
        { status: 404, statusText: 'Not Found' },
      );
    await t.fixture.whenStable();
    t.fixture.detectChanges();
    expect(t.el.textContent).toContain('Procedimento não encontrado.');
    await t.carregarLista([proc(1234)]);
    const textos = Array.from(t.el.querySelectorAll('option')).map((o) => o.textContent?.trim());
    expect(textos).not.toContain('Nº 4567 · Exame A');
  });

  it('mapeia VALIDACAO_INVALIDA aos campos', async () => {
    const t = await montar();
    await t.carregarLista();
    await t.preencher('4567', '20', 'F');
    await t.enviar();
    t.http.expectOne(`${BASE}/autorizacoes`).flush(
      {
        code: 'VALIDACAO_INVALIDA',
        title: 'Inválido',
        detail: 'Dados inválidos.',
        status: 400,
        errors: [{ campo: 'idade', mensagem: 'Idade fora do limite.' }],
      },
      { status: 400, statusText: 'Bad Request' },
    );
    await t.fixture.whenStable();
    t.fixture.detectChanges();
    expect(t.el.textContent).toContain('Idade fora do limite.');
  });

  it('abre o diálogo "Tentar novamente" em falha de rede e preserva o formulário', async () => {
    const t = await montar();
    await t.carregarLista();
    await t.preencher('4567', '20', 'F');
    await t.enviar();
    t.http.expectOne(`${BASE}/autorizacoes`).error(new ProgressEvent('error'));
    await t.fixture.whenStable();
    const dialogo = TestBed.inject(ErrorDialogService);
    expect(dialogo.state()).not.toBeNull();
    expect((t.el.querySelector('input[type=number]') as HTMLInputElement).value).toBe('20');
    dialogo.retry();
    await t.fixture.whenStable();
    t.http
      .expectOne(`${BASE}/autorizacoes`)
      .flush(
        { permitido: true, procedimento: 4567, criadaEm: '2026-01-01T00:00:00Z' },
        { status: 201, statusText: 'Created' },
      );
    await t.fixture.whenStable();
    t.fixture.detectChanges();
    expect(t.resultado()).toBe('autorizado');
  });

  it('pré-seleciona o procedimento por ?numero=', async () => {
    const t = await montar('4567');
    await t.carregarLista();
    const sel = t.el.querySelector('select') as HTMLSelectElement;
    expect(sel.selectedOptions[0]?.textContent).toContain('Nº 4567');
  });

  it('Limpar restaura formulário e resultado', async () => {
    const t = await montar();
    await t.carregarLista();
    await t.preencher('4567', '20', 'F');
    await t.enviar();
    t.http
      .expectOne(`${BASE}/autorizacoes`)
      .flush(
        { permitido: true, procedimento: 4567, criadaEm: '2026-01-01T00:00:00Z' },
        { status: 201, statusText: 'Created' },
      );
    await t.fixture.whenStable();
    t.fixture.detectChanges();
    t.botao('Limpar').click();
    await t.fixture.whenStable();
    t.fixture.detectChanges();
    expect(t.resultado()).toBe('inicial');
    expect((t.el.querySelector('input[type=number]') as HTMLInputElement).value).toBe('');
  });
});
