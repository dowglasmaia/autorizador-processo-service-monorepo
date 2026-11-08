import { FormControl, FormGroup, Validators } from '@angular/forms';

export const IDADE_MINIMA = 0;
export const IDADE_MAXIMA = 150;

export type SexoSolicitacao = 'M' | 'F';

export const OPCOES_SEXO = [
  { value: 'M', label: 'Masculino' },
  { value: 'F', label: 'Feminino' },
] as const;

export interface FormularioSolicitacao {
  /** Número do procedimento escolhido; `null` enquanto nada foi selecionado. */
  procedimento: FormControl<number | null>;
  /** Idade inteira entre 0 e 150; `null` enquanto vazia. */
  idade: FormControl<number | null>;
  /** `'M'`, `'F'` ou vazio enquanto nada foi escolhido. */
  sexo: FormControl<string>;
}

/** Validador: aceita somente números inteiros (campo vazio é tratado por `required`). */
function inteiro(controle: FormControl<number | null>): { inteiro: true } | null {
  const valor = controle.value;
  return valor === null || Number.isInteger(valor) ? null : { inteiro: true };
}

/** Cria o formulário tipado de solicitação: procedimento e sexo obrigatórios, idade inteira 0..150. */
export function criarFormularioSolicitacao(): FormGroup<FormularioSolicitacao> {
  return new FormGroup<FormularioSolicitacao>({
    procedimento: new FormControl<number | null>(null, [Validators.required]),
    idade: new FormControl<number | null>(null, {
      validators: [
        Validators.required,
        Validators.min(IDADE_MINIMA),
        Validators.max(IDADE_MAXIMA),
        (c) => inteiro(c as FormControl<number | null>),
      ],
    }),
    sexo: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^[MF]$/)],
    }),
  });
}
