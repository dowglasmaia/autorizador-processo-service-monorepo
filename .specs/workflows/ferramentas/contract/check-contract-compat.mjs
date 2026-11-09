// Verifica compatibilidade do contrato OpenAPI atual com o instantâneo base (.specs/workflows/ferramentas/contract/baseline).
// Falha (código 1) em mudança incompatível dentro de /api/v1: remoção de operação, parâmetro, propriedade,
// resposta ou valor de enum; mudança de tipo; e nova obrigatoriedade. Mudanças aditivas opcionais passam.
// Uso: node .specs/workflows/ferramentas/contract/check-contract-compat.mjs [--base <pasta>] [--atual <pasta>] [--atualizar-base]
import { cpSync, existsSync, readFileSync, rmSync } from 'node:fs';
import { createRequire } from 'node:module';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const raiz = resolve(dirname(fileURLToPath(import.meta.url)), '../../../..');
// js-yaml já é instalado pelo app (dependência transitiva do toolchain do openapi-typescript); evita nova ferramenta.
const { load } = createRequire(resolve(raiz, 'app-autorizador-processo/package.json'))('js-yaml');

const args = process.argv.slice(2);
const opcao = (nome, padrao) => (args.includes(nome) ? resolve(args[args.indexOf(nome) + 1]) : padrao);
const pastaBase = opcao('--base', resolve(raiz, '.specs/workflows/ferramentas/contract/baseline'));
const pastaAtual = opcao('--atual', resolve(raiz, 'api-autorizador-processo/src/main/resources/openapi'));

if (args.includes('--atualizar-base')) {
  rmSync(pastaBase, { recursive: true, force: true });
  cpSync(pastaAtual, pastaBase, { recursive: true });
  console.log(`Instantâneo base atualizado a partir de ${pastaAtual}`);
  process.exit(0);
}
if (!existsSync(pastaBase)) {
  console.error(`FALHA: instantâneo base não encontrado em ${pastaBase}.`);
  process.exit(1);
}

const METODOS = ['get', 'put', 'post', 'delete', 'options', 'head', 'patch', 'trace'];

function carregarContrato(pasta) {
  const cache = new Map();
  const arquivo = (caminho) => {
    if (!cache.has(caminho)) cache.set(caminho, load(readFileSync(caminho, 'utf8')));
    return cache.get(caminho);
  };
  // Resolve $ref (local ou entre arquivos) recursivamente; ciclos viram { $ciclo }.
  const resolver = (no, caminho, pilha = []) => {
    if (Array.isArray(no)) return no.map((n) => resolver(n, caminho, pilha));
    if (no === null || typeof no !== 'object') return no;
    if (typeof no.$ref === 'string') {
      const [arq, ponteiro] = no.$ref.split('#');
      const alvoCaminho = arq ? resolve(dirname(caminho), arq) : caminho;
      const chave = `${alvoCaminho}#${ponteiro}`;
      if (pilha.includes(chave)) return { $ciclo: chave };
      let alvo = arquivo(alvoCaminho);
      for (const parte of ponteiro.split('/').filter(Boolean)) alvo = alvo?.[parte.replace(/~1/g, '/').replace(/~0/g, '~')];
      return resolver(alvo, alvoCaminho, [...pilha, chave]);
    }
    return Object.fromEntries(Object.entries(no).map(([k, v]) => [k, resolver(v, caminho, pilha)]));
  };

  const operacoes = new Map();
  for (const nome of ['auth', 'autorizacao', 'painel', 'procedimento']) {
    const caminho = join(pasta, 'modules', `${nome}.yaml`);
    if (!existsSync(caminho)) continue;
    const doc = resolver(arquivo(caminho), caminho);
    for (const [rota, item] of Object.entries(doc.paths ?? {})) {
      for (const metodo of METODOS.filter((m) => item[m])) {
        const op = item[metodo];
        const parametros = new Map([...(item.parameters ?? []), ...(op.parameters ?? [])].map((p) => [`${p.in}:${p.name}`, p]));
        operacoes.set(`${metodo.toUpperCase()} ${rota}`, { op, parametros });
      }
    }
  }
  return operacoes;
}

const problemas = [];
const quebra = (onde, mensagem) => problemas.push(`${onde}: ${mensagem}`);

function compararEsquema(antigo, novo, onde) {
  if (!antigo || !novo || antigo.$ciclo || novo.$ciclo) return;
  const tipo = (t) => (Array.isArray(t) ? [...t].sort().join("|") : t);
  if (tipo(antigo.type) !== tipo(novo.type)) quebra(onde, `tipo mudou de "${tipo(antigo.type)}" para "${tipo(novo.type)}"`);
  if (antigo.format !== undefined && antigo.format !== novo.format) {
    quebra(onde, `formato mudou de "${antigo.format}" para "${novo.format}"`);
  }
  for (const valor of antigo.enum ?? []) {
    if (novo.enum && !novo.enum.includes(valor)) quebra(onde, `valor de enum "${valor}" removido`);
  }
  const reqAntigo = new Set(antigo.required ?? []);
  for (const campo of novo.required ?? []) {
    if (!reqAntigo.has(campo)) quebra(`${onde}.${campo}`, 'passou a ser obrigatório');
  }
  for (const [campo, esquema] of Object.entries(antigo.properties ?? {})) {
    if (!novo.properties || !(campo in novo.properties)) quebra(`${onde}.${campo}`, 'propriedade removida ou renomeada');
    else compararEsquema(esquema, novo.properties[campo], `${onde}.${campo}`);
  }
  if (antigo.items && novo.items) compararEsquema(antigo.items, novo.items, `${onde}[]`);
}

function compararConteudo(antigo, novo, onde) {
  for (const [tipo, midia] of Object.entries(antigo?.content ?? {})) {
    if (!novo?.content?.[tipo]) quebra(onde, `content-type "${tipo}" removido`);
    else compararEsquema(midia.schema, novo.content[tipo].schema, `${onde} <${tipo}>`);
  }
}

const base = carregarContrato(pastaBase);
const atual = carregarContrato(pastaAtual);

for (const [chave, { op: opAntiga, parametros: pAntigos }] of base) {
  const novo = atual.get(chave);
  if (!novo) {
    quebra(chave, 'operação removida ou renomeada');
    continue;
  }
  const { op: opNova, parametros: pNovos } = novo;
  if (opAntiga.operationId !== opNova.operationId) {
    quebra(chave, `operationId mudou de "${opAntiga.operationId}" para "${opNova.operationId}"`);
  }
  for (const [id, p] of pAntigos) {
    const n = pNovos.get(id);
    if (!n) quebra(`${chave} parâmetro ${id}`, 'removido ou renomeado');
    else {
      if (!p.required && n.required) quebra(`${chave} parâmetro ${id}`, 'passou a ser obrigatório');
      compararEsquema(p.schema, n.schema, `${chave} parâmetro ${id}`);
    }
  }
  for (const [id, n] of pNovos) {
    if (!pAntigos.has(id) && n.required) quebra(`${chave} parâmetro ${id}`, 'novo parâmetro obrigatório');
  }
  if (opAntiga.requestBody) {
    if (!opNova.requestBody) quebra(`${chave} corpo`, 'corpo da requisição removido');
    else {
      if (!opAntiga.requestBody.required && opNova.requestBody.required) quebra(`${chave} corpo`, 'passou a ser obrigatório');
      compararConteudo(opAntiga.requestBody, opNova.requestBody, `${chave} corpo`);
    }
  } else if (opNova.requestBody?.required) {
    quebra(`${chave} corpo`, 'novo corpo obrigatório');
  }
  for (const [status, resposta] of Object.entries(opAntiga.responses ?? {})) {
    if (!opNova.responses?.[status]) quebra(`${chave} resposta ${status}`, 'removida');
    else compararConteudo(resposta, opNova.responses[status], `${chave} resposta ${status}`);
  }
}

if (problemas.length > 0) {
  console.error(`FALHA: ${problemas.length} mudança(s) incompatível(is) em /api/v1 em relação a .specs/workflows/ferramentas/contract/baseline:`);
  for (const p of problemas) console.error(`  - ${p}`);
  console.error(
    'Se a mudança é intencional, publique uma nova versão da API. Mudança aditiva aprovada: ' +
      'rode "node .specs/workflows/ferramentas/contract/check-contract-compat.mjs --atualizar-base" e versione o instantâneo.',
  );
  process.exit(1);
}
console.log(`OK: contrato compatível com o instantâneo base (${base.size} operações verificadas, ${atual.size} no contrato atual).`);
