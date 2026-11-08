import { compose, gerarEnvDeTeste, removerEnvDeTeste } from './ambiente';

/**
 * Sobe o ambiente completo (PostgreSQL + API + Nginx) no projeto Compose `e2e` e espera os serviços
 * ficarem `healthy`. Em qualquer falha, derruba tudo antes de propagar o erro.
 */
export default async function globalSetup(): Promise<void> {
  const credenciais = gerarEnvDeTeste();
  // Os workers herdam estas variáveis (usadas pelos helpers de login).
  process.env['E2E_EMAIL'] = credenciais.email;
  process.env['E2E_SENHA'] = credenciais.senha;

  try {
    // Descarta restos de uma execução interrompida (volume antigo guarda outra senha do banco).
    compose('down', '-v', '--remove-orphans');
    // `--wait` bloqueia até todos os serviços ficarem healthy (api e app têm healthcheck).
    compose('up', '-d', '--build', '--wait', '--wait-timeout', '420');
    const estado = compose('ps', '--format', '{{.Service}}={{.Health}}');
    for (const servico of ['api', 'app']) {
      if (!estado.split(/\r?\n/).includes(`${servico}=healthy`)) {
        throw new Error(`O serviço "${servico}" não ficou healthy:\n${estado}`);
      }
    }
  } catch (erro) {
    try {
      compose('down', '-v', '--remove-orphans');
    } finally {
      removerEnvDeTeste();
    }
    throw erro;
  }
}
