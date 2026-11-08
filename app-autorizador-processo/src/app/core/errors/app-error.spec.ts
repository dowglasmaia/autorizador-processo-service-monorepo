import { mapProblem, AppErrorKind, MENSAGEM_SERVIDOR } from './app-error';
import type { ProblemCode } from '../../shared/api';

// Record<ProblemCode, ...> faz o compilador exigir todos os códigos do contrato.
const ESPERADO: Record<ProblemCode, { status: number; kind: AppErrorKind }> = {
  VALIDACAO_INVALIDA: { status: 400, kind: 'validacao' },
  CORPO_INVALIDO: { status: 400, kind: 'validacao' },
  PARAMETRO_INVALIDO: { status: 400, kind: 'validacao' },
  PROCEDIMENTO_DUPLICADO: { status: 403, kind: 'negocio' },
  AUTORIZACAO_NEGADA: { status: 403, kind: 'negocio' },
  PROCEDIMENTO_NAO_ENCONTRADO: { status: 404, kind: 'nao-encontrado' },
  ERRO_INTERNO: { status: 500, kind: 'servidor' },
  CREDENCIAIS_INVALIDAS: { status: 401, kind: 'negocio' },
  NAO_AUTENTICADO: { status: 401, kind: 'negocio' },
  SESSAO_EXPIRADA: { status: 401, kind: 'negocio' },
  ACESSO_NEGADO: { status: 403, kind: 'negocio' },
  MUITAS_TENTATIVAS: { status: 429, kind: 'negocio' },
};

describe('mapProblem', () => {
  for (const [code, { status, kind }] of Object.entries(ESPERADO)) {
    it(`mapeia ${code} para kind ${kind}`, () => {
      const erro = mapProblem({ code: code as ProblemCode, detail: 'detalhe' }, status);
      expect(erro.kind).toBe(kind);
      expect(erro.code).toBe(code);
    });
  }

  it('decide pelo code e não pelo status', () => {
    expect(mapProblem({ code: 'PROCEDIMENTO_DUPLICADO' }, 500).kind).toBe('negocio');
  });

  it('usa o status como reserva quando o code é desconhecido', () => {
    expect(mapProblem({ code: 'OUTRO' as ProblemCode }, 404).kind).toBe('nao-encontrado');
    expect(mapProblem({}, 400).kind).toBe('validacao');
    expect(mapProblem({}, 502).kind).toBe('servidor');
  });

  it('não expõe o detalhe do backend em erros de servidor', () => {
    const erro = mapProblem({ code: 'ERRO_INTERNO', detail: 'NullPointerException em X' }, 500);
    expect(erro.detail).toBe(MENSAGEM_SERVIDOR);
  });

  it('converte errors[] em campos', () => {
    const erro = mapProblem(
      {
        code: 'VALIDACAO_INVALIDA',
        errors: [
          { campo: 'codigo', mensagem: 'obrigatório' },
          { campo: 'nome', mensagem: 'muito longo' },
        ],
      },
      400,
    );
    expect(erro.campos).toEqual({ codigo: 'obrigatório', nome: 'muito longo' });
  });
});
