import { describe, expect, it } from 'vitest';
import { MENSAGENS, mensagemDoErro } from './mensagens';

/** Todos os `code` do contrato (`ProblemDetail.code`). */
const CODES_DO_CONTRATO = [
  'VALIDACAO_INVALIDA',
  'CORPO_INVALIDO',
  'PARAMETRO_INVALIDO',
  'PROCEDIMENTO_DUPLICADO',
  'AUTORIZACAO_NEGADA',
  'PROCEDIMENTO_NAO_ENCONTRADO',
  'ERRO_INTERNO',
  'CREDENCIAIS_INVALIDAS',
  'NAO_AUTENTICADO',
  'SESSAO_EXPIRADA',
  'ACESSO_NEGADO',
  'MUITAS_TENTATIVAS',
];

describe('catálogo de mensagens', () => {
  it('todo code do contrato tem mensagem própria e não vazia', () => {
    expect(Object.keys(MENSAGENS.erros.porCode).sort()).toEqual([...CODES_DO_CONTRATO].sort());
    for (const code of CODES_DO_CONTRATO) {
      expect(mensagemDoErro(code).trim().length).toBeGreaterThan(0);
      expect(mensagemDoErro(code)).not.toBe(MENSAGENS.erros.generico);
    }
  });

  it('code desconhecido, ausente ou herdado do protótipo usa a mensagem genérica', () => {
    expect(mensagemDoErro('CODIGO_NOVO')).toBe(MENSAGENS.erros.generico);
    expect(mensagemDoErro(undefined)).toBe(MENSAGENS.erros.generico);
    expect(mensagemDoErro('toString')).toBe(MENSAGENS.erros.generico);
  });

  it('não contém jargão técnico proibido', () => {
    const texto = JSON.stringify(MENSAGENS);
    const proibidos = ['perfil ' + 'test', 'ná ' + 'Cadastrado', 'Integri' + 'dades'];
    for (const p of proibidos) expect(texto).not.toContain(p);
    expect(texto).not.toContain('API');
  });
});
