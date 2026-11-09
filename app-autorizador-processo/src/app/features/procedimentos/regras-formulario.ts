import { FAIXAS_ETARIAS, type FaixaEtaria, type Periodo } from './rotulos';

export type SexoCodigo = 'M' | 'F';

/** Alterna a faixa na seleção e devolve a lista na ordem do contrato (da mais nova para a mais velha). */
export function alternarFaixa(
  atuais: readonly FaixaEtaria[],
  faixa: FaixaEtaria,
): FaixaEtaria[] {
  const conjunto = new Set(atuais);
  if (conjunto.has(faixa)) conjunto.delete(faixa);
  else conjunto.add(faixa);
  return FAIXAS_ETARIAS.filter((f) => conjunto.has(f));
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
