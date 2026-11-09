import type { AutorizacaoDto } from '../../shared/api';
import { ROTULO_FAIXA } from '../procedimentos/rotulos';

const ROTULO_SEXO: Readonly<Record<string, string>> = { M: 'Masculino', F: 'Feminino' };

type ProcedimentoRegras = Pick<AutorizacaoDto['procedimento'], 'sexos' | 'faixasEtarias'>;

/** Texto das regras: "Masculino e Feminino · Criança, Adulto"; sem faixas, "Sem faixas etárias cadastradas" (RN9). */
export function descreverRegras(procedimento: ProcedimentoRegras): string {
  if (procedimento.faixasEtarias.length === 0) return 'Sem faixas etárias cadastradas';
  const sexos = ['M', 'F']
    .filter((s) => procedimento.sexos.some((x) => x.toUpperCase() === s))
    .map((s) => ROTULO_SEXO[s]);
  const faixas = procedimento.faixasEtarias.map((f) => ROTULO_FAIXA[f]).join(', ');
  return sexos.length === 0 ? faixas : `${sexos.join(' e ')} · ${faixas}`;
}
