import { ChangeDetectionStrategy, Component, OnInit, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { SessaoSolicitacoes } from '../../../core/sessao/sessao-solicitacoes';
import { Button, Card, EmptyState, KpiCard, PageHeader, Skeleton } from '../../../shared/ui';
import { calcularAprovacao, formatarDataExtenso } from '../calculos';
import { BarraCobertura } from '../components/barra-cobertura';
import { RoscaSexos } from '../components/rosca-sexos';
import { UltimasAutorizacoes } from '../components/ultimas-autorizacoes';
import { PainelService } from '../data-access/painel.service';
import { MENSAGENS } from '../../../core/i18n/mensagens';

/** Painel: cabeçalho, hero, KPIs, gráficos e últimas autorizações, com esqueleto, erro e vazio. */
@Component({
  selector: 'app-painel-page',
  imports: [
    PageHeader,
    Button,
    Card,
    KpiCard,
    Skeleton,
    EmptyState,
    BarraCobertura,
    RoscaSexos,
    UltimasAutorizacoes,
  ],
  template: `
    <app-page-header [kicker]="m.kicker" [title]="m.titulo" [description]="hoje" />

    <section
      class="mt-6 flex flex-wrap items-center justify-between gap-4 rounded-card bg-accent-900 p-6 text-white sm:p-8"
      aria-labelledby="hero-titulo"
    >
      <div>
        <h2 id="hero-titulo" class="text-2xl font-extrabold sm:text-3xl">
          {{ m.heroTitulo }}
        </h2>
        <p class="mt-1 text-accent-200">{{ m.heroTexto }}</p>
      </div>
      <button app-button (click)="nova()">{{ acoes.novaSolicitacao }}</button>
    </section>

    @if (servico.erro(); as erro) {
      <div class="mt-6" aria-live="polite">
        <app-empty-state
          icon="warning"
          [title]="feedback.tituloErroCarregar"
          [description]="erro.detail"
        >
          <button app-button variant="secondary" (click)="servico.carregar()">
            {{ acoes.tentarNovamente }}
          </button>
        </app-empty-state>
      </div>
    } @else {
      <section
        class="mt-6 grid gap-4 [grid-template-columns:repeat(auto-fit,minmax(14rem,1fr))]"
        [attr.aria-label]="m.indicadores"
      >
        <app-kpi-card
          [label]="m.kpiProcedimentos"
          icon="clipboard-text"
          [hint]="m.kpiProcedimentosDica"
          [value]="resumo()?.totalProcedimentos ?? 0"
          [loading]="carregando()"
        />
        <app-kpi-card
          [label]="m.kpiAutorizacoes"
          icon="check-circle"
          [hint]="m.kpiAutorizacoesDica"
          [value]="resumo()?.totalAutorizacoes ?? 0"
          [loading]="carregando()"
        />
        <app-kpi-card
          [label]="m.kpiIdades"
          icon="users"
          [hint]="m.kpiIdadesDica"
          [value]="resumo()?.idadesCobertas ?? 0"
          [loading]="carregando()"
        />
        <app-kpi-card
          [label]="m.kpiAprovacao"
          icon="chart-pie"
          [hint]="m.kpiAprovacaoDica"
          [value]="aprovacao()"
        />
      </section>

      <div class="mt-6 grid gap-4 md:grid-cols-2">
        <app-card>
          <h2 class="text-lg font-bold text-content">{{ m.coberturaTitulo }}</h2>
          <div class="mt-4">
            @if (resumo(); as r) {
              @if (r.coberturaPorIdade.length === 0) {
                <p class="text-content-muted">{{ m.coberturaVazia }}</p>
              } @else {
                <app-barra-cobertura [dados]="r.coberturaPorIdade" />
              }
            } @else {
              <div class="grid gap-3" role="status" [attr.aria-label]="m.coberturaCarregando">
                <app-skeleton class="h-4 w-full" />
                <app-skeleton class="h-4 w-full" />
                <app-skeleton class="h-4 w-full" />
              </div>
            }
          </div>
        </app-card>
        <app-card>
          <h2 class="text-lg font-bold text-content">{{ m.sexosTitulo }}</h2>
          <div class="mt-4">
            @if (resumo(); as r) {
              <app-rosca-sexos [dados]="r.distribuicaoSexos" />
            } @else {
              <app-skeleton class="h-36 w-36" />
            }
          </div>
        </app-card>
      </div>

      <app-card class="mt-6 block">
        @if (resumo(); as r) {
          <app-ultimas-autorizacoes [autorizacoes]="r.ultimasAutorizacoes" />
        } @else {
          <div class="grid gap-3" role="status" [attr.aria-label]="m.ultimasCarregando">
            <app-skeleton class="h-12 w-full" />
            <app-skeleton class="h-12 w-full" />
          </div>
        }
      </app-card>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PainelPage implements OnInit {
  private readonly router = inject(Router);
  private readonly sessao = inject(SessaoSolicitacoes);
  protected readonly servico = inject(PainelService);
  protected readonly m = MENSAGENS.painel;
  protected readonly acoes = MENSAGENS.acoes;
  protected readonly feedback = MENSAGENS.feedback;

  protected readonly hoje = formatarDataExtenso(new Date());
  protected readonly resumo = this.servico.resumo;
  protected readonly carregando = this.servico.carregando;
  protected readonly aprovacao = computed(() =>
    calcularAprovacao(this.sessao.concedidas(), this.sessao.negadas()),
  );

  ngOnInit(): void {
    this.servico.carregar();
  }

  protected nova(): void {
    void this.router.navigate(['/solicitar']);
  }
}
