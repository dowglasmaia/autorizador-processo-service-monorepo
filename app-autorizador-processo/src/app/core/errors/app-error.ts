import { MENSAGENS, mensagemDoErro } from '../i18n/mensagens';
import type { FieldViolation, ProblemCode, ProblemDetail } from '../../shared/api';

export type AppErrorKind = 'validacao' | 'negocio' | 'nao-encontrado' | 'rede' | 'servidor';

export interface AppError {
  readonly kind: AppErrorKind;
  readonly code?: ProblemCode;
  readonly detail: string;
  /** Violações por campo (`errors[]` do problem+json), indexadas pelo nome do campo. */
  readonly campos?: Readonly<Record<string, string>>;
  readonly status?: number;
  /** Segundos de espera informados por `Retry-After` em respostas `429`. */
  readonly retryAfter?: number;
}

export function isAppError(valor: unknown): valor is AppError {
  return (
    typeof valor === 'object' &&
    valor !== null &&
    'kind' in valor &&
    'detail' in valor &&
    typeof (valor as { detail: unknown }).detail === 'string'
  );
}

const KIND_POR_CODE: Readonly<Record<ProblemCode, AppErrorKind>> = {
  VALIDACAO_INVALIDA: 'validacao',
  CORPO_INVALIDO: 'validacao',
  PARAMETRO_INVALIDO: 'validacao',
  PROCEDIMENTO_DUPLICADO: 'negocio',
  AUTORIZACAO_NEGADA: 'negocio',
  PROCEDIMENTO_NAO_ENCONTRADO: 'nao-encontrado',
  ERRO_INTERNO: 'servidor',
  CREDENCIAIS_INVALIDAS: 'negocio',
  NAO_AUTENTICADO: 'negocio',
  SESSAO_EXPIRADA: 'negocio',
  ACESSO_NEGADO: 'negocio',
  MUITAS_TENTATIVAS: 'negocio',
};

export const MENSAGEM_SERVIDOR = MENSAGENS.erros.servidor;
export const MENSAGEM_REDE = MENSAGENS.erros.rede;

/** Reserva por status HTTP quando o `code` é desconhecido ou ausente. */
function kindPorStatus(status: number): AppErrorKind {
  if (status === 400) return 'validacao';
  if (status === 404) return 'nao-encontrado';
  if (status === 401 || status === 403 || status === 429) return 'negocio';
  return 'servidor';
}

function camposDe(
  errors: readonly FieldViolation[] | undefined,
): Record<string, string> | undefined {
  if (!errors?.length) return undefined;
  return Object.fromEntries(errors.map((e) => [e.campo, e.mensagem]));
}

/** Converte um `ProblemDetail` em `AppError` decidindo por `code` (nunca por texto) e, na falta dele, pelo status. */
export function mapProblem(problem: Partial<ProblemDetail>, status: number): AppError {
  const code = problem.code && problem.code in KIND_POR_CODE ? problem.code : undefined;
  const kind = code ? KIND_POR_CODE[code] : kindPorStatus(status);
  // Erros de servidor nunca expõem detalhes técnicos.
  const detail =
    kind === 'servidor'
      ? MENSAGEM_SERVIDOR
      : (problem.detail ?? mensagemDoErro(problem.code));
  const campos = kind === 'servidor' ? undefined : camposDe(problem.errors);
  return { kind, code, detail, status, ...(campos ? { campos } : {}) };
}
