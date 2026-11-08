import type { AutorizacaoDto } from '../../shared/api';

const ROTULO_SEXO: Readonly<Record<string, string>> = { M: 'Masculino', F: 'Feminino' };

type ProcedimentoRegras = Pick<AutorizacaoDto['procedimento'], 'sexos' | 'idades'>;

/** Texto das regras: "Masculino e Feminino · 20, 30 anos"; sem idades, "Sem idades cadastradas" (RN9). */
export function descreverRegras(procedimento: ProcedimentoRegras): string {
  if (procedimento.idades.length === 0) return 'Sem idades cadastradas';
  const sexos = ['M', 'F']
    .filter((s) => procedimento.sexos.some((x) => x.toUpperCase() === s))
    .map((s) => ROTULO_SEXO[s]);
  const idades = `${[...procedimento.idades].sort((a, b) => a - b).join(', ')} anos`;
  return sexos.length === 0 ? idades : `${sexos.join(' e ')} · ${idades}`;
}
