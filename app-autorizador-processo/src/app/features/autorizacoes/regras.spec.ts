import { descreverRegras } from './regras';

describe('descreverRegras', () => {
  it('descreve dois sexos', () => {
    expect(descreverRegras({ sexos: ['M', 'F'], faixasEtarias: ['ADOLESCENTE', 'ADULTO'] })).toBe(
      'Masculino e Feminino · Adolescente, Adulto',
    );
  });

  it('descreve um sexo', () => {
    expect(descreverRegras({ sexos: ['F'], faixasEtarias: ['ADULTO'] })).toBe('Feminino · Adulto');
  });

  it('informa ausência de faixas etárias', () => {
    expect(descreverRegras({ sexos: ['M'], faixasEtarias: [] })).toBe('Sem faixas etárias cadastradas');
  });
});
