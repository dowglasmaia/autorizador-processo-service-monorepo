#!/usr/bin/env node
// Sondagens ativas de segurança (SPEC-41, etapa 29). Node >= 20, sem dependências.
// Uso: node sondagem.mjs --api URL --front URL --email E --senha S --jwt-secret SEGREDO [--email-inativo E2]
// Normalmente chamado por sondagem.sh, que sobe um Compose descartável. Sai com código 1 se algo falhar.
import { createHmac } from 'node:crypto';
import http from 'node:http';

const arg = (nome, padrao) => {
  const i = process.argv.indexOf(`--${nome}`);
  return i > -1 ? process.argv[i + 1] : padrao;
};
const API = arg('api', 'http://localhost:8080');
const FRONT = arg('front', 'http://localhost:4200');
const EMAIL = arg('email');
const SENHA = arg('senha');
const SEGREDO = arg('jwt-secret');
const EMAIL_INATIVO = arg('email-inativo');
const EMISSOR = 'autorizador-processo';
if (!EMAIL || !SENHA || !SEGREDO) {
  console.error('Informe --email, --senha e --jwt-secret.');
  process.exit(2);
}

const resultados = [];
const registrar = (id, descricao, ok, detalhe = '') => {
  resultados.push({ id, descricao, ok, detalhe });
  console.log(`${ok ? 'PASSOU' : 'FALHOU'}  ${id}  ${descricao}${detalhe ? `  [${detalhe}]` : ''}`);
};

const b64 = (o) => Buffer.from(typeof o === 'string' ? o : JSON.stringify(o)).toString('base64url');
const assinar = (header, payload, segredo, alg = 'sha256') =>
  `${b64(header)}.${b64(payload)}.${createHmac(alg, segredo).update(`${b64(header)}.${b64(payload)}`).digest('base64url')}`;

async function req(base, caminho, { metodo = 'GET', token, corpo, cabecalhos = {} } = {}) {
  const h = { ...cabecalhos };
  if (token) h.Authorization = `Bearer ${token}`;
  if (corpo !== undefined && !h['Content-Type']) h['Content-Type'] = 'application/json';
  const t0 = performance.now();
  const r = await fetch(base + caminho, {
    method: metodo,
    headers: h,
    body: corpo === undefined ? undefined : typeof corpo === 'string' ? corpo : JSON.stringify(corpo),
    redirect: 'manual',
  });
  const texto = await r.text();
  let json;
  try {
    json = JSON.parse(texto);
  } catch {
    /* corpo não JSON */
  }
  return { status: r.status, h: r.headers, texto, json, ms: performance.now() - t0 };
}
const login = (email, senha) => req(API, '/api/v1/auth/login', { metodo: 'POST', corpo: { email, senha } });
const ehProblem = (r) => (r.h.get('content-type') ?? '').includes('application/problem+json');
const mediana = (v) => [...v].sort((a, b) => a - b)[Math.floor(v.length / 2)];

// --- 0. Preparação: login válido e token de controle -------------------------------------------------
const ok = await login(EMAIL, SENHA);
if (ok.status !== 200) {
  console.error(`Login de preparação falhou (${ok.status}): ${ok.texto}`);
  process.exit(2);
}
const tokenValido = ok.json.accessToken;
const sub = String(ok.json.usuario.id);
const agora = Math.floor(Date.now() / 1000);
const claims = (extra = {}) => ({ sub, email: EMAIL, iss: EMISSOR, iat: agora, exp: agora + 3600, ...extra });
const HS256 = { alg: 'HS256', typ: 'JWT' };

// --- 2.1 JWT ----------------------------------------------------------------------------------------
const controle = await req(API, '/api/v1/auth/me', { token: assinar(HS256, claims(), SEGREDO) });
registrar('JWT-00', 'controle: token forjado corretamente (HS256, segredo, iss) é aceito', controle.status === 200, `status ${controle.status}`);

const partes = tokenValido.split('.');
const adulteradoPayload = `${partes[0]}.${b64(claims({ sub: '999999' }))}.${partes[2]}`;
const adulteradoAssinatura = `${partes[0]}.${partes[1]}.${partes[2].slice(0, -2)}${partes[2].endsWith('AA') ? 'BB' : 'AA'}`;
const semExp = claims();
delete semExp.exp;
const casosJwt = {
  'JWT-01 adulterado (payload alterado, assinatura original)': adulteradoPayload,
  'JWT-02 adulterado (assinatura alterada)': adulteradoAssinatura,
  'JWT-03 expirado': assinar(HS256, claims({ iat: agora - 7200, exp: agora - 3600 }), SEGREDO),
  'JWT-04 alg=none sem assinatura': `${b64({ alg: 'none', typ: 'JWT' })}.${b64(claims())}.`,
  'JWT-05 alg=none com assinatura': `${b64({ alg: 'none', typ: 'JWT' })}.${b64(claims())}.${partes[2]}`,
  'JWT-06 algoritmo trocado (HS512 com o mesmo segredo)': assinar({ alg: 'HS512', typ: 'JWT' }, claims(), SEGREDO, 'sha512'),
  'JWT-07 algoritmo trocado (HS384 com o mesmo segredo)': assinar({ alg: 'HS384', typ: 'JWT' }, claims(), SEGREDO, 'sha384'),
  'JWT-08 iss incorreto': assinar(HS256, claims({ iss: 'outro-emissor' }), SEGREDO),
  'JWT-09 assinado com outro segredo': assinar(HS256, claims(), 'outro-segredo-qualquer-com-mais-de-32-bytes-xyz'),
  'JWT-10 sem exp': assinar(HS256, semExp, SEGREDO),
  'JWT-11 lixo (não é JWT)': 'isto-nao-e-um-jwt',
};
for (const [nome, token] of Object.entries(casosJwt)) {
  const r = await req(API, '/api/v1/auth/me', { token });
  const [id, ...desc] = nome.split(' ');
  registrar(id, `${desc.join(' ')} → 401 em problem+json`, r.status === 401 && ehProblem(r), `status ${r.status}, ${r.h.get('content-type')}`);
}
const semToken = await req(API, '/api/v1/procedimentos');
registrar('JWT-12', 'rota de negócio sem token → 401 em problem+json', semToken.status === 401 && ehProblem(semToken), `status ${semToken.status}`);

// --- 2.2 Login: enumeração, tempo e limitador ---------------------------------------------------------
const rotulo = Math.random().toString(36).slice(2, 8);
const medidas = { inexistente: [], senhaErrada: [], inativo: [] };
const corpos = new Set();
const assinatura = (r) => `${r.status}|${r.json?.code}|${r.json?.title}|${r.json?.detail}`;
for (let i = 0; i < 4; i++) {
  const a = await login(`nao-existe-${rotulo}-${i}@exemplo.com`, 'senha-errada-123');
  const b = await login(EMAIL, `senha-errada-${i}`);
  medidas.inexistente.push(a.ms);
  medidas.senhaErrada.push(b.ms);
  corpos.add(assinatura(a));
  corpos.add(assinatura(b));
  if (EMAIL_INATIVO) {
    const c = await login(EMAIL_INATIVO, `senha-errada-${i}`);
    medidas.inativo.push(c.ms);
    corpos.add(assinatura(c));
  }
}
registrar('LOGIN-01', 'e-mail inexistente, senha errada e usuário inativo → mesma resposta (status, code, title, detail)',
  corpos.size === 1 && [...corpos][0].startsWith('401|CREDENCIAIS_INVALIDAS'), [...corpos].join(' // '));
const med = Object.fromEntries(Object.entries(medidas).filter(([, v]) => v.length).map(([k, v]) => [k, mediana(v)]));
const maior = Math.max(...Object.values(med));
const menor = Math.min(...Object.values(med));
registrar('LOGIN-02', 'tempo equivalente (medianas: razão ≤ 2 ou diferença ≤ 75 ms)',
  maior / menor <= 2 || maior - menor <= 75, Object.entries(med).map(([k, v]) => `${k}=${v.toFixed(0)}ms`).join(', '));
if (!EMAIL_INATIVO) registrar('LOGIN-03', 'usuário inativo coberto', false, 'passe --email-inativo');

const alvo = `limite-${rotulo}@exemplo.com`;
const falhas = [];
for (let i = 0; i < 5; i++) falhas.push((await login(alvo, 'senha-errada-x')).status);
const sexta = await login(alvo, 'senha-errada-x');
registrar('LOGIN-04', '5 falhas → 401; 6ª tentativa → 429 com Retry-After e problem+json',
  falhas.every((s) => s === 401) && sexta.status === 429 && Number(sexta.h.get('retry-after')) > 0 && ehProblem(sexta),
  `falhas ${falhas.join(',')}; 6ª ${sexta.status}, Retry-After ${sexta.h.get('retry-after')}`);
const outroEmail = await login(EMAIL, SENHA);
registrar('LOGIN-05', 'o bloqueio é por e-mail+IP: outro e-mail continua entrando', outroEmail.status === 200);

// --- 2.3 Exposição, erros, CORS, cabeçalhos, corpo com id ----------------------------------------------
for (const caminho of ['env', 'beans', 'heapdump', 'metrics', 'mappings', 'loggers']) {
  const viaFront = await req(FRONT, `/actuator/${caminho}`);
  const viaApi = await req(API, `/actuator/${caminho}`, { token: tokenValido });
  registrar(`ACT-${caminho}`, `/actuator/${caminho} → 404 (pelo Nginx e direto na API, até autenticado)`,
    viaFront.status === 404 && viaApi.status === 404, `front ${viaFront.status}, api ${viaApi.status}`);
}
const saude = await req(FRONT, '/actuator/health');
registrar('ACT-health', '/actuator/health público → 200 sem detalhes', saude.status === 200 && !/"db"|"components"/.test(saude.texto), `status ${saude.status}, corpo ${saude.texto.slice(0, 80)}`);

for (const caminho of ['/swagger-ui.html', '/v3/api-docs']) {
  const r = await req(API, caminho);
  registrar('DOC', `${caminho} fora do perfil local → 404 (sem revelar a rota)`, r.status === 404, `status ${r.status}`);
}

const VAZAMENTO = /Exception|stackTrace|\bat [\w.$]+\([\w.]+:\d+\)|org\.springframework|java\.lang|jakarta\.|com\.zaxxer|SQLState|PSQLException|autorizador\.[a-z]+\./i;
const falhasForcadas = {
  'JSON malformado': () => req(API, '/api/v1/procedimentos', { metodo: 'POST', token: tokenValido, corpo: '{"numProcedimento":' }),
  'parâmetro de tipo errado': () => req(API, '/api/v1/procedimentos/abc', { token: tokenValido }),
  'rota inexistente': () => req(API, '/api/v1/nao-existe', { token: tokenValido }),
  'método não permitido': () => req(API, '/api/v1/painel/resumo', { metodo: 'DELETE', token: tokenValido }),
  'tipo de mídia não suportado': () => req(API, '/api/v1/procedimentos', { metodo: 'POST', token: tokenValido, corpo: 'x=1', cabecalhos: { 'Content-Type': 'text/plain' } }),
  'sort inválido': () => req(API, '/api/v1/procedimentos?sort=campoInexistente,asc', { token: tokenValido }),
};
for (const [nome, f] of Object.entries(falhasForcadas)) {
  const r = await f();
  registrar('ERRO', `${nome} → 4xx sem stack trace, SQL ou nome de classe`, r.status >= 400 && r.status < 500 && !VAZAMENTO.test(r.texto), `status ${r.status}`);
}

const evil = 'http://evil.example';
const pre = await req(API, '/api/v1/procedimentos', { metodo: 'OPTIONS', cabecalhos: { Origin: evil, 'Access-Control-Request-Method': 'GET', 'Access-Control-Request-Headers': 'authorization' } });
const real = await req(API, '/api/v1/painel/resumo', { token: tokenValido, cabecalhos: { Origin: evil } });
registrar('CORS-01', 'Origin não permitida: preflight recusado e nenhuma resposta com Access-Control-Allow-Origin',
  !pre.h.get('access-control-allow-origin') && !real.h.get('access-control-allow-origin') && pre.status === 403,
  `preflight ${pre.status}, real ${real.status}, ACAO=${pre.h.get('access-control-allow-origin')}/${real.h.get('access-control-allow-origin')}`);
const permitida = await req(API, '/api/v1/procedimentos', { metodo: 'OPTIONS', cabecalhos: { Origin: 'http://localhost:4200', 'Access-Control-Request-Method': 'GET', 'Access-Control-Request-Headers': 'authorization' } });
registrar('CORS-02', 'Origin da allowlist: preflight aceito (controle), sem "*"',
  permitida.status === 200 && permitida.h.get('access-control-allow-origin') === 'http://localhost:4200', `ACAO=${permitida.h.get('access-control-allow-origin')}`);

const raiz = await req(FRONT, '/', { metodo: 'HEAD' });
const exigidos = ['x-content-type-options', 'referrer-policy', 'x-frame-options', 'content-security-policy'];
const faltando = exigidos.filter((c) => !raiz.h.get(c));
registrar('HDR-01', 'curl -I /: nosniff, Referrer-Policy, X-Frame-Options e CSP presentes', faltando.length === 0, faltando.length ? `faltando ${faltando}` : '');
registrar('HDR-02', 'curl -I /: Server sem número de versão', !/\d/.test(raiz.h.get('server') ?? ''), `Server: ${raiz.h.get('server')}`);
registrar('HDR-03', 'curl -I /: Permissions-Policy presente', !!raiz.h.get('permissions-policy'), `Permissions-Policy: ${raiz.h.get('permissions-policy')}`);
const apiAutenticada = await req(FRONT, '/api/v1/auth/me', { token: tokenValido });
registrar('HDR-04', 'API autenticada: Cache-Control com no-store', /no-store/.test(apiAutenticada.h.get('cache-control') ?? ''), `Cache-Control: ${apiAutenticada.h.get('cache-control')}`);

const numero = 800000 + Math.floor(Math.random() * 99999);
const comId = await req(API, '/api/v1/procedimentos', {
  metodo: 'POST', token: tokenValido,
  corpo: { id: 123456789, numProcedimento: numero, nome: 'Sondagem id', categoria: 'CONSULTAS' },
});
const idRetornado = comId.json?.id;
registrar('IN-01', 'corpo com "id" é ignorado (201 com id gerado ≠ 123456789) ou rejeitado (400)',
  (comId.status === 201 && idRetornado !== 123456789) || comId.status === 400, `status ${comId.status}, id ${idRetornado}`);

// --- 2.4 Injeção, XSS e limite de corpo -----------------------------------------------------------------
const cargas = ["x'); DROP TABLE procedimento;--", '<script>alert(1)</script>', '"><img src=x onerror=alert(1)>'];
let i = 0;
for (const nome of cargas) {
  const n = numero + 1 + i++;
  const criado = await req(API, '/api/v1/procedimentos', { metodo: 'POST', token: tokenValido, corpo: { numProcedimento: n, nome, categoria: 'CONSULTAS' } });
  const lido = await req(API, `/api/v1/procedimentos/${criado.json?.id}`, { token: tokenValido });
  const lista = await req(API, '/api/v1/procedimentos', { token: tokenValido });
  const tipo = lido.h.get('content-type') ?? '';
  registrar('INJ', `nome ${JSON.stringify(nome)} armazenado literalmente, servido como JSON (nunca HTML) e tabela intacta`,
    criado.status === 201 && lido.json?.nome === nome && tipo.includes('application/json') && !tipo.includes('html') &&
      lido.h.get('x-content-type-options') === 'nosniff' && lista.status === 200,
    `criado ${criado.status}, lido ${lido.status}, ${tipo}`);
}

const grande = JSON.stringify({ numProcedimento: 1, nome: 'x'.repeat(200_000), categoria: 'CONSULTAS' });
const g1 = await req(API, '/api/v1/procedimentos', { metodo: 'POST', token: tokenValido, corpo: grande });
registrar('BODY-01', 'corpo de ~200 KB direto na API → 413 em problem+json', g1.status === 413 && ehProblem(g1), `status ${g1.status}`);
const g2 = await req(FRONT, '/api/v1/procedimentos', { metodo: 'POST', token: tokenValido, corpo: grande });
registrar('BODY-02', 'corpo de ~200 KB pelo Nginx → 413', g2.status === 413, `status ${g2.status}`);
const g3 = await req(API, '/api/v1/auth/login', { metodo: 'POST', corpo: grande });
registrar('BODY-03', 'corpo grande no login (sem token) → 413', g3.status === 413, `status ${g3.status}`);

// Cabeçalho gigante: fetch limita o tamanho, então usa http cru.
const cabecalhoGrande = (porta) => new Promise((resolve) => {
  const r = http.request({ host: 'localhost', port: porta, path: '/api/v1/painel/resumo', headers: { 'X-Lixo': 'a'.repeat(40_000), Authorization: `Bearer ${tokenValido}` } },
    (res) => { res.resume(); resolve(res.statusCode); });
  r.on('error', () => resolve('conexão encerrada'));
  r.end();
});
const hApi = await cabecalhoGrande(new URL(API).port);
const hFront = await cabecalhoGrande(new URL(FRONT).port);
const recusado = (s) => s === 'conexão encerrada' || (typeof s === 'number' && s >= 400 && s !== 401);
registrar('HDR-05', 'cabeçalho de 40 KB recusado (API e Nginx; nunca 200)', recusado(hApi) && recusado(hFront), `api ${hApi}, front ${hFront}`);

// --- Por último: limit_req do Nginx no login (bloqueia o IP no Nginx por alguns segundos) --------------
const codigos = [];
let retryAfter = null;
for (let k = 0; k < 25; k++) {
  const r = await req(FRONT, '/api/v1/auth/login', { metodo: 'POST', corpo: { email: `rajada-${rotulo}-${k}@exemplo.com`, senha: 'x' } });
  codigos.push(r.status);
  if (r.status === 429) retryAfter ??= r.h.get('retry-after');
}
registrar('NGX-01', 'rajada de 25 logins pelo Nginx → 429 com Retry-After (limit_req por IP)',
  codigos.includes(429) && codigos[0] === 401 && !!retryAfter, `códigos ${codigos.join(',')}; Retry-After ${retryAfter}`);

const falhou = resultados.filter((r) => !r.ok);
console.log(`\n${resultados.length - falhou.length}/${resultados.length} sondagens passaram.`);
process.exit(falhou.length ? 1 : 0);
