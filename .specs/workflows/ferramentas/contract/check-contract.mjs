// Verifica desvio entre o contrato OpenAPI e os tipos versionados do frontend.
// Regenera os tipos em diretório temporário e compara com api-types.ts. Sai com código 1 se diferirem.
// Uso: node scripts/check-contract.mjs
import { spawnSync } from 'node:child_process';
import { existsSync, mkdtempSync, readFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const raiz = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const app = resolve(raiz, 'app-autorizador-processo');
const versionado = resolve(app, 'src/app/shared/api/api-types.ts');
const rotulo = 'app-autorizador-processo/src/app/shared/api/api-types.ts';

const falhar = (mensagem) => {
  console.error(`FALHA: ${mensagem}`);
  process.exit(1);
};

if (!existsSync(versionado)) falhar(`${rotulo} não existe. Rode "npm run api:types" em app-autorizador-processo.`);

const temporario = mkdtempSync(join(tmpdir(), 'check-contract-'));
try {
  const gerado = join(temporario, 'api-types.ts');
  const execucao = spawnSync(process.execPath, [resolve(app, 'scripts/gerar-api-types.mjs'), gerado], {
    cwd: app,
    encoding: 'utf8',
  });
  if (execucao.status !== 0) {
    falhar(`a geração dos tipos falhou (o contrato é inválido ou faltam dependências).\n${execucao.stderr}`);
  }

  const normalizar = (texto) => texto.replace(/\r\n/g, '\n');
  const a = normalizar(readFileSync(versionado, 'utf8'));
  const b = normalizar(readFileSync(gerado, 'utf8'));
  if (a !== b) {
    const linhasA = a.split('\n');
    const linhasB = b.split('\n');
    const indice = linhasA.findIndex((linha, i) => linha !== linhasB[i]);
    const primeira = indice === -1 ? Math.min(linhasA.length, linhasB.length) : indice;
    falhar(
      `${rotulo} está defasado em relação ao contrato OpenAPI (primeira diferença na linha ${primeira + 1}).\n` +
        'Regenere com "npm run api:types" em app-autorizador-processo e versione o resultado.',
    );
  }
  console.log(`OK: ${rotulo} corresponde ao contrato OpenAPI.`);
} finally {
  rmSync(temporario, { recursive: true, force: true });
}
