import { descreverRegras } from './regras';

describe('descreverRegras', () => {
  it('descreve dois sexos', () => {
    expect(descreverRegras({ sexos: ['M', 'F'], idades: [20, 30] })).toBe(
      'Masculino e Feminino · 20, 30 anos',
    );
  });

  it('descreve um sexo', () => {
    expect(descreverRegras({ sexos: ['F'], idades: [25] })).toBe('Feminino · 25 anos');
  });

  it('informa ausência de idades', () => {
    expect(descreverRegras({ sexos: ['M'], idades: [] })).toBe('Sem idades cadastradas');
  });
});
