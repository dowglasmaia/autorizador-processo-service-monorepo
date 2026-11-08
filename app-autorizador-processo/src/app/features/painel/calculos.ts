/** Aprovação da sessão: "—" sem solicitações; senão o percentual inteiro de concedidas (1/3 → "33%"). */
export function calcularAprovacao(concedidas: number, negadas: number): string {
  const total = concedidas + negadas;
  if (total <= 0) return '—';
  return `${Math.round((concedidas / total) * 100)}%`;
}

/** Data por extenso em pt-BR, ex.: "domingo, 8 de novembro de 2026" (sem depender do locale global). */
export function formatarDataExtenso(data: Date): string {
  return new Intl.DateTimeFormat('pt-BR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(data);
}
