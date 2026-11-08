import { compose, removerEnvDeTeste } from './ambiente';

/** Derruba o projeto Compose `e2e` e apaga o volume do banco, mesmo se os testes falharam. */
export default async function globalTeardown(): Promise<void> {
  try {
    compose('down', '-v', '--remove-orphans');
  } finally {
    removerEnvDeTeste();
  }
}
