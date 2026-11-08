import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { errorInterceptor } from '../../../core/http/error.interceptor';
import { ErrorDialogService } from '../../../core/feedback/error-dialog';
import { NotificationService } from '../../../core/feedback/notification.service';
import { ProcedimentoNovoPage } from './procedimento-novo-page';

const URL = `${environment.apiBaseUrl.replace(/\/+$/, '')}/procedimentos`;

describe('ProcedimentoNovoPage', () => {
  function montar() {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    const http = TestBed.inject(HttpTestingController);
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(ProcedimentoNovoPage);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const botao = (texto: string): HTMLButtonElement =>
      Array.from(el.querySelectorAll('button')).find((b) =>
        b.textContent?.trim().includes(texto),
      ) as HTMLButtonElement;
    const digitar = async (rotulo: string, valor: string): Promise<void> => {
      const campo = Array.from(el.querySelectorAll('app-field')).find((f) =>
        f.querySelector('label')?.textContent?.includes(rotulo),
      );
      const input = campo?.querySelector('input, select') as HTMLInputElement;
      input.value = valor;
      input.dispatchEvent(new Event(input.tagName === 'SELECT' ? 'change' : 'input'));
      await fixture.whenStable();
    };
    return { fixture, el, botao, digitar, http };
  }

  async function ate2(t: ReturnType<typeof montar>): Promise<void> {
    await t.digitar('Número', '9999');
    await t.digitar('Nome', 'Ressonância');
    await t.digitar('Categoria', 'EXAMES_IMAGEM');
    t.botao('Avançar').click();
    await t.fixture.whenStable();
  }

  it('bloqueia o avanço da etapa 1 com campos obrigatórios vazios', async () => {
    const t = montar();
    t.botao('Avançar').click();
    await t.fixture.whenStable();
    expect(t.el.textContent).toContain('Campo obrigatório.');
    expect(t.el.querySelector('[aria-current="step"]')?.textContent).toContain('Identificação');
  });

  it('avança para a cobertura e exige período com quantidade', async () => {
    const t = montar();
    await ate2(t);
    expect(t.el.querySelector('[aria-current="step"]')?.textContent).toContain('Cobertura');
    await t.digitar('Quantidade', '2');
    t.botao('Avançar').click();
    await t.fixture.whenStable();
    expect(t.el.textContent).toContain('Informe o período');
    await t.digitar('Período', 'MES');
    t.botao('Avançar').click();
    await t.fixture.whenStable();
    expect(t.el.querySelector('[aria-current="step"]')?.textContent).toContain('Elegibilidade');
  });

  it('aplica a faixa, remove uma idade e mostra o aviso RN9 sem sexo', async () => {
    const t = montar();
    await ate2(t);
    t.botao('Avançar').click();
    await t.fixture.whenStable();
    expect(t.el.textContent).toContain('nenhuma solicitação será autorizada');

    await t.digitar('Idade inicial', '18');
    await t.digitar('Idade final', '20');
    t.botao('Aplicar faixa').click();
    await t.fixture.whenStable();
    expect(t.el.querySelectorAll('ul[aria-label="Idades aceitas"] li').length).toBe(3);
    expect(t.el.textContent).not.toContain('nenhuma solicitação será autorizada');

    t.botao('18').click();
    await t.fixture.whenStable();
    expect(t.el.querySelectorAll('ul[aria-label="Idades aceitas"] li').length).toBe(2);

    t.botao('Nenhum').click();
    await t.fixture.whenStable();
    expect(t.el.textContent).toContain('nenhuma solicitação será autorizada');
  });

  it('rejeita faixa invertida', async () => {
    const t = montar();
    await ate2(t);
    t.botao('Avançar').click();
    await t.fixture.whenStable();
    await t.digitar('Idade inicial', '20');
    await t.digitar('Idade final', '18');
    t.botao('Aplicar faixa').click();
    await t.fixture.whenStable();
    expect(t.el.textContent).toContain('não pode ser maior');
    expect(t.el.querySelector('ul[aria-label="Idades aceitas"]')).toBeNull();
  });

  /** Preenche tudo (18–20 anos, Ambos) e chega à etapa 4. */
  async function ate4(t: ReturnType<typeof montar>): Promise<void> {
    await ate2(t);
    t.botao('Avançar').click(); // cobertura → elegibilidade
    await t.fixture.whenStable();
    await t.digitar('Idade inicial', '18');
    await t.digitar('Idade final', '20');
    t.botao('Aplicar faixa').click();
    await t.fixture.whenStable();
    t.botao('Avançar').click();
    await t.fixture.whenStable();
  }

  const etapaAtual = (t: ReturnType<typeof montar>): string =>
    t.el.querySelector('[aria-current="step"]')?.textContent ?? '';

  it('mostra utilização com flags marcadas, texto PEN-02 e resumo', async () => {
    const t = montar();
    await ate4(t);
    expect(etapaAtual(t)).toContain('Utilização');
    const caixas = Array.from(t.el.querySelectorAll<HTMLInputElement>('input[type="checkbox"]'));
    expect(caixas.length).toBe(2);
    expect(caixas.every((c) => c.checked)).toBe(true);
    expect(t.el.textContent).toContain('não alteram o resultado da autorização');
    expect(t.el.textContent).not.toContain('conceituais');
    expect(t.el.querySelector('section[aria-labelledby="titulo-resumo"]')?.textContent).toContain(
      'Ressonância',
    );
    expect(t.el.querySelector('section[aria-labelledby="titulo-resumo"]')?.textContent).toContain(
      '3 idades (18 a 20)',
    );
  });

  it('envia o corpo esperado, mostra toast e volta à lista', async () => {
    const t = montar();
    const navegar = vi.mocked(TestBed.inject(Router).navigate);
    await ate4(t);
    t.botao('Cadastrar procedimento').click();
    await t.fixture.whenStable();
    const req = t.http.expectOne(URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      numProcedimento: 9999,
      nome: 'Ressonância',
      categoria: 'EXAMES_IMAGEM',
      exigeAutorizacaoPrevia: true,
      exigeJustificativaMedica: true,
      sexos: ['M', 'F'],
      idades: [18, 19, 20],
    });
    req.flush(
      { id: 1, numProcedimento: 9999, criadoEm: '2026-01-01T00:00:00Z' },
      { status: 201, statusText: 'Created' },
    );
    await t.fixture.whenStable();
    expect(TestBed.inject(NotificationService).items()[0].message).toBe(
      'Procedimento 9999 cadastrado',
    );
    expect(navegar).toHaveBeenCalledWith(['/procedimentos']);
    t.http.verify();
  });

  it('mostra PROCEDIMENTO_DUPLICADO no campo número, na etapa 1', async () => {
    const t = montar();
    await ate4(t);
    t.botao('Cadastrar procedimento').click();
    t.http
      .expectOne(URL)
      .flush(
        { code: 'PROCEDIMENTO_DUPLICADO', detail: 'Já existe o procedimento 9999.', status: 409 },
        { status: 409, statusText: 'Conflict' },
      );
    await t.fixture.whenStable();
    expect(etapaAtual(t)).toContain('Identificação');
    expect(t.el.textContent).toContain('Já existe o procedimento 9999.');
    // Editar o número descarta o erro do servidor.
    await t.digitar('Número', '10000');
    expect(t.el.textContent).not.toContain('Já existe o procedimento 9999.');
  });

  it('leva VALIDACAO_INVALIDA à etapa do campo', async () => {
    const t = montar();
    await ate4(t);
    t.botao('Cadastrar procedimento').click();
    t.http.expectOne(URL).flush(
      {
        code: 'VALIDACAO_INVALIDA',
        detail: 'Dados inválidos.',
        status: 400,
        errors: [{ campo: 'quantidadeMaxima', mensagem: 'Valor acima do permitido.' }],
      },
      { status: 400, statusText: 'Bad Request' },
    );
    await t.fixture.whenStable();
    expect(etapaAtual(t)).toContain('Cobertura');
    expect(t.el.textContent).toContain('Valor acima do permitido.');
  });

  it('abre o modal "Tentar novamente" em falha de rede e preserva o formulário', async () => {
    const t = montar();
    await ate4(t);
    t.botao('Cadastrar procedimento').click();
    t.http.expectOne(URL).error(new ProgressEvent('error'));
    await t.fixture.whenStable();
    const dialogo = TestBed.inject(ErrorDialogService);
    expect(dialogo.state()).not.toBeNull();
    expect(etapaAtual(t)).toContain('Utilização');
    dialogo.retry();
    const nova = t.http.expectOne(URL);
    expect((nova.request.body as { numProcedimento: number }).numProcedimento).toBe(9999);
    nova.flush({ id: 1, numProcedimento: 9999, criadoEm: '2026-01-01T00:00:00Z' });
  });
});
