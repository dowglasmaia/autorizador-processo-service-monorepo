export const ROTA_PADRAO = '/painel';

/**
 * Valida o destino pós-login vindo da URL. Aceita só caminhos internos (`/x`): rejeita
 * `//host`, URLs com esquema (`http:`, `javascript:`), barra invertida, caracteres de controle
 * e o próprio `/login`. Qualquer outra coisa cai no painel.
 */
export function retornoSeguro(url: string | null | undefined): string {
  if (typeof url !== 'string' || url.length === 0) return ROTA_PADRAO;
  if (!url.startsWith('/') || url.startsWith('//')) return ROTA_PADRAO;
  // eslint-disable-next-line no-control-regex -- rejeita caracteres de controle de propósito
  if (url.includes('\\') || /[\u0000-\u001f\u007f]/.test(url)) return ROTA_PADRAO;
  const caminho = url.split(/[?#]/)[0];
  if (caminho === '/login' || caminho.startsWith('/login/')) return ROTA_PADRAO;
  return url;
}
