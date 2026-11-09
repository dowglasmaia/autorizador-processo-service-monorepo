import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValueChangeEvent,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { filter } from 'rxjs';
import { isAppError, type AppError } from '../../../core/errors/app-error';
import { ErrorDialogService } from '../../../core/feedback/error-dialog';
import { paraAppError } from '../../../core/http/error.interceptor';
import { MENSAGENS } from '../../../core/i18n/mensagens';
import { NotificationService } from '../../../core/feedback/notification.service';
import type { ProcedimentoRequest } from '../../../shared/api';
import { ProcedimentosService } from '../data-access/procedimentos.service';
import { Button, Field, FieldControl, PageHeader, SegmentedChoice } from '../../../shared/ui';
import type { SegmentedOption } from '../../../shared/ui';
import {
  CATEGORIAS,
  COBERTURAS,
  ESPECIALIDADES,
  PERIODOS,
  ROTULO_CATEGORIA,
  ROTULO_COBERTURA,
  ROTULO_ESPECIALIDADE,
  ROTULO_FAIXA,
  INTERVALO_FAIXA,
  FAIXAS_ETARIAS,
  ROTULO_PERIODO,
  type Categoria,
  type Especialidade,
  type FaixaEtaria,
  type Periodo,
  type TipoCobertura,
} from '../rotulos';
import {
  ambosParaSexos,
  alternarFaixa,
  validarQuantidadePeriodo,
} from '../regras-formulario';

export type EscolhaSexo = 'AMBOS' | 'M' | 'F' | 'NENHUM';

export const ETAPAS = [
  { titulo: MENSAGENS.procedimentoNovo.etapas[0] },
  { titulo: MENSAGENS.procedimentoNovo.etapas[1] },
  { titulo: MENSAGENS.procedimentoNovo.etapas[2] },
  { titulo: MENSAGENS.procedimentoNovo.etapas[3] },
] as const;

const NUM_MAX = 2147483647;

/** Etapa (0..3) em que cada campo do `ProcedimentoRequest` é preenchido. */
const ETAPA_DO_CAMPO: Readonly<Record<string, number>> = {
  numProcedimento: 0,
  nome: 0,
  codigoTuss: 0,
  categoria: 0,
  especialidade: 0,
  tipoCobertura: 1,
  quantidadeMaxima: 1,
  periodo: 1,
  faixasEtarias: 2,
  sexos: 2,
  exigeAutorizacaoPrevia: 3,
  exigeJustificativaMedica: 3,
};

/** Nome do campo sem índice (`faixasEtarias[0]` → `faixasEtarias`). */
function nomeDoCampo(campo: string): string {
  return campo.replace(/\[.*$/, '');
}

/** Cadastro de procedimento em 4 etapas, com resumo e envio na última. */
@Component({
  selector: 'app-procedimento-novo-page',
  imports: [ReactiveFormsModule, PageHeader, Field, FieldControl, Button, SegmentedChoice],
  template: `
    <app-page-header
      [kicker]="m.kicker"
      [title]="m.titulo"
      [description]="m.descricao"
    />

    <nav class="mt-6" [attr.aria-label]="m.etapasRotulo">
      <ol class="flex flex-wrap gap-2">
        @for (e of etapas; track e.titulo; let i = $index) {
          <li>
            <button
              type="button"
              class="inline-flex min-h-11 items-center gap-2 rounded-pill border px-4 py-2 text-sm font-semibold transition-colors"
              [class]="
                i === etapa()
                  ? 'border-accent-700 bg-accent-100 text-accent-800'
                  : 'border-divider bg-surface text-content hover:bg-neutral-100'
              "
              [attr.aria-current]="i === etapa() ? 'step' : null"
              (click)="irPara(i)"
            >
              {{ i + 1 }} · {{ e.titulo }}
            </button>
          </li>
        }
      </ol>
    </nav>

    <form class="mt-6 grid max-w-3xl gap-5" [formGroup]="form" (ngSubmit)="avancar()" novalidate>
      @switch (etapa()) {
        @case (0) {
          <div formGroupName="identificacao" class="grid gap-5">
            <app-field
              label="Número do procedimento"
              [required]="true"
              [error]="erro('identificacao.numProcedimento')"
            >
              <input
                appFieldControl
                type="number"
                inputmode="numeric"
                formControlName="numProcedimento"
              />
            </app-field>
            <app-field
              label="Nome do procedimento"
              [required]="true"
              [error]="erro('identificacao.nome')"
            >
              <input appFieldControl type="text" formControlName="nome" autocomplete="off" />
            </app-field>
            <app-field label="Código TUSS" help="Opcional.">
              <input appFieldControl type="text" formControlName="codigoTuss" autocomplete="off" />
            </app-field>
            <app-field
              label="Categoria"
              [required]="true"
              [error]="erro('identificacao.categoria')"
            >
              <select appFieldControl formControlName="categoria">
                <option value="">Selecione</option>
                @for (c of categorias; track c) {
                  <option [value]="c">{{ rotuloCategoria[c] }}</option>
                }
              </select>
            </app-field>
            <app-field label="Especialidade" help="Opcional.">
              <select appFieldControl formControlName="especialidade">
                <option value="">Não informada</option>
                @for (e of especialidades; track e) {
                  <option [value]="e">{{ rotuloEspecialidade[e] }}</option>
                }
              </select>
            </app-field>
          </div>
        }
        @case (1) {
          <div formGroupName="cobertura" class="grid gap-5">
            <app-field label="Cobertura" help="Opcional.">
              <select appFieldControl formControlName="tipoCobertura">
                <option value="">Não informada</option>
                @for (c of coberturas; track c) {
                  <option [value]="c">{{ rotuloCobertura[c] }}</option>
                }
              </select>
            </app-field>
            <app-field
              label="Quantidade máxima"
              help="Opcional; exige o período."
              [error]="erroQuantidade('quantidadeMaxima')"
            >
              <input
                appFieldControl
                type="number"
                inputmode="numeric"
                formControlName="quantidadeMaxima"
              />
            </app-field>
            <app-field label="Período" [error]="erroQuantidade('periodo')">
              <select appFieldControl formControlName="periodo">
                <option value="">Não informado</option>
                @for (p of periodos; track p) {
                  <option [value]="p">{{ rotuloPeriodo[p] }}</option>
                }
              </select>
            </app-field>
          </div>
        }
        @case (2) {
          <div formGroupName="elegibilidade" class="grid gap-5">
            <fieldset class="grid gap-3">
              <legend class="mb-1.5 text-sm font-semibold text-content">Faixas etárias aceitas</legend>
              <ul class="grid gap-3 sm:grid-cols-2">
                @for (f of faixasDisponiveis; track f) {
                  <li>
                    <button
                      type="button"
                      class="flex min-h-11 w-full items-center justify-between gap-3 rounded-card border px-4 py-3 text-left transition-[transform,background-color,border-color] duration-150 ease-out active:scale-[0.98] motion-reduce:transition-none"
                      [class]="
                        faixaSelecionada(f)
                          ? 'border-accent-700 bg-accent-100 text-accent-800'
                          : 'border-divider bg-surface text-content hover:bg-neutral-100'
                      "
                      [attr.aria-pressed]="faixaSelecionada(f)"
                      (click)="alternarFaixaEtaria(f)"
                    >
                      <span class="grid">
                        <span class="font-semibold">{{ rotuloFaixa[f] }}</span>
                        <span class="text-sm">{{ intervaloFaixa[f] }}</span>
                      </span>
                      @if (faixaSelecionada(f)) {
                        <span aria-hidden="true" class="font-bold">✓</span>
                      }
                    </button>
                  </li>
                }
              </ul>
              @if (erroServidor('faixasEtarias'); as e) {
                <p class="text-sm font-semibold text-danger" role="alert">{{ e }}</p>
              }
            </fieldset>

            <div>
              <p class="mb-1.5 text-sm font-semibold text-content">Sexo</p>
              <app-segmented-choice
                label="Sexo"
                [options]="opcoesSexo"
                [value]="sexo()"
                (valueChange)="definirSexo($event)"
              />
            </div>

            @if (avisoRn9()) {
              <p
                class="rounded-card border border-divider bg-neutral-100 p-4 text-sm text-content"
                role="status"
              >
                Sem faixa etária ou sexo definidos, nenhuma solicitação será autorizada para este
                procedimento. Você pode continuar assim mesmo.
              </p>
            }
          </div>
        }
        @default {
          <div formGroupName="utilizacao" class="grid gap-5">
            <fieldset class="grid gap-3">
              <legend class="mb-1.5 text-sm font-semibold text-content">Utilização</legend>
              <label class="flex min-h-11 items-center gap-3 text-content">
                <input
                  type="checkbox"
                  class="size-5 accent-accent-700"
                  formControlName="exigeAutorizacaoPrevia"
                />
                Exige autorização prévia
              </label>
              <label class="flex min-h-11 items-center gap-3 text-content">
                <input
                  type="checkbox"
                  class="size-5 accent-accent-700"
                  formControlName="exigeJustificativaMedica"
                />
                Exige justificativa médica
              </label>
              <p class="text-sm text-content-muted">
                Cobertura, quantidade máxima, autorização prévia e justificativa médica são
                informativas e não alteram o resultado da autorização, que depende só da faixa etária e
                do sexo.
              </p>
            </fieldset>

            <section aria-labelledby="titulo-resumo" class="rounded-card border border-divider p-4">
              <h2 id="titulo-resumo" class="mb-3 text-base font-semibold text-content">Resumo</h2>
              <dl class="grid gap-2 text-sm sm:grid-cols-[auto_1fr] sm:gap-x-6">
                @for (linha of resumo(); track linha.rotulo) {
                  <dt class="font-semibold text-content">{{ linha.rotulo }}</dt>
                  <dd class="text-content">{{ linha.valor }}</dd>
                }
              </dl>
            </section>
          </div>
        }
      }

      @if (erroGeral(); as e) {
        <p class="text-sm font-semibold text-danger" role="alert">{{ e }}</p>
      }

      <div class="flex flex-wrap gap-3">
        <button app-button variant="secondary" [disabled]="etapa() === 0" (click)="voltar()">
          {{ m.voltar }}
        </button>
        @if (etapa() < ultima) {
          <button app-button type="submit">{{ m.avancar }}</button>
        } @else {
          <button app-button type="submit" [loading]="salvando()">{{ m.cadastrar }}</button>
        }
      </div>
    </form>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProcedimentoNovoPage {
  protected readonly m = MENSAGENS.procedimentoNovo;
  protected readonly etapas = ETAPAS;
  protected readonly ultima = ETAPAS.length - 1;
  protected readonly categorias = CATEGORIAS;
  protected readonly especialidades = ESPECIALIDADES;
  protected readonly coberturas = COBERTURAS;
  protected readonly periodos = PERIODOS;
  protected readonly rotuloCategoria = ROTULO_CATEGORIA;
  protected readonly rotuloEspecialidade = ROTULO_ESPECIALIDADE;
  protected readonly rotuloCobertura = ROTULO_COBERTURA;
  protected readonly rotuloPeriodo = ROTULO_PERIODO;
  protected readonly faixasDisponiveis = FAIXAS_ETARIAS;
  protected readonly rotuloFaixa = ROTULO_FAIXA;
  protected readonly intervaloFaixa = INTERVALO_FAIXA;
  protected readonly opcoesSexo: readonly SegmentedOption[] = [
    { value: 'AMBOS', label: 'Ambos' },
    { value: 'M', label: 'Masculino' },
    { value: 'F', label: 'Feminino' },
    { value: 'NENHUM', label: 'Nenhum' },
  ];

  readonly form = new FormGroup({
    identificacao: new FormGroup({
      numProcedimento: new FormControl<number | null>(null, {
        validators: [
          Validators.required,
          Validators.min(1),
          Validators.max(NUM_MAX),
          Validators.pattern(/^\d+$/),
        ],
      }),
      nome: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(/\S/)],
      }),
      codigoTuss: new FormControl('', { nonNullable: true }),
      categoria: new FormControl<Categoria | ''>('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
      especialidade: new FormControl<Especialidade | ''>('', { nonNullable: true }),
    }),
    cobertura: new FormGroup({
      tipoCobertura: new FormControl<TipoCobertura | ''>('', { nonNullable: true }),
      quantidadeMaxima: new FormControl<number | null>(null),
      periodo: new FormControl<Periodo | ''>('', { nonNullable: true }),
    }),
    elegibilidade: new FormGroup({
    }),
    utilizacao: new FormGroup({
      exigeAutorizacaoPrevia: new FormControl(true, { nonNullable: true }),
      exigeJustificativaMedica: new FormControl(true, { nonNullable: true }),
    }),
  });

  protected readonly etapa = signal(0);
  protected readonly faixas = signal<readonly FaixaEtaria[]>([]);
  protected readonly sexo = signal<EscolhaSexo>('AMBOS');
  protected readonly salvando = signal(false);
  /** Mensagens do servidor por campo (`PROCEDIMENTO_DUPLICADO`, `VALIDACAO_INVALIDA`). */
  private readonly errosServidor = signal<Readonly<Record<string, string>>>({});
  protected readonly erroGeral = signal('');

  private readonly service = inject(ProcedimentosService);
  private readonly router = inject(Router);
  private readonly notificacoes = inject(NotificationService);
  private readonly dialogo = inject(ErrorDialogService);
  /** Etapas em que o usuário já tentou avançar (mostra os erros dos campos). */
  private readonly tentadas = signal<ReadonlySet<number>>(new Set());
  /** Muda a cada alteração do formulário, para reavaliar os erros no template. */
  private readonly versao = signal(0);

  constructor() {
    this.form.events
      .pipe(takeUntilDestroyed(inject(DestroyRef)))
      .subscribe(() => this.versao.update((v) => v + 1));
    // Editar um valor descarta o erro do servidor (que já não vale para o novo valor).
    this.form.events
      .pipe(
        filter((e) => e instanceof ValueChangeEvent),
        takeUntilDestroyed(inject(DestroyRef)),
      )
      .subscribe(() => {
        if (Object.keys(this.errosServidor()).length > 0) this.errosServidor.set({});
        this.erroGeral.set('');
      });
  }

  protected resumo(): readonly { rotulo: string; valor: string }[] {
    this.versao();
    const id = this.form.controls.identificacao.getRawValue();
    const cob = this.form.controls.cobertura.getRawValue();
    const uti = this.form.controls.utilizacao.getRawValue();
    const sim = (b: boolean): string => (b ? 'Sim' : 'Não');
    const faixas = this.faixas();
    const textoFaixas =
      faixas.length === 0 ? 'Nenhuma' : faixas.map((f) => ROTULO_FAIXA[f]).join(', ');
    return [
      { rotulo: 'Número', valor: id.numProcedimento === null ? '—' : String(id.numProcedimento) },
      { rotulo: 'Nome', valor: id.nome.trim() || '—' },
      { rotulo: 'Código TUSS', valor: id.codigoTuss.trim() || 'Não informado' },
      { rotulo: 'Categoria', valor: id.categoria ? ROTULO_CATEGORIA[id.categoria] : '—' },
      {
        rotulo: 'Especialidade',
        valor: id.especialidade ? ROTULO_ESPECIALIDADE[id.especialidade] : 'Não informada',
      },
      {
        rotulo: 'Cobertura',
        valor: cob.tipoCobertura ? ROTULO_COBERTURA[cob.tipoCobertura] : 'Não informada',
      },
      {
        rotulo: 'Quantidade máxima',
        valor:
          cob.quantidadeMaxima !== null && cob.periodo
            ? `${cob.quantidadeMaxima} por ${ROTULO_PERIODO[cob.periodo].toLowerCase()}`
            : 'Não informada',
      },
      { rotulo: 'Faixas etárias', valor: textoFaixas },
      {
        rotulo: 'Sexo',
        valor: this.opcoesSexo.find((o) => o.value === this.sexo())?.label ?? '—',
      },
      { rotulo: 'Autorização prévia', valor: sim(uti.exigeAutorizacaoPrevia) },
      { rotulo: 'Justificativa médica', valor: sim(uti.exigeJustificativaMedica) },
    ];
  }

  /** Monta o corpo do POST; campos opcionais vazios são omitidos. */
  protected montarRequest(): ProcedimentoRequest {
    const id = this.form.controls.identificacao.getRawValue();
    const cob = this.form.controls.cobertura.getRawValue();
    const uti = this.form.controls.utilizacao.getRawValue();
    const sexo = this.sexo();
    const sexos: ProcedimentoRequest['sexos'] =
      sexo === 'AMBOS' ? ambosParaSexos(true) : sexo === 'NENHUM' ? [] : [sexo];
    const codigoTuss = id.codigoTuss.trim();
    return {
      numProcedimento: id.numProcedimento as number,
      nome: id.nome.trim(),
      ...(codigoTuss ? { codigoTuss } : {}),
      categoria: id.categoria as Categoria,
      ...(id.especialidade ? { especialidade: id.especialidade } : {}),
      ...(cob.tipoCobertura ? { tipoCobertura: cob.tipoCobertura } : {}),
      ...(cob.quantidadeMaxima !== null && cob.periodo
        ? { quantidadeMaxima: cob.quantidadeMaxima, periodo: cob.periodo }
        : {}),
      exigeAutorizacaoPrevia: uti.exigeAutorizacaoPrevia,
      exigeJustificativaMedica: uti.exigeJustificativaMedica,
      sexos,
      faixasEtarias: [...this.faixas()],
    };
  }

  protected salvar(): void {
    if (this.salvando()) return;
    for (let i = 0; i < this.ultima; i++) {
      if (!this.etapaValida(i)) {
        this.etapa.set(i);
        return;
      }
    }
    this.salvando.set(true);
    this.errosServidor.set({});
    this.erroGeral.set('');
    const corpo = this.montarRequest();
    this.service.cadastrarProcedimento(corpo).subscribe({
      next: () => {
        this.salvando.set(false);
        this.notificacoes.show(MENSAGENS.procedimentoNovo.cadastrado(corpo.numProcedimento));
        void this.router.navigate(['/procedimentos']);
      },
      error: (erro: unknown) => {
        this.salvando.set(false);
        this.tratarErro(isAppError(erro) ? erro : paraAppError(erro));
      },
    });
  }

  private tratarErro(erro: AppError): void {
    if (erro.code === 'PROCEDIMENTO_DUPLICADO') {
      this.errosServidor.set({ numProcedimento: erro.detail });
      this.etapa.set(0);
      return;
    }
    if (erro.code === 'VALIDACAO_INVALIDA') {
      const mapeados: Record<string, string> = {};
      let primeira = Number.POSITIVE_INFINITY;
      const naoMapeados: string[] = [];
      for (const [campo, mensagem] of Object.entries(erro.campos ?? {})) {
        const nome = nomeDoCampo(campo);
        const etapa = ETAPA_DO_CAMPO[nome];
        if (etapa === undefined) {
          naoMapeados.push(mensagem);
          continue;
        }
        mapeados[nome] = mensagem;
        primeira = Math.min(primeira, etapa);
      }
      this.errosServidor.set(mapeados);
      this.erroGeral.set(
        naoMapeados.length > 0
          ? naoMapeados.join(' ')
          : primeira === Number.POSITIVE_INFINITY
            ? erro.detail
            : '',
      );
      if (primeira !== Number.POSITIVE_INFINITY) this.etapa.set(primeira);
      return;
    }
    if (erro.kind === 'rede' || erro.kind === 'servidor') {
      this.dialogo.show(erro.detail, () => this.salvar());
      return;
    }
    this.erroGeral.set(erro.detail);
  }

  protected avisoRn9(): boolean {
    return this.faixas().length === 0 || this.sexo() === 'NENHUM';
  }

  protected erro(caminho: string): string {
    this.versao();
    const doServidor = this.errosServidor()[caminho.split('.').pop() ?? ''];
    if (doServidor) return doServidor;
    const c = this.form.get(caminho);
    if (!c || c.valid || !(c.touched || this.tentadas().has(this.etapa()))) return '';
    if (c.hasError('required')) return this.m.campoObrigatorio;
    if (caminho.endsWith('nome')) return this.m.informeNome;
    return this.m.numeroInvalido;
  }

  protected erroQuantidade(campo: 'quantidadeMaxima' | 'periodo'): string {
    this.versao();
    const doServidor = this.errosServidor()[campo];
    if (doServidor) return doServidor;
    if (!this.tentadas().has(1)) return '';
    return this.errosCobertura()[campo] ?? '';
  }

  private errosCobertura(): { quantidadeMaxima?: string; periodo?: string } {
    const v = this.form.controls.cobertura.getRawValue();
    return validarQuantidadePeriodo(v.quantidadeMaxima, v.periodo === '' ? null : v.periodo);
  }

  protected erroServidor(campo: string): string | null {
    return this.errosServidor()[campo] ?? null;
  }

  protected definirSexo(valor: string): void {
    this.sexo.set(valor as EscolhaSexo);
  }

  protected faixaSelecionada(faixa: FaixaEtaria): boolean {
    return this.faixas().includes(faixa);
  }

  protected alternarFaixaEtaria(faixa: FaixaEtaria): void {
    this.faixas.update((atuais) => alternarFaixa(atuais, faixa));
  }

  /** Valida a etapa e marca os campos para exibir os erros. */
  protected etapaValida(i: number): boolean {
    this.tentadas.update((s) => new Set(s).add(i));
    this.versao.update((v) => v + 1);
    switch (i) {
      case 0:
        this.form.controls.identificacao.markAllAsTouched();
        return this.form.controls.identificacao.valid;
      case 1:
        return Object.keys(this.errosCobertura()).length === 0;
      default:
        return true;
    }
  }

  protected avancar(): void {
    if (this.etapa() >= this.ultima) {
      this.salvar();
      return;
    }
    if (this.etapaValida(this.etapa())) this.etapa.update((e) => e + 1);
  }

  protected voltar(): void {
    this.etapa.update((e) => Math.max(0, e - 1));
  }

  /** Voltar é livre; avançar por chip exige as etapas anteriores válidas. */
  protected irPara(destino: number): void {
    while (this.etapa() < destino) {
      if (!this.etapaValida(this.etapa())) return;
      this.etapa.update((e) => e + 1);
    }
    this.etapa.set(destino);
  }
}
