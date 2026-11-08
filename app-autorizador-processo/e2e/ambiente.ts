import { execFileSync } from 'node:child_process';
import { randomBytes } from 'node:crypto';
import { mkdirSync, rmSync, writeFileSync } from 'node:fs';
import { resolve } from 'node:path';

/** Projeto Compose descartável do e2e (não colide com o ambiente de desenvolvimento). */
export const PROJETO_COMPOSE = 'e2e';

const aqui = __dirname;
export const RAIZ_REPOSITORIO = resolve(aqui, '..', '..');
export const PASTA_TEMPORARIA = resolve(aqui, '.tmp');
export const ARQUIVO_ENV = resolve(PASTA_TEMPORARIA, 'e2e.env');

export const APP_PORT = process.env['APP_PORT'] ?? '4200';
export const URL_BASE = `http://localhost:${APP_PORT}`;

export interface Credenciais {
  email: string;
  senha: string;
}

/**
 * Gera o `.env` descartável do ambiente de teste. Os valores são sorteados a cada execução
 * e o arquivo fica em `e2e/.tmp/` (ignorado pelo git): nenhum segredo é versionado.
 */
export function gerarEnvDeTeste(): Credenciais {
  const credenciais: Credenciais = {
    email: 'e2e@autorizador.test',
    senha: `E2e-${randomBytes(12).toString('hex')}`,
  };
  const linhas = [
    `APP_PORT=${APP_PORT}`,
    // Portas altas para não colidir com um ambiente de desenvolvimento já em execução.
    `API_PORT=${process.env['E2E_API_PORT'] ?? '18080'}`,
    `POSTGRES_PORT=${process.env['E2E_POSTGRES_PORT'] ?? '15432'}`,
    `POSTGRES_PASSWORD=${randomBytes(12).toString('hex')}`,
    `JWT_SECRET=${randomBytes(48).toString('base64')}`,
    // TTL mínimo (1 minuto) para o cenário de sessão expirada.
    'JWT_TTL_MINUTOS=1',
    `AUTH_BOOTSTRAP_EMAIL=${credenciais.email}`,
    `AUTH_BOOTSTRAP_SENHA=${credenciais.senha}`,
    `CORS_ALLOWED_ORIGINS=${URL_BASE}`,
  ];
  mkdirSync(PASTA_TEMPORARIA, { recursive: true });
  writeFileSync(ARQUIVO_ENV, linhas.join('\n') + '\n', { encoding: 'utf8', mode: 0o600 });
  return credenciais;
}

export function removerEnvDeTeste(): void {
  rmSync(PASTA_TEMPORARIA, { recursive: true, force: true });
}

/** Usa o plugin "docker compose" quando existir; senão o binário independente "docker-compose". */
function comandoCompose(): { bin: string; prefixo: string[] } {
  try {
    execFileSync('docker', ['compose', 'version'], { stdio: 'ignore' });
    return { bin: 'docker', prefixo: ['compose'] };
  } catch {
    return { bin: 'docker-compose', prefixo: [] };
  }
}

const { bin, prefixo } = comandoCompose();

/** Executa "compose -p e2e ..." na raiz do repositório com o .env descartável. */
export function compose(...args: string[]): string {
  return execFileSync(
    bin,
    [...prefixo, '-p', PROJETO_COMPOSE, '--env-file', ARQUIVO_ENV, '-f', 'compose.yaml', ...args],
    { cwd: RAIZ_REPOSITORIO, encoding: 'utf8', stdio: ['ignore', 'pipe', 'inherit'] },
  );
}
