import {
  ambosParaSexos,
  alternarFaixa,
  validarQuantidadePeriodo,
} from './regras-formulario';

describe('alternarFaixa', () => {
  it('adiciona e remove mantendo a ordem do contrato', () => {
    expect(alternarFaixa([], 'IDOSO')).toEqual(['IDOSO']);
    expect(alternarFaixa(['IDOSO'], 'CRIANCA')).toEqual(['CRIANCA', 'IDOSO']);
    expect(alternarFaixa(['CRIANCA', 'IDOSO'], 'CRIANCA')).toEqual(['IDOSO']);
    expect(alternarFaixa(['ADULTO'], 'ADULTO')).toEqual([]);
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
