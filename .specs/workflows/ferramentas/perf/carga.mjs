#!/usr/bin/env node
// Executor de carga simples (SPEC-42). Node >= 20, sem dependências. Faz UM login e reutiliza o token (não aciona o
// limitador de login). Por endpoint: aquecimento, depois N conexões concorrentes por D segundos.
// Uso: node carga.mjs --api URL --email E --senha S [--conexoes 20] [--segundos 60] [--aquecimento 5] [--saida arq.json]
import { writeFileSync } from 'node:fs';

const arg = (n, p) => {
  const i = process.argv.indexOf(`--${n}`);
  return i > -1 ? process.argv[i + 1] : p;
};
const API = arg('api', 'http://localhost:8080');
const EMAIL = arg('email');
const SENHA = arg('senha');
const CONEXOES = Number(arg('conexoes', 20));
const SEGUNDOS = Number(arg('segundos', 60));
const AQUECIMENTO = Number(arg('aquecimento', 5));
const SAIDA = arg('saida');
if (!EMAIL || !SENHA) {
  console.error('Informe --email e --senha.');
  process.exit(2);
}

const login = await fetch(`${API}/api/v1/auth/login`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: EMAIL, senha: SENHA }),
});
if (login.status !== 200) {
  console.error(`Login falhou: ${login.status}`);
  process.exit(1);
}
const token = (await login.json()).accessToken;
const auth = { Authorization: `Bearer ${token}` };

let seq = 0;
const cenarios = [
  { nome: 'GET /procedimentos', pedido: () => fetch(`${API}/api/v1/procedimentos?page=${seq++ % 50}&size=20`, { headers: auth }), ok: [200] },
  { nome: 'GET /autorizacoes', pedido: () => fetch(`${API}/api/v1/autorizacoes?page=${seq++ % 500}&size=20`, { headers: auth }), ok: [200] },
  { nome: 'GET /painel/resumo', pedido: () => fetch(`${API}/api/v1/painel/resumo`, { headers: auth }), ok: [200] },
  {
    nome: 'POST /autorizacoes',
    pedido: () =>
      fetch(`${API}/api/v1/autorizacoes`, {
        method: 'POST',
        headers: { ...auth, 'Content-Type': 'application/json' },
        body: JSON.stringify({ numProcedimento: 1 + (seq++ % 1000), idade: (seq % 15) * 10, sexo: 'M' }),
      }),
    ok: [201],
  },
];

const pct = (o, p) => (o.length ? o[Math.min(o.length - 1, Math.ceil((p / 100) * o.length) - 1)] : NaN);

async function rodar(c, segundos, medir) {
  const lat = [];
  let erros = 0;
  const fim = performance.now() + segundos * 1000;
  const t0 = performance.now();
  await Promise.all(
    Array.from({ length: CONEXOES }, async () => {
      while (performance.now() < fim) {
        const t = performance.now();
        try {
          const r = await c.pedido();
          await r.arrayBuffer();
          if (!c.ok.includes(r.status)) erros++;
        } catch {
          erros++;
        }
        if (medir) lat.push(performance.now() - t);
      }
    }),
  );
  const dur = (performance.now() - t0) / 1000;
  const o = lat.sort((a, b) => a - b);
  return {
    endpoint: c.nome,
    requisicoes: o.length,
    erros,
    taxaErro: o.length ? erros / o.length : 0,
    throughput: o.length / dur,
    p50: pct(o, 50),
    p95: pct(o, 95),
    p99: pct(o, 99),
    max: o[o.length - 1],
  };
}

const resultados = [];
for (const c of cenarios) {
  await rodar(c, AQUECIMENTO, false);
  const r = await rodar(c, SEGUNDOS, true);
  resultados.push(r);
  console.log(
    `${r.endpoint.padEnd(20)} n=${r.requisicoes} erros=${r.erros} rps=${r.throughput.toFixed(1)} p50=${r.p50.toFixed(1)}ms p95=${r.p95.toFixed(1)}ms p99=${r.p99.toFixed(1)}ms`,
  );
}
if (SAIDA) writeFileSync(SAIDA, JSON.stringify({ conexoes: CONEXOES, segundos: SEGUNDOS, aquecimento: AQUECIMENTO, resultados }, null, 2));
