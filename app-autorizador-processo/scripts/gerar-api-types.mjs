// Gera src/app/shared/api/api-types.ts a partir do contrato OpenAPI do backend.
// Cada módulo do contrato vira um namespace de tipos; os $ref (inclusive para common/) são resolvidos pelo openapi-typescript.
import { readdirSync, writeFileSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import openapiTS, { astToString } from 'openapi-typescript';

const raiz = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const pastaContrato = resolve(raiz, '../api-autorizador-processo/src/main/resources/openapi/modules');
// Caminho de saída opcional (1º argumento); sem ele, grava o arquivo versionado.
const saida = process.argv[2] ? resolve(process.argv[2]) : resolve(raiz, 'src/app/shared/api/api-types.ts');

const modulos = readdirSync(pastaContrato)
  .filter((arquivo) => arquivo.endsWith('.yaml'))
  .sort();

const nomeNamespace = (arquivo) => {
  const base = arquivo.replace(/\.yaml$/, '');
  return base.charAt(0).toUpperCase() + base.slice(1);
};

let conteudo = `/* eslint-disable */
// ARQUIVO GERADO por \`npm run api:types\` (scripts/gerar-api-types.mjs). NÃO EDITE À MÃO.
// Fonte: api-autorizador-processo/src/main/resources/openapi/modules/*.yaml
`;

for (const arquivo of modulos) {
  const ast = await openapiTS(pathToFileURL(resolve(pastaContrato, arquivo)));
  const corpo = astToString(ast)
    .trim()
    .split('\n')
    .map((linha) => (linha ? `  ${linha}` : linha))
    .join('\n');
  conteudo += `\nexport namespace ${nomeNamespace(arquivo)} {\n${corpo}\n}\n`;
}

mkdirSync(dirname(saida), { recursive: true });
writeFileSync(saida, conteudo, 'utf8');
console.log(`Tipos gerados em ${saida} (${modulos.length} módulos)`);
