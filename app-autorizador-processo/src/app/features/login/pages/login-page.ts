import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  afterNextRender,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { retornoSeguro } from '../../../core/auth/retorno-seguro';
import { isAppError } from '../../../core/errors/app-error';
import { paraAppError } from '../../../core/http/error.interceptor';
import { ErrorDialog, ErrorDialogService } from '../../../core/feedback/error-dialog';
import { Button, Card, Field, FieldControl, Icon } from '../../../shared/ui';
import { MENSAGEM_SESSAO_EXPIRADA, classificarErroLogin } from '../mensagens-login';
import { MENSAGENS } from '../../../core/i18n/mensagens';

/** Tela `/login`: painel de marca (a partir de `lg`) e cartão com o formulário de e-mail e senha. */
@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, Button, Card, Field, FieldControl, Icon, ErrorDialog],
  template: `
    <div class="grid min-h-dvh lg:grid-cols-2">
      <section
        class="hidden flex-col justify-center bg-linear-to-b from-sidebar-from to-sidebar-to p-12 text-sidebar-content lg:flex"
        [attr.aria-label]="m.apresentacaoRotulo"
      >
        <p class="text-4xl font-extrabold text-white">Autorizador</p>
        <p class="mt-4 max-w-md text-lg">
          {{ m.apresentacao }}
        </p>
      </section>

      <main id="conteudo" class="flex items-center justify-center p-4 sm:p-8">
        <app-card class="w-full max-w-md">
          <h1 class="text-2xl font-extrabold text-content">{{ m.titulo }}</h1>
          <p class="mt-1 text-content-muted">{{ m.ajuda }}</p>

          @if (sessaoExpirada()) {
            <p
              class="mt-4 rounded-input bg-neutral-100 px-3.5 py-2.5 font-semibold text-content"
              role="status"
            >
              {{ avisoExpiracao }}
            </p>
          }

          <form
            class="mt-6 flex flex-col gap-4"
            [formGroup]="form"
            (ngSubmit)="enviar()"
            novalidate
          >
            <app-field [label]="m.email" [required]="true" [error]="erroDe('email')">
              <input
                #campoEmail
                appFieldControl
                type="email"
                autocomplete="username"
                formControlName="email"
              />
            </app-field>
            <app-field [label]="m.senha" [required]="true" [error]="erroDe('senha')">
              <div class="relative">
                <input
                  appFieldControl
                  [type]="senhaVisivel() ? 'text' : 'password'"
                  autocomplete="current-password"
                  formControlName="senha"
                  class="pr-12"
                />
                <button
                  type="button"
                  class="absolute inset-y-0 right-0 inline-flex size-11 items-center justify-center rounded-pill hover:bg-neutral-100"
                  [attr.aria-label]="m.mostrarSenha"
                  [attr.aria-pressed]="senhaVisivel()"
                  (click)="senhaVisivel.set(!senhaVisivel())"
                >
                  <app-icon [name]="senhaVisivel() ? 'eye-slash' : 'eye'" />
                </button>
              </div>
            </app-field>
            @if (alerta()) {
              <p class="text-sm font-semibold text-danger" role="alert">{{ alerta() }}</p>
            }
            <button app-button type="submit" [loading]="enviando()" [disabled]="enviando()">
              {{ m.titulo }}
            </button>
          </form>
        </app-card>
      </main>
    </div>
    <app-error-dialog />
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly rota = inject(ActivatedRoute);

  private readonly dialogo = inject(ErrorDialogService);
  private readonly campoEmail = viewChild<ElementRef<HTMLInputElement>>('campoEmail');

  protected readonly m = MENSAGENS.login;
  protected readonly avisoExpiracao = MENSAGEM_SESSAO_EXPIRADA;
  protected readonly sessaoExpirada = this.auth.sessaoExpirada;
  protected readonly enviando = signal(false);
  protected readonly senhaVisivel = signal(false);
  protected readonly alerta = signal('');
  protected readonly erros = signal<Readonly<Record<string, string>>>({});

  constructor() {
    afterNextRender(() => this.campoEmail()?.nativeElement.focus());
  }

  protected readonly form = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    senha: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  protected erroDe(campo: 'email' | 'senha'): string {
    const servidor = this.erros()[campo];
    if (servidor) return servidor;
    const c = this.form.controls[campo];
    if (!c.touched || c.valid) return '';
    if (c.hasError('required')) return campo === 'email' ? this.m.informeEmail : this.m.informeSenha;
    return this.m.emailInvalido;
  }

  protected enviar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.enviando()) return;
    this.enviando.set(true);
    this.alerta.set('');
    this.erros.set({});
    this.auth.entrar(this.form.getRawValue()).subscribe({
      next: () => {
        this.enviando.set(false);
        this.auth.consumirAvisoExpiracao();
        void this.router.navigateByUrl(
          retornoSeguro(this.rota.snapshot.queryParamMap.get('retorno')),
        );
      },
      error: (erro: unknown) => {
        this.enviando.set(false);
        this.tratarErro(isAppError(erro) ? erro : paraAppError(erro));
      },
    });
  }

  private tratarErro(erro: ReturnType<typeof paraAppError>): void {
    const resultado = classificarErroLogin(erro);
    switch (resultado.tipo) {
      case 'alerta':
        this.alerta.set(resultado.mensagem);
        break;
      case 'campos':
        this.erros.set(resultado.campos);
        break;
      case 'modal':
        this.dialogo.show(resultado.mensagem, () => this.enviar());
        break;
    }
  }
}
