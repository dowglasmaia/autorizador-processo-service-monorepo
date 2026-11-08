import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ErrorDialogService } from '../../../core/feedback/error-dialog';
import type { AppError } from '../../../core/errors/app-error';
import { SessaoSolicitacoes } from '../../../core/sessao/sessao-solicitacoes';
import type { ProcedimentoDto } from '../../../shared/api';
import {
  Button,
  Card,
  Field,
  FieldControl,
  Icon,
  PageHeader,
  SegmentedChoice,
  Skeleton,
} from '../../../shared/ui';
import { AutorizacaoService } from '../data-access/autorizacao.service';
import {
  IDADE_MAXIMA,
  IDADE_MINIMA,
  OPCOES_SEXO,
  criarFormularioSolicitacao,
  type SexoSolicitacao,
} from '../formulario-solicitacao';

/** Resultado exibido no painel; `null` representa o estado inicial. */
export type ResultadoExibido =
  | {
      readonly tipo: 'autorizado';
      readonly numero: number;
      readonly idade: number;
      readonly sexo: SexoSolicitacao;
    }
  | { readonly tipo: 'negado'; readonly detail: string };

/** Tela de solicitação: formulário à esquerda e painel de resultado à direita. */
@Component({
  selector: 'app-solicitar-page',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    PageHeader,
    Card,
    Field,
    FieldControl,
    Button,
    Icon,
    SegmentedChoice,
    Skeleton,
  ],
  template: `
    <app-page-header
      kicker="Nova solicitação"
      title="Solicitar autorização"
      description="Informe o procedimento, a idade e o sexo do paciente."
    />

    <div class="mt-6 grid gap-6 lg:grid-cols-2">
      <app-card>
        <form class="grid gap-5" [formGroup]="form" (ngSubmit)="solicitar()" novalidate>
          <app-field label="Procedimento" [required]="true" [error]="erro('procedimento')">
            @if (carregandoLista()) {
              <app-skeleton class="h-11 w-full" />
              <span class="sr-only" role="status">Carregando procedimentos…</span>
            } @else if (erroLista()) {
              <div class="flex flex-wrap items-center gap-3">
                <p class="text-sm font-semibold text-danger" role="alert">
                  Não foi possível carregar os procedimentos.
                </p>
                <button app-button variant="secondary" (click)="carregarLista()">
                  Tentar novamente
                </button>
              </div>
            } @else {
              <select appFieldControl formControlName="procedimento">
                <option [ngValue]="null">Selecione</option>
                @for (p of procedimentos(); track p.numProcedimento) {
                  <option [ngValue]="p.numProcedimento">{{ rotulo(p) }}</option>
                }
              </select>
            }
          </app-field>

          <app-field
            label="Idade"
            [required]="true"
            [help]="'Em anos, de ' + idadeMinima + ' a ' + idadeMaxima + '.'"
            [error]="erro('idade')"
          >
            <input
              appFieldControl
              type="number"
              inputmode="numeric"
              formControlName="idade"
              [min]="idadeMinima"
              [max]="idadeMaxima"
            />
          </app-field>

          <div>
            <p class="mb-1.5 text-sm font-semibold text-content">
              Sexo <span class="text-danger" aria-hidden="true">*</span>
            </p>
            <app-segmented-choice
              label="Sexo"
              [options]="opcoesSexo"
              [value]="form.controls.sexo.value"
              (valueChange)="definirSexo($event)"
            />
            @if (erro('sexo'); as e) {
              <p class="mt-1.5 text-sm font-semibold text-danger">{{ e }}</p>
            }
          </div>

          @if (erroGeral(); as geral) {
            <p class="text-sm font-semibold text-danger" role="alert">{{ geral }}</p>
          }

          <div class="flex flex-wrap gap-3">
            <button app-button type="submit" [loading]="enviando()">Solicitar autorização</button>
            <button app-button variant="secondary" (click)="limpar()">Limpar</button>
          </div>
        </form>
      </app-card>

      <app-card>
        <section aria-labelledby="titulo-resultado">
          <h2 id="titulo-resultado" class="mb-3 text-base font-semibold text-content">Resultado</h2>
          <div aria-live="polite" aria-atomic="true">
            @let r = resultado();
            @if (r?.tipo === 'autorizado') {
              <div
                class="animate-aparecer motion-reduce:animate-none grid gap-3 rounded-card bg-accent-100 p-5 text-accent-900"
                data-resultado="autorizado"
              >
                <p class="flex items-center gap-2 text-lg font-bold">
                  <app-icon name="check-circle" class="text-2xl" /> Autorizado
                </p>
                <p>{{ textoAutorizado() }}</p>
                <a
                  routerLink="/autorizacoes"
                  class="font-semibold text-accent-800 underline underline-offset-2"
                >
                  Ver autorizações concedidas
                </a>
              </div>
            } @else if (r?.tipo === 'negado') {
              <div
                class="animate-aparecer motion-reduce:animate-none grid gap-3 rounded-card bg-danger-soft p-5 text-danger"
                data-resultado="negado"
              >
                <p class="flex items-center gap-2 text-lg font-bold">
                  <app-icon name="x-circle" class="text-2xl" /> Negado
                </p>
                <p>{{ textoNegado() }}</p>
                <p class="text-sm">A regra não informa qual critério falhou.</p>
              </div>
            } @else {
              <div
                class="flex items-center gap-2 rounded-card bg-neutral-100 p-5 text-content-muted"
                data-resultado="inicial"
              >
                <app-icon name="hourglass" class="text-2xl" />
                <p>O resultado aparece aqui.</p>
              </div>
            }
          </div>
        </section>
      </app-card>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SolicitarPage {
  private readonly service = inject(AutorizacaoService);
  private readonly sessao = inject(SessaoSolicitacoes);
  private readonly rota = inject(ActivatedRoute);
  private readonly dialogo = inject(ErrorDialogService);

  protected readonly form = criarFormularioSolicitacao();
  protected readonly opcoesSexo = OPCOES_SEXO;
  protected readonly idadeMinima = IDADE_MINIMA;
  protected readonly idadeMaxima = IDADE_MAXIMA;

  protected readonly procedimentos = signal<readonly ProcedimentoDto[]>([]);
  protected readonly carregandoLista = signal(false);
  protected readonly erroLista = signal(false);
  protected readonly enviando = signal(false);
  protected readonly resultado = signal<ResultadoExibido | null>(null);
  /** Verdadeiro depois da primeira tentativa de envio (mostra os erros de todos os campos). */
  private readonly tentou = signal(false);
  /** Muda a cada alteração do formulário, para reavaliar as mensagens no template. */
  private readonly versao = signal(0);
  /** Mensagens de validação devolvidas pelo servidor, por campo. */
  protected readonly errosServidor = signal<Readonly<Record<string, string>>>({});
  protected readonly erroGeral = signal('');

  constructor() {
    this.form.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.versao.update((v) => v + 1));
    this.carregarLista();
  }

  /** Texto do seletor: "Nº n" e, quando existir, " · nome". */
  protected rotulo(p: ProcedimentoDto): string {
    return p.nome ? `Nº ${p.numProcedimento} · ${p.nome}` : `Nº ${p.numProcedimento}`;
  }

  protected textoAutorizado(): string {
    const r = this.resultado();
    if (r?.tipo !== 'autorizado') return '';
    const sexo = r.sexo === 'M' ? 'masculino' : 'feminino';
    return `Procedimento ${r.numero} liberado para paciente ${sexo} de ${r.idade} ${r.idade === 1 ? 'ano' : 'anos'}.`;
  }

  protected textoNegado(): string {
    const r = this.resultado();
    return r?.tipo === 'negado' ? r.detail : '';
  }

  protected carregarLista(): void {
    this.carregandoLista.set(true);
    this.erroLista.set(false);
    this.service.listarProcedimentos().subscribe({
      next: (lista) => {
        this.procedimentos.set(lista);
        this.carregandoLista.set(false);
        this.preSelecionar(lista);
      },
      error: () => {
        this.carregandoLista.set(false);
        this.erroLista.set(true);
      },
    });
  }

  /** Aplica `?numero=` quando o procedimento existe na lista e nada foi escolhido ainda. */
  private preSelecionar(lista: readonly ProcedimentoDto[]): void {
    if (this.form.controls.procedimento.value !== null) return;
    const numero = Number(this.rota.snapshot.queryParamMap.get('numero'));
    if (Number.isInteger(numero) && lista.some((p) => p.numProcedimento === numero)) {
      this.form.controls.procedimento.setValue(numero);
    }
  }

  private nomeApi(campo: 'procedimento' | 'idade' | 'sexo'): string {
    return campo === 'procedimento' ? 'numProcedimento' : campo;
  }

  protected definirSexo(valor: string): void {
    this.form.controls.sexo.setValue(valor);
    this.form.controls.sexo.markAsTouched();
  }

  /** Restaura formulário e resultado ao estado inicial (a lista carregada é mantida). */
  protected limpar(): void {
    this.form.reset();
    this.resultado.set(null);
    this.errosServidor.set({});
    this.erroGeral.set('');
    this.tentou.set(false);
    this.versao.update((v) => v + 1);
  }

  /** Mensagem de validação do campo (vazia quando válido ou ainda não tocado). */
  protected erro(campo: 'procedimento' | 'idade' | 'sexo'): string {
    this.versao();
    const doServidor = this.errosServidor()[this.nomeApi(campo)];
    if (doServidor) return doServidor;
    const c = this.form.controls[campo];
    if (c.valid || !(c.touched || this.tentou())) return '';
    if (campo === 'procedimento') return 'Escolha um procedimento.';
    if (campo === 'sexo') return 'Escolha o sexo do paciente.';
    if (c.hasError('required')) return 'Informe a idade.';
    if (c.hasError('inteiro')) return 'Use uma idade inteira.';
    return `A idade deve estar entre ${IDADE_MINIMA} e ${IDADE_MAXIMA}.`;
  }

  protected solicitar(): void {
    if (this.enviando()) return;
    this.tentou.set(true);
    this.errosServidor.set({});
    this.erroGeral.set('');
    this.form.markAllAsTouched();
    this.versao.update((v) => v + 1);
    if (this.form.invalid) return;
    const { procedimento, idade, sexo } = this.form.getRawValue();
    const numero = procedimento as number;
    const anos = idade as number;
    const sexoEnviado = sexo as SexoSolicitacao;
    this.enviando.set(true);
    this.service
      .solicitarAutorizacao({ numProcedimento: numero, idade: anos, sexo: sexoEnviado })
      .subscribe({
        next: (r) => {
          this.enviando.set(false);
          this.sessao.registrar(r.resultado);
          this.resultado.set(
            r.resultado === 'autorizado'
              ? { tipo: 'autorizado', numero, idade: anos, sexo: sexoEnviado }
              : { tipo: 'negado', detail: r.detail },
          );
        },
        error: (erro: AppError) => {
          this.enviando.set(false);
          this.tratarErro(erro);
        },
      });
  }

  private tratarErro(erro: AppError): void {
    if (erro.code === 'PROCEDIMENTO_NAO_ENCONTRADO') {
      this.form.controls.procedimento.setValue(null);
      this.errosServidor.set({ numProcedimento: erro.detail });
      this.carregarLista();
      return;
    }
    if (erro.code === 'VALIDACAO_INVALIDA') {
      const campos = erro.campos ?? {};
      const conhecidos = ['numProcedimento', 'idade', 'sexo'];
      const mapeados: Record<string, string> = {};
      const outros: string[] = [];
      for (const [campo, mensagem] of Object.entries(campos)) {
        if (conhecidos.includes(campo)) mapeados[campo] = mensagem;
        else outros.push(mensagem);
      }
      this.errosServidor.set(mapeados);
      this.erroGeral.set(
        outros.length > 0 ? outros.join(' ') : Object.keys(mapeados).length > 0 ? '' : erro.detail,
      );
      return;
    }
    if (erro.kind === 'rede' || erro.kind === 'servidor') {
      // O formulário é preservado; "Tentar novamente" reenvia os mesmos dados.
      this.dialogo.show(erro.detail, () => this.solicitar());
      return;
    }
    this.erroGeral.set(erro.detail);
  }
}
