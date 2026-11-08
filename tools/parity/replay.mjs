#!/usr/bin/env node
// Replay dos golden cases (B01-B17 + adicionais) contra a versão original ou a v1.
// Node >= 20, sem dependências. Uso:
//   node tools/parity/replay.mjs --target original --base-url http://localhost:18080 [--only grupo]
//   node tools/parity/replay.mjs --target v1 --base-url http://localhost:8080 --email e --senha s

import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const GRUPOS = ['procedimento', 'autorizacao', 'infra'];
const AJUDA = `Uso: node tools/parity/replay.mjs --target original|v1 --base-url <url> [--only procedimento|autorizacao|infra] [--email <e> --senha <s>]
  --target    original (sem autenticação) ou v1 (login em /api/v1/auth/login, Bearer)
  --base-url  URL base, ex.: http://localhost:18080
  --only      executa só um grupo de casos
  --email/--senha  credenciais da v1 (ou PARITY_EMAIL / PARITY_SENHA)`;

// ---------- argumentos ----------
function lerArgs(argv) {
  const permitidos = new Set(['target', 'base-url', 'only', 'email', 'senha']);
  const args = {};
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a === '--help' || a === '-h') { console.log(AJUDA); process.exit(0); }
    if (!a.startsWith('--')) erroUso(`argumento inesperado: ${a}`);
    let [chave, valor] = a.slice(2).split(/=(.*)/s, 2);
    if (!permitidos.has(chave)) erroUso(`opção desconhecida: --${chave}`);
    if (valor === undefined) valor = argv[++i];
    if (valor === undefined || valor.startsWith('--')) erroUso(`a opção --${chave} exige um valor`);
    args[chave] = valor;
  }
  if (!['original', 'v1'].includes(args.target)) erroUso('--target deve ser "original" ou "v1"');
  if (!args['base-url']) erroUso('--base-url é obrigatório');
  try {
    const u = new URL(args['base-url']);
    if (!/^https?:$/.test(u.protocol)) throw new Error();
  } catch { erroUso(`--base-url inválida: ${args['base-url']}`); }
  if (args.only && !GRUPOS.includes(args.only)) erroUso(`--only deve ser um de: ${GRUPOS.join(', ')}`);
  args.email ??= process.env.PARITY_EMAIL;
  args.senha ??= process.env.PARITY_SENHA;
  if (args.target === 'v1' && (!args.email || !args.senha)) {
    erroUso('--target v1 exige --email e --senha (ou PARITY_EMAIL e PARITY_SENHA)');
  }
  args.baseUrl = args['base-url'].replace(/\/+$/, '');
  return args;
}
function erroUso(msg) { console.error(`Erro: ${msg}\n\n${AJUDA}`); process.exit(2); }

// ---------- caminhos ----------
// Segmentos: nome | nome[] | [] | nome[chave=valor]. '$' é a raiz; '$.length' o tamanho do array raiz.
function resolver(raiz, caminho) {
  if (caminho === '$') return { multi: false, valores: [raiz] };
  if (caminho === '$.length') return { multi: false, valores: [Array.isArray(raiz) ? raiz.length : undefined] };
  let atual = [raiz];
  let multi = false;
  for (const seg of caminho.split('.')) {
    const m = /^([^[\]]*)(?:\[(.*)\])?$/.exec(seg);
    if (!m) return { multi, valores: [] };
    const [, nome, filtro] = m;
    const proximo = [];
    for (const v of atual) {
      let alvo = v;
      if (nome) alvo = v != null && typeof v === 'object' ? v[nome] : undefined;
      if (filtro === undefined) { if (alvo !== undefined) proximo.push(alvo); continue; }
      multi = true;
      if (!Array.isArray(alvo)) continue;
      if (filtro === '') proximo.push(...alvo);
      else {
        const [k, val] = filtro.split('=');
        proximo.push(...alvo.filter((it) => it != null && String(it[k]) === val));
      }
    }
    atual = proximo;
  }
  if (multi) atual = atual.flatMap((v) => (Array.isArray(v) ? v : [v]));
  return { multi, valores: atual };
}
const valorDe = (r) => (r.multi ? r.valores : r.valores[0]);
const ordenar = (l) => [...l].map((x) => JSON.stringify(x)).sort();
const igual = (a, b) => JSON.stringify(a) === JSON.stringify(b);
const mesmoConjunto = (a, b) => Array.isArray(a) && Array.isArray(b) && igual(ordenar(a), ordenar(b));
const resumir = (v) => { const s = JSON.stringify(v); return s && s.length > 80 ? s.slice(0, 77) + '...' : s; };

// ---------- comparação ----------
function comparar(esp, res) {
  const falhas = [];
  const { status, texto, json, cabecalhos } = res;
  if (esp.status !== undefined && status !== esp.status) falhas.push(`status ${status} ≠ ${esp.status}`);
  if (esp.statusAnyOf && !esp.statusAnyOf.includes(status)) falhas.push(`status ${status} fora de [${esp.statusAnyOf}]`);
  if (esp.corpoVazio && texto.trim() !== '') falhas.push(`corpo deveria estar vazio: ${resumir(texto)}`);
  const alvo = json ?? undefined;
  const ler = (c) => (c === '$' ? { multi: false, valores: [texto] } : resolver(alvo, c));

  for (const [c, v] of Object.entries(esp.estavel ?? {})) {
    const r = resolver(alvo, c);
    const obtido = valorDe(r);
    if (!igual(obtido, v)) falhas.push(`${c}: obtido ${resumir(obtido)} ≠ ${resumir(v)}`);
  }
  for (const [c, v] of Object.entries(esp.conjuntos ?? {})) {
    const obtido = valorDe(resolver(alvo, c));
    if (!mesmoConjunto(obtido, v)) falhas.push(`${c}: conjunto obtido ${resumir(obtido)} ≠ ${resumir(v)}`);
  }
  for (const [c, v] of Object.entries(esp.conjuntosOrdenados ?? {})) {
    const obtido = valorDe(resolver(alvo, c));
    if (!igual(obtido, v)) falhas.push(`${c}: lista obtida ${resumir(obtido)} ≠ ${resumir(v)}`);
  }
  for (const [c, trecho] of Object.entries(esp.contem ?? {})) {
    const r = ler(c);
    if (!r.valores.some((x) => typeof x === 'string' && x.includes(trecho))) {
      falhas.push(`${c}: não contém "${trecho}" (obtido ${resumir(valorDe(r))})`);
    }
  }
  for (const c of esp.ausentes ?? []) {
    if (resolver(alvo, c).valores.length > 0) falhas.push(`${c}: deveria estar ausente`);
  }
  if (esp.codeAnyOf && !esp.codeAnyOf.includes(alvo?.code)) {
    falhas.push(`code ${resumir(alvo?.code)} fora de [${esp.codeAnyOf}]`);
  }
  for (const h of esp.cabecalhosPresentes ?? []) {
    if (!cabecalhos.has(h)) falhas.push(`cabeçalho ausente: ${h}`);
  }
  return falhas;
}

function descreverEsperado(esp) {
  if (esp.status !== undefined) return String(esp.status);
  if (esp.statusAnyOf) return esp.statusAnyOf.join('|');
  return '?';
}

// ---------- HTTP ----------
async function chamar(baseUrl, req, token) {
  const url = new URL(baseUrl + req.caminho);
  for (const [k, v] of Object.entries(req.query ?? {})) url.searchParams.set(k, v);
  const cab = { ...(req.cabecalhos ?? {}) };
  if (token && !req.semAutenticacao) cab.Authorization = `Bearer ${token}`;
  let corpo;
  if (req.corpoBruto !== undefined) corpo = req.corpoBruto;
  else if (req.corpo !== undefined) {
    corpo = JSON.stringify(req.corpo);
    cab['Content-Type'] ??= 'application/json';
  }
  const r = await fetch(url, { method: req.metodo, headers: cab, body: corpo, signal: AbortSignal.timeout(15000) });
  const texto = await r.text();
  let json = null;
  try { json = texto ? JSON.parse(texto) : null; } catch { /* corpo não é JSON */ }
  return { status: r.status, texto, json, cabecalhos: r.headers };
}

async function login(args) {
  const r = await chamar(args.baseUrl, {
    metodo: 'POST', caminho: '/api/v1/auth/login', semAutenticacao: true,
    corpo: { email: args.email, senha: args.senha },
  });
  const token = r.json?.accessToken ?? r.json?.token ?? r.json?.access_token;
  if (r.status !== 200 || !token) {
    throw new Error(`login na v1 falhou (status ${r.status}): ${resumir(r.texto)}`);
  }
  return token;
}

// ---------- principal ----------
async function main() {
  const args = lerArgs(process.argv.slice(2));
  const dir = dirname(fileURLToPath(import.meta.url));
  const base = JSON.parse(readFileSync(join(dir, 'cases.json'), 'utf8'));
  const v1 = args.target === 'v1';

  let casos = [...base.casos, ...(v1 ? base.adicionais ?? [] : [])];
  if (args.only) casos = casos.filter((c) => c.grupo === args.only);
  casos.sort((a, b) => a.ordem - b.ordem);

  let token = null;
  try {
    if (v1) token = await login(args);
    else await chamar(args.baseUrl, { metodo: 'GET', caminho: '/procedimentos' }).catch((e) => { throw e; });
  } catch (e) {
    console.error(`Erro: não foi possível usar ${args.baseUrl}: ${e.message}`);
    process.exit(2);
  }

  let baseSuja = false;
  // Setup: confere a semente (a original não tem rota de remoção; a base precisa estar limpa).
  const listagem = v1 ? '/api/v1/procedimentos' : '/procedimentos';
  try {
    const r = await chamar(args.baseUrl, { metodo: 'GET', caminho: listagem }, token);
    const itens = v1 ? r.json?.itens : r.json;
    const numeros = Array.isArray(itens) ? itens.map((i) => i.numProcedimento) : null;
    if (r.status !== 200 || !numeros || !mesmoConjunto(numeros, base.semente.numeros)) {
      baseSuja = true;
      console.warn(`AVISO: a base não está limpa (esperado só a semente ${base.semente.numeros}; ` +
        `GET ${listagem} -> ${r.status} ${resumir(numeros ?? r.texto)}).`);
      console.warn(v1
        ? '       Remova os procedimentos de teste (9999, 9998) ou recrie a base.'
        : '       A original usa H2 em memória e não tem remoção: reinicie o contêiner e rode de novo.');
    }
  } catch (e) {
    console.error(`Erro ao conferir a semente: ${e.message}`);
    process.exit(2);
  }

  const linhas = [];
  for (const caso of casos) {
    const def = v1 ? caso.mapeamentoV1 : caso.original;
    let esperado = descreverEsperado(def.resposta);
    let obtido = '-';
    let falhas;
    try {
      const res = await chamar(args.baseUrl, def.requisicao, token);
      obtido = String(res.status);
      falhas = comparar(def.resposta, res);
    } catch (e) {
      falhas = [`erro de requisição: ${e.message}`];
      obtido = 'ERRO';
    }
    linhas.push({ id: caso.id, esperado, obtido, ok: falhas.length === 0, falhas, div: caso.divergencia });
  }

  // Limpeza própria (v1): remove os números criados pelos casos, ignorando falhas (a rota pode não existir).
  if (v1) {
    for (const n of [9999, 9998]) {
      await chamar(args.baseUrl, { metodo: 'DELETE', caminho: `/api/v1/procedimentos/${n}` }, token).catch(() => {});
    }
  }

  // ---------- tabela ----------
  const cab = ['caso', 'esperado', 'obtido', 'resultado'];
  const dados = linhas.map((l) => [l.id + (l.div ? ` (${l.div})` : ''), l.esperado, l.obtido, l.ok ? 'OK' : 'FALHA']);
  const larg = cab.map((c, i) => Math.max(c.length, ...dados.map((d) => d[i].length)));
  const fmt = (l) => l.map((c, i) => c.padEnd(larg[i])).join(' · ');
  console.log(`\nReplay contra ${args.target} (${args.baseUrl})${args.only ? ` · grupo ${args.only}` : ''}\n`);
  console.log(fmt(cab));
  console.log(larg.map((n) => '-'.repeat(n)).join('-+-'));
  dados.forEach((d) => console.log(fmt(d)));
  const falhos = linhas.filter((l) => !l.ok);
  for (const l of falhos) {
    console.log(`\n${l.id}:`);
    l.falhas.forEach((f) => console.log(`  - ${f}`));
  }
  console.log(`\n${linhas.length - falhos.length}/${linhas.length} OK` +
    (baseSuja ? ' · base fora do estado inicial (resultado não confiável)' : ''));
  process.exit(falhos.length > 0 || baseSuja ? 1 : 0);
}

main().catch((e) => { console.error(`Erro inesperado: ${e.stack ?? e}`); process.exit(2); });
