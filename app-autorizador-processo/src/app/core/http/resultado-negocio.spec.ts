import { of, throwError, lastValueFrom } from 'rxjs';
import { resultadoNegocio } from './resultado-negocio';
import type { AppError } from '../errors/app-error';

describe('resultadoNegocio', () => {
  it('envolve o sucesso em resultado ok', async () => {
    expect(await lastValueFrom(of(42).pipe(resultadoNegocio()))).toEqual({
      resultado: 'ok',
      valor: 42,
    });
  });

  it('AUTORIZACAO_NEGADA vira resultado negado sem exceção', async () => {
    const erro: AppError = { kind: 'negocio', code: 'AUTORIZACAO_NEGADA', detail: 'Negado' };
    const r = await lastValueFrom(throwError(() => erro).pipe(resultadoNegocio()));
    expect(r).toEqual({ resultado: 'negado', detail: 'Negado' });
  });

  it('propaga outros erros', async () => {
    const erro: AppError = { kind: 'servidor', detail: 'x' };
    await expect(lastValueFrom(throwError(() => erro).pipe(resultadoNegocio()))).rejects.toBe(erro);
  });
});
