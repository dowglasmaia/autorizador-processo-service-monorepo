import type { Periodo } from './rotulos';

export const IDADE_MINIMA = 0;
export const IDADE_MAXIMA = 150;

export type SexoCodigo = 'M' | 'F';

/** Mensagem de erro da faixa de idade ou `null` quando válida (ambos vazios também é válido: nenhuma idade). */
export function validarFaixa(min: number | null, max: number | null): string | null {
  if (min === null && max === null) return null;
  if (min === null || max === null) return 'Informe a idade inicial e a final.';
  if (!Number.isInteger(min) || !Number.isInteger(max)) return 'Use idades inteiras.';
  if (min < IDADE_MINIMA || max > IDADE_MAXIMA) {
    return `As idades devem estar entre ${IDADE_MINIMA} e ${IDADE_MAXIMA}.`;
  }
  if (min > max) return 'A idade inicial não pode ser maior que a final.';
  return null;
}

/** Expande a faixa [min, max] em idades; faixa vazia, inválida ou invertida resulta em lista vazia. */
export function faixaParaIdades(min: number | null, max: number | null): number[] {
  if (min === null || max === null || validarFaixa(min, max) !== null) return [];
  return Array.from({ length: max - min + 1 }, (_, i) => min + i);
}

/** "Ambos" equivale a ["M", "F"], na ordem do contrato. */
export function ambosParaSexos(ambos: boolean): SexoCodigo[] {
  return ambos ? ['M', 'F'] : [];
}

/** `quantidadeMaxima` exige `periodo` e vice-versa; devolve a mensagem por campo. */
export function validarQuantidadePeriodo(
  quantidade: number | null,
  periodo: Periodo | null,
): { quantidadeMaxima?: string; periodo?: string } {
  const erros: { quantidadeMaxima?: string; periodo?: string } = {};
  if (quantidade !== null && !Number.isInteger(quantidade)) {
    erros.quantidadeMaxima = 'Use um número inteiro.';
  } else if (quantidade !== null && quantidade < 1) {
    erros.quantidadeMaxima = 'A quantidade deve ser de pelo menos 1.';
  } else if (quantidade !== null && periodo === null) {
    erros.periodo = 'Informe o período da quantidade máxima.';
  } else if (quantidade === null && periodo !== null) {
    erros.quantidadeMaxima = 'Informe a quantidade máxima do período.';
  }
  return erros;
}
