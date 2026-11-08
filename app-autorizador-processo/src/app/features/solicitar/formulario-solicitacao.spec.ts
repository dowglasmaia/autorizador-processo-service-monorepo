import { criarFormularioSolicitacao } from './formulario-solicitacao';

describe('formulário de solicitação', () => {
  it('começa inválido com os três campos obrigatórios', () => {
    const f = criarFormularioSolicitacao();
    expect(f.valid).toBe(false);
    expect(f.controls.procedimento.hasError('required')).toBe(true);
    expect(f.controls.idade.hasError('required')).toBe(true);
    expect(f.controls.sexo.hasError('required')).toBe(true);
  });

  it('aceita valores válidos', () => {
    const f = criarFormularioSolicitacao();
    f.setValue({ procedimento: 4567, idade: 20, sexo: 'F' });
    expect(f.valid).toBe(true);
  });

  it.each([0, 150])('aceita a idade limite %i', (idade) => {
    const f = criarFormularioSolicitacao();
    f.controls.idade.setValue(idade);
    expect(f.controls.idade.valid).toBe(true);
  });

  it('rejeita idade fora de 0..150 e não inteira', () => {
    const f = criarFormularioSolicitacao();
    f.controls.idade.setValue(-1);
    expect(f.controls.idade.hasError('min')).toBe(true);
    f.controls.idade.setValue(151);
    expect(f.controls.idade.hasError('max')).toBe(true);
    f.controls.idade.setValue(20.5);
    expect(f.controls.idade.hasError('inteiro')).toBe(true);
  });

  it('rejeita sexo diferente de M ou F', () => {
    const f = criarFormularioSolicitacao();
    f.controls.sexo.setValue('X');
    expect(f.controls.sexo.hasError('pattern')).toBe(true);
  });

  it('reset volta ao estado inicial', () => {
    const f = criarFormularioSolicitacao();
    f.setValue({ procedimento: 1, idade: 5, sexo: 'M' });
    f.reset();
    expect(f.getRawValue()).toEqual({ procedimento: null, idade: null, sexo: '' });
  });
});
