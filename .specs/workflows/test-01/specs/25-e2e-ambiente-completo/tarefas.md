# Tarefas — Testes e2e do ambiente

## 1. Playwright e ambiente
- [ ] 1.1 Conferir o estado após a etapa 24 (rotas, textos, seletores acessíveis, `compose.yaml`) e registrar o desvio da pasta `06-execution-plan`.
- [ ] 1.2 Instalar `@playwright/test` como devDependency em `app-autorizador-processo/`, baixar Chromium e adicionar o script `npm run e2e`.
- [ ] 1.3 Criar `playwright.config.ts` com projetos desktop e móvel, URL base `http://localhost:${APP_PORT:-4200}`, relatório HTML/JUnit e capturas em falha.
- [ ] 1.4 Adicionar relatório e resultados ao `.gitignore` do app.

## 2. Fixture de ambiente
- [ ] 2.1 Criar o `.env` de teste descartável gerado em tempo de execução (usuário *bootstrap*, `JWT_SECRET`, TTL curto), sem segredos versionados.
- [ ] 2.2 Confirmar que a seed (perfil `local` padrão) está ativa no Compose; só se não estiver, criar override mínimo `compose.e2e.yaml` sem tocar o `compose.yaml` principal.
- [ ] 2.3 Implementar global setup: `docker compose -p e2e up -d --build`, espera por `healthy` de `api` e `app`.
- [ ] 2.4 Implementar global teardown: `docker compose -p e2e down -v`, inclusive em caso de falha.
- [ ] 2.5 Criar helpers: login via UI, geração de números únicos de procedimento e leitura da seed.

## 3. Cenários de autenticação e painel
- [ ] 3.1 Cenário 0: login inválido mostra erro; login válido entra; rota protegida sem sessão vai a `/login` com `retorno`; sair encerra a sessão.
- [ ] 3.2 Cenário 0b: sessão expirada (TTL curto) volta ao login.
- [ ] 3.3 Cenário 1: painel mostra os totais da seed (3 procedimentos, 0 autorizações, 3 idades).

## 4. Cenários de procedimentos e autorizações
- [ ] 4.1 Cenário 2: cadastrar procedimento completo (4 etapas) e vê-lo na lista.
- [ ] 4.2 Cenário 3: número duplicado mostra erro no campo.
- [ ] 4.3 Cenário 4: solicitar 4567/20/F mostra "Autorizado" e aparece no histórico e no painel.
- [ ] 4.4 Cenário 5: solicitar 1234/30/M mostra "Negado" e o histórico não muda.
- [ ] 4.5 Cenário 6: `?numero=` inexistente forjado mostra erro tratado.

## 5. Resiliência e responsividade
- [ ] 5.1 Cenário 7: parar a API mostra o modal "Não foi possível carregar" e a tela retoma ao religar.
- [ ] 5.2 Cenário 8: rota SPA direta (`/autorizacoes`) funciona após reload.
- [ ] 5.3 Cenário 9: viewport de 320 px sem rolagem horizontal nas telas principais.

## 6. Verificação e documentação
- [ ] 6.1 Rodar `npm run e2e` contra o Compose limpo (desktop e móvel) e corrigir apenas problemas dos testes; falhas de produto viram registro.
- [ ] 6.2 Conferir que o teardown removeu contêineres e volume do projeto `e2e` e que nada ficou rodando.
- [ ] 6.3 Documentar a execução local (`npm run e2e`, pré-requisitos, Docker) no README do app.
- [ ] 6.4 Rodar `npm run lint`, `npm run build` e `npm test` do app e corrigir falhas.
- [ ] 6.5 Registrar evidência por tarefa, resumo do relatório e bloqueios, e arquivar a spec conforme `como-executar.md`.
