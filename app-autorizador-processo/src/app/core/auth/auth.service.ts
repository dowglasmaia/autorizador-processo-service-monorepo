import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { ApiClient } from '../http/api-client';
import type { LoginRequest, TokenResponse, Usuario } from '../../shared/api';

const CHAVE_SESSAO = 'autorizador.sessao';
/** Maior atraso aceito por `setTimeout` (~24,8 dias). */
const ATRASO_MAXIMO_MS = 2_147_483_647;

interface SessaoArmazenada {
  readonly token: string;
  readonly usuario: Usuario;
  /** Instante de expiração, em milissegundos desde a época. */
  readonly expiraEm: number;
}

export interface OpcoesSair {
  /** Caminho a retomar após novo login (validado por `retornoSeguro` no destino). */
  readonly retorno?: string;
  /** Indica que a sessão terminou por expiração, para exibir o aviso no login. */
  readonly expirada?: boolean;
}

/**
 * Estado de autenticação. O token vive em memória e em `sessionStorage` (some ao fechar a aba);
 * a decodificação do JWT serve apenas para agendar o logout (UX) — a validade real é do servidor.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiClient);
  private readonly router = inject(Router);

  private readonly _usuario = signal<Usuario | null>(null);
  private readonly _token = signal<string | null>(null);
  private readonly _expiraEm = signal<number | null>(null);
  private readonly _sessaoExpirada = signal(false);
  private readonly limpezas: (() => void)[] = [];
  private temporizador: ReturnType<typeof setTimeout> | undefined;

  readonly usuario = this._usuario.asReadonly();
  readonly token = this._token.asReadonly();
  /** Instante de expiração (ms desde a época) ou `null` sem sessão. */
  readonly expiraEm = this._expiraEm.asReadonly();
  readonly autenticado = computed(() => this._token() !== null);
  /** `true` quando a última sessão terminou por expiração; consumido pela tela de login. */
  readonly sessaoExpirada = this._sessaoExpirada.asReadonly();

  constructor() {
    this.restaurar();
  }

  entrar(credenciais: LoginRequest): Observable<TokenResponse> {
    return this.api
      .post<TokenResponse, LoginRequest>('auth/login', credenciais)
      .pipe(tap((resposta) => this.iniciarSessao(resposta)));
  }

  /** Encerra a sessão, limpa o estado em memória e navega ao login. */
  sair(opcoes: OpcoesSair = {}): void {
    this.encerrar();
    this._sessaoExpirada.set(opcoes.expirada === true);
    void this.router.navigate(['/login'], {
      queryParams: opcoes.retorno ? { retorno: opcoes.retorno } : {},
    });
  }

  /** Marca o aviso de sessão expirada como já exibido. */
  consumirAvisoExpiracao(): void {
    this._sessaoExpirada.set(false);
  }

  /** Ponto de extensão: registra limpezas de estado em memória executadas no logout. */
  registrarLimpeza(limpeza: () => void): void {
    this.limpezas.push(limpeza);
  }

  private iniciarSessao(resposta: TokenResponse): void {
    const expiraEm =
      this.expiracaoDoJwt(resposta.accessToken) ?? Date.now() + resposta.expiraEm * 1000;
    this.aplicar({ token: resposta.accessToken, usuario: resposta.usuario, expiraEm });
    this._sessaoExpirada.set(false);
    this.gravar();
  }

  private aplicar(sessao: SessaoArmazenada): void {
    this._token.set(sessao.token);
    this._usuario.set(sessao.usuario);
    this._expiraEm.set(sessao.expiraEm);
    this.agendarExpiracao(sessao.expiraEm);
  }

  private encerrar(): void {
    clearTimeout(this.temporizador);
    this.temporizador = undefined;
    this._token.set(null);
    this._usuario.set(null);
    this._expiraEm.set(null);
    try {
      sessionStorage.removeItem(CHAVE_SESSAO);
    } catch {
      // Armazenamento indisponível: a sessão em memória já foi limpa.
    }
    for (const limpar of this.limpezas) limpar();
  }

  private agendarExpiracao(expiraEm: number): void {
    clearTimeout(this.temporizador);
    const atraso = Math.min(Math.max(expiraEm - Date.now(), 0), ATRASO_MAXIMO_MS);
    this.temporizador = setTimeout(() => {
      if (Date.now() >= expiraEm) this.sair({ expirada: true });
      else this.agendarExpiracao(expiraEm);
    }, atraso);
  }

  private gravar(): void {
    const token = this._token();
    const usuario = this._usuario();
    const expiraEm = this._expiraEm();
    if (!token || !usuario || expiraEm === null) return;
    try {
      sessionStorage.setItem(
        CHAVE_SESSAO,
        JSON.stringify({ token, usuario, expiraEm } satisfies SessaoArmazenada),
      );
    } catch {
      // Sem armazenamento (modo privado/cota): a sessão vale só em memória.
    }
  }

  private restaurar(): void {
    let sessao: SessaoArmazenada | null;
    try {
      const bruto = sessionStorage.getItem(CHAVE_SESSAO);
      sessao = bruto ? this.validar(JSON.parse(bruto)) : null;
    } catch {
      sessao = null;
    }
    if (sessao && sessao.expiraEm > Date.now()) {
      this.aplicar(sessao);
    } else if (sessao) {
      this.encerrar();
      this._sessaoExpirada.set(true);
    }
  }

  private validar(valor: unknown): SessaoArmazenada | null {
    if (typeof valor !== 'object' || valor === null) return null;
    const { token, usuario, expiraEm } = valor as Partial<SessaoArmazenada>;
    if (
      typeof token !== 'string' ||
      typeof expiraEm !== 'number' ||
      typeof usuario?.email !== 'string' ||
      typeof usuario.id !== 'number'
    ) {
      return null;
    }
    return { token, usuario, expiraEm };
  }

  /** Lê o `exp` (segundos) do payload do JWT; `null` se ilegível. Apenas para UX. */
  private expiracaoDoJwt(token: string): number | null {
    try {
      const payload = token.split('.')[1];
      if (!payload) return null;
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const json = JSON.parse(atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '='))) as {
        exp?: unknown;
      };
      return typeof json.exp === 'number' ? json.exp * 1000 : null;
    } catch {
      return null;
    }
  }
}
