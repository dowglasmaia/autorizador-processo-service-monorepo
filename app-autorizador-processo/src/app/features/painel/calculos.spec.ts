import { calcularAprovacao, formatarDataExtenso } from './calculos';

describe('calcularAprovacao', () => {
  it('retorna "—" sem solicitações', () => expect(calcularAprovacao(0, 0)).toBe('—'));
  it('retorna 100% com uma concedida e nenhuma negada', () =>
    expect(calcularAprovacao(1, 0)).toBe('100%'));
  it('retorna 0% só com negadas', () => expect(calcularAprovacao(0, 2)).toBe('0%'));
  it('arredonda 1/3 para 33%', () => expect(calcularAprovacao(1, 2)).toBe('33%'));
  it('arredonda 2/3 para 67%', () => expect(calcularAprovacao(2, 1)).toBe('67%'));
});

describe('formatarDataExtenso', () => {
  it('escreve a data em português', () => {
    const texto = formatarDataExtenso(new Date(2026, 10, 8));
    expect(texto).toContain('domingo');
    expect(texto).toContain('8 de novembro de 2026');
  });
});
