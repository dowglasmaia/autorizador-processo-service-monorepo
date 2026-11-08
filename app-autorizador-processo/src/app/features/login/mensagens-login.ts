import { AppError } from '../../core/errors/app-error';

export const MENSAGEM_CREDENCIAIS = 'E-mail ou senha incorretos.';
export const MENSAGEM_SESSAO_EXPIRADA = 'Sua sessão expirou. Entre novamente para continuar.';

export type ResultadoErroLogin =
  /** Mensagem global exibida no formulário (`role="alert"`). */
  | { readonly tipo: 'alerta'; readonly mensagem: string }
  /** Erros por campo vindos de `400`. */
  | { readonly tipo: 'campos'; readonly campos: Readonly<Record<string, string>> }
  /** Falha de rede/servidor: abre o modal com "Tentar novamente". */
  | { readonly tipo: 'modal'; readonly mensagem: string };

export function mensagemMuitasTentativas(retryAfter?: number): string {
  if (retryAfter === undefined) return 'Muitas tentativas. Aguarde um instante e tente novamente.';
  if (retryAfter < 60) {
    return `Muitas tentativas. Tente novamente em ${retryAfter} ${retryAfter === 1 ? 'segundo' : 'segundos'}.`;
  }
  const minutos = Math.ceil(retryAfter / 60);
  return `Muitas tentativas. Tente novamente em ${minutos} ${minutos === 1 ? 'minuto' : 'minutos'}.`;
}

/** Decide a apresentação do erro de login por `code` (nunca por texto) e, na falta dele, por `kind`. */
export function classificarErroLogin(erro: AppError): ResultadoErroLogin {
  switch (erro.code) {
    case 'CREDENCIAIS_INVALIDAS':
      return { tipo: 'alerta', mensagem: MENSAGEM_CREDENCIAIS };
    case 'MUITAS_TENTATIVAS':
      return { tipo: 'alerta', mensagem: mensagemMuitasTentativas(erro.retryAfter) };
    default:
      break;
  }
  if (erro.status === 429) {
    return { tipo: 'alerta', mensagem: mensagemMuitasTentativas(erro.retryAfter) };
  }
  if (erro.kind === 'validacao') {
    return erro.campos && Object.keys(erro.campos).length > 0
      ? { tipo: 'campos', campos: erro.campos }
      : { tipo: 'alerta', mensagem: erro.detail };
  }
  if (erro.kind === 'rede' || erro.kind === 'servidor') {
    return { tipo: 'modal', mensagem: erro.detail };
  }
  return { tipo: 'alerta', mensagem: erro.detail };
}
