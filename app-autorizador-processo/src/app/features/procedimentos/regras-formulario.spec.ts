import {
  ambosParaSexos,
  faixaParaIdades,
  validarFaixa,
  validarQuantidadePeriodo,
} from './regras-formulario';

describe('faixaParaIdades', () => {
  it('expande a faixa inclusiva', () => {
    expect(faixaParaIdades(18, 20)).toEqual([18, 19, 20]);
    expect(faixaParaIdades(5, 5)).toEqual([5]);
  });
  it('aceita os limites 0 e 150', () => {
    expect(faixaParaIdades(0, 0)).toEqual([0]);
    expect(faixaParaIdades(0, 150)).toHaveLength(151);
  });
  it('devolve vazio para faixa invertida, fora do limite ou incompleta', () => {
    expect(faixaParaIdades(20, 18)).toEqual([]);
    expect(faixaParaIdades(-1, 5)).toEqual([]);
    expect(faixaParaIdades(0, 151)).toEqual([]);
    expect(faixaParaIdades(10, null)).toEqual([]);
    expect(faixaParaIdades(null, null)).toEqual([]);
  });
});

describe('validarFaixa', () => {
  it('aceita vazia e válida', () => {
    expect(validarFaixa(null, null)).toBeNull();
    expect(validarFaixa(0, 150)).toBeNull();
  });
  it('rejeita incompleta, invertida, fora do limite e fracionária', () => {
    expect(validarFaixa(1, null)).toContain('inicial e a final');
    expect(validarFaixa(20, 18)).toContain('maior');
    expect(validarFaixa(0, 151)).toContain('entre 0 e 150');
    expect(validarFaixa(-1, 10)).toContain('entre 0 e 150');
    expect(validarFaixa(1.5, 3)).toContain('inteiras');
  });
});

describe('ambosParaSexos', () => {
  it('mapeia Ambos para M e F', () => {
    expect(ambosParaSexos(true)).toEqual(['M', 'F']);
    expect(ambosParaSexos(false)).toEqual([]);
  });
});

describe('validarQuantidadePeriodo', () => {
  it('aceita os dois ou nenhum', () => {
    expect(validarQuantidadePeriodo(null, null)).toEqual({});
    expect(validarQuantidadePeriodo(2, 'MES')).toEqual({});
  });
  it('exige período quando há quantidade e vice-versa', () => {
    expect(validarQuantidadePeriodo(2, null).periodo).toBeDefined();
    expect(validarQuantidadePeriodo(null, 'ANO').quantidadeMaxima).toBeDefined();
  });
  it('rejeita quantidade menor que 1 ou fracionária', () => {
    expect(validarQuantidadePeriodo(0, 'MES').quantidadeMaxima).toBeDefined();
    expect(validarQuantidadePeriodo(1.5, 'MES').quantidadeMaxima).toBeDefined();
  });
});
