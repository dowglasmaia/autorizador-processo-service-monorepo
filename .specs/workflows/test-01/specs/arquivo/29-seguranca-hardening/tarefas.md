# Tarefas

## 1. Preparação e checklist
- [x] 1.1 Ler `SPEC-41`, `SECURITY_REQUIREMENTS.md`, `DECISIONS.md` (ADR-16, PEN-09..PEN-12) e registrar o desvio da pasta `06-execution-plan` inexistente
- [x] 1.2 Montar a tabela SEC-01..SEC-21 na evidência (requisito, comando/teste, resultado), apontando os testes existentes que já cobrem cada item

## 2. Sondagens ativas
- [x] 2.1 Criar script de sondagem reproduzível em `tools/security/` (`.env` descartável, sobe e derruba o Compose) para token adulterado, expirado, `alg=none`, algoritmo trocado e `iss` incorreto → `401` em `problem+json`
- [x] 2.2 Sondar login com e-mail inexistente, senha errada e usuário inativo (resposta idêntica e tempo equivalente) e a 6ª falha → `429` com `Retry-After`
- [x] 2.3 Sondar `actuator/env|beans|heappump` (404), erro forçado sem stack trace, `Origin` não permitida, `curl -I /` e corpo com `id`
- [x] 2.4 Sondar SQL/HTML em `nome` (armazenado e exibido escapado) e corpo acima do limite (`413`)

## 3. Endurecimento
- [x] 3.1 Definir limite de corpo e de cabeçalho no Nginx e no Spring, configuráveis, com teste de regressão
- [x] 3.2 Avaliar `limit_req` no login pelo Nginx; aplicar ou registrar como risco aceito
- [x] 3.3 Revisar `security-headers.inc` e completar o que faltar (`Permissions-Policy`, `Cache-Control` na API) sem enfraquecer a CSP
- [x] 3.4 Registrar evidência de usuário sem root e imagem mínima nos dois Dockerfiles (SEC-08; frontend usa `nginx-unprivileged`) e corrigir se faltar

## 4. Segredos e dependências
- [x] 4.1 Varrer o histórico atual e o bundle do frontend por segredos; confirmar que a senha MySQL da versão original não migrou e que só `.env.example` está versionado
- [x] 4.2 Rodar varredura de dependências Maven e npm e registrar os resultados
- [x] 4.3 Varrer as imagens base e revisar permissões/segredos dos workflows em `.github/workflows` dos dois Dockerfiles (sem Trivy/Grype instalados: limitação registrada na evidência)
- [x] 4.4 Atualizar dependência com vulnerabilidade crítica quando compatível, com testes verdes

## 5. Documentação
- [x] 5.1 Escrever no README raiz o modelo de ameaça resumido e a recomendação de HTTPS na frente do Nginx (SEC-12)
- [x] 5.2 Registrar os riscos aceitos no README: sem refresh token, limitador em memória (uma instância), sem papéis (PEN-10/11/12)

## 6. Verificação final
- [x] 6.1 Rodar `./mvnw verify` em `api-autorizador-processo` e corrigir falhas
- [x] 6.2 Rodar `npm run lint`, `npm run build` e `npm test` em `app-autorizador-processo` e corrigir falhas
- [x] 6.3 Escrever evidência curta por tarefa e resumo, e arquivar a spec com `git mv` conforme `como-executar.md`
