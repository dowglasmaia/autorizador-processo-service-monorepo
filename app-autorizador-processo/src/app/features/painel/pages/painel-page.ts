import { ChangeDetectionStrategy, Component, OnInit, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { SessaoSolicitacoes } from '../../../core/sessao/sessao-solicitacoes';
import { Button, Card, EmptyState, KpiCard, PageHeader, Skeleton } from '../../../shared/ui';
import { calcularAprovacao, formatarDataExtenso } from '../calculos';
import { BarraCobertura } from '../components/barra-cobertura';
import { RoscaSexos } from '../components/rosca-sexos';
import { UltimasAutorizacoes } from '../components/ultimas-autorizacoes';
import { PainelService } from '../data-access/painel.service';

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
    <app-page-header kicker="Visão geral" title="Painel" [description]="hoje" />

    <section
      class="mt-6 flex flex-wrap items-center justify-between gap-4 rounded-card bg-accent-900 p-6 text-white sm:p-8"
      aria-labelledby="hero-titulo"
    >
      <div>
        <h2 id="hero-titulo" class="text-2xl font-extrabold sm:text-3xl">
          Autorize um procedimento em segundos
        </h2>
        <p class="mt-1 text-accent-200">Informe o procedimento, a idade e o sexo do paciente.</p>
      </div>
      <button app-button (click)="nova()">Nova solicitação</button>
    </section>

    @if (servico.erro(); as erro) {
      <div class="mt-6" aria-live="polite">
        <app-empty-state
          icon="warning"
          title="Não foi possível carregar"
          [description]="erro.detail"
        >
          <button app-button variant="secondary" (click)="servico.carregar()">
            Tentar novamente
          </button>
        </app-empty-state>
      </div>
    } @else {
      <section
        class="mt-6 grid gap-4 [grid-template-columns:repeat(auto-fit,minmax(14rem,1fr))]"
        aria-label="Indicadores"
      >
        <app-kpi-card
          label="Procedimentos"
          icon="clipboard-text"
          hint="cadastrados"
          [value]="resumo()?.totalProcedimentos ?? 0"
          [loading]="carregando()"
        />
        <app-kpi-card
          label="Autorizações"
          icon="check-circle"
          hint="concedidas"
          [value]="resumo()?.totalAutorizacoes ?? 0"
          [loading]="carregando()"
        />
        <app-kpi-card
          label="Idades cobertas"
          icon="users"
          hint="faixas distintas"
          [value]="resumo()?.idadesCobertas ?? 0"
          [loading]="carregando()"
        />
        <app-kpi-card
          label="Aprovação"
          icon="chart-pie"
          hint="solicitações desta sessão"
          [value]="aprovacao()"
        />
      </section>

      <div class="mt-6 grid gap-4 md:grid-cols-2">
        <app-card>
          <h2 class="text-lg font-bold text-content">Cobertura por idade</h2>
          <div class="mt-4">
            @if (resumo(); as r) {
              @if (r.coberturaPorIdade.length === 0) {
                <p class="text-content-muted">Nenhuma idade coberta ainda.</p>
              } @else {
                <app-barra-cobertura [dados]="r.coberturaPorIdade" />
              }
            } @else {
              <div class="grid gap-3" role="status" aria-label="Carregando cobertura">
                <app-skeleton class="h-4 w-full" />
                <app-skeleton class="h-4 w-full" />
                <app-skeleton class="h-4 w-full" />
              </div>
            }
          </div>
        </app-card>
        <app-card>
          <h2 class="text-lg font-bold text-content">Sexos aceitos</h2>
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
          <div class="grid gap-3" role="status" aria-label="Carregando últimas autorizações">
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
