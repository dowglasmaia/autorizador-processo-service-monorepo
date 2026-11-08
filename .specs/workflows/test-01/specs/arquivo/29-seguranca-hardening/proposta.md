# Segurança e hardening

Etapa 29 de 31 · Fase 4 · derivada da `SPEC-41` (`.specs/05-quality/SPEC-41-seguranca-hardening.md`), com referência a `.specs/05-quality/SECURITY_REQUIREMENTS.md` (SEC-01..SEC-21), SPEC-06, 11, 13, 22, 23, 30 e 40. Revisada contra a base entregue até a etapa 28 (CI por projeto).

## Objetivo
Verificar cada requisito de `SECURITY_REQUIREMENTS.md` com **evidência executada** (comando ou teste + resultado) e corrigir lacunas dentro do escopo, incluindo a revisão da autenticação JWT (algoritmo, segredo, expiração, `iss`, adulteração, limitador de tentativas, armazenamento no frontend, enumeração de usuários). Revisar cabeçalhos, segredos, limites de requisição e dependências. Registrar os riscos aceitos (sem refresh token, limitador em memória, sem papéis) no README raiz.

## Ponto de partida (conferido após a etapa 28)
- Não existe `SECURITY_REQUIREMENTS.md` na raiz: ele está em `.specs/05-quality/`. `.specs/06-execution-plan/` também não existe (desvio a registrar na evidência).
- Backend: pacote `autenticacao` (`AuthController`, `LimitadorTentativas`/adaptador, `HashSenhaAdapter`, `EmissorTokenAdapter`) e `shared/security` (`SecurityConfig`, `JwtConfig`, `EmissorJwt`, `Problem*Handler/EntryPoint`). Testes existentes: `AuthApiTest`, `LimitadorTentativasAdapterTest`, `AcessoNegadoContratoTest`, `VarreduraSegurancaTest`, `JwtTestSupport`. `application.yml` já exige `JWT_SECRET` sem padrão, CORS por allowlist vazia por padrão e datasource sem padrão fora do perfil `local`.
- Frontend: `core/auth` (`auth.service`, `auth.guard`, `auth.interceptor`, `retorno-seguro.ts`) e `core/http/error.interceptor`. Nginx com `security-headers.inc` (nosniff, Referrer-Policy, X-Frame-Options, CSP) e `server_tokens off`.
- Dockerfile do backend roda como usuário `app` sem root. **Lacunas prováveis a confirmar:** não há `client_max_body_size` nem `limit_req` no Nginx, nem limite explícito de tamanho de cabeçalho/corpo no Spring; confirmado que `security-headers.inc` não tem `Permissions-Policy`; falta confirmar `Cache-Control` em respostas autenticadas. O Dockerfile do frontend já usa `nginxinc/nginx-unprivileged` (porta 8080), então SEC-08 do frontend tende a só precisar de evidência.
- O README raiz não tem seção de modelo de ameaça nem de riscos aceitos.
- A etapa 28 entregou `.github/workflows/backend-ci.yml`, `frontend-ci.yml` e `integracao-ci.yml`; a etapa 29 apenas lê esses workflows para a varredura de dependências (SEC-09) e não os altera, salvo se a revisão apontar falha de segurança nos próprios workflows (permissões, segredos).

## Escopo
1. **Checklist SEC-01..SEC-21** (a SPEC cita SEC-01..12; o arquivo de requisitos tem 21 itens, e todos entram): tabela SEC × comando/teste executado × resultado, registrada na evidência da etapa.
2. **Sondagens ativas no Compose** (script reproduzível, com `.env` descartável): token adulterado, expirado, `alg=none`, algoritmo trocado e `iss` incorreto → `401` em `problem+json`; login com e-mail inexistente × senha errada × usuário inativo → mesma resposta e tempo equivalente; 6ª falha → `429` com `Retry-After`; `GET /actuator/env|beans|heappump` → 404; erro forçado → sem stack trace; `Origin` não permitida → bloqueada; `curl -I /` → cabeçalhos do Nginx; corpo com `id` → ignorado/rejeitado; SQL/HTML em `nome` → armazenado e exibido escapado; corpo acima do limite → `413`.
3. **Limites de requisição:** definir limite de corpo (Nginx `client_max_body_size` e propriedade do Spring) e, se couber como endurecimento de configuração, `limit_req` no `/api/v1/auth/login` do Nginx. Valores conservadores, configuráveis.
4. **Cabeçalhos:** revisar `security-headers.inc` e completar o que faltar (por exemplo `Permissions-Policy`, `Cache-Control: no-store` nas respostas da API), sem enfraquecer a CSP.
5. **Segredos:** varredura do histórico atual (`git grep`/ferramenta) e do bundle do frontend; confirmar que a senha MySQL da versão original não migrou e que só `.env.example` está versionado.
6. **Dependências e imagens:** varredura Maven, npm e imagens base com resultado registrado; atualizar apenas vulnerabilidade crítica quando compatível.
7. **README raiz:** modelo de ameaça resumido, riscos aceitos (SEC-21; PEN-10/11/12) e recomendação de HTTPS na frente do Nginx (SEC-12).
8. Testes de regressão para cada correção feita; `./mvnw verify`, `npm run lint`, `npm run build` e `npm test` verdes ao final.

## Fora do escopo
- Mudança de comportamento (refresh token, papéis, limitador distribuído, cadastro/recuperação de senha, MFA): volta ao fluxo de autorização e fica fora do plano.
- Terminação TLS, WAF, deploy e infraestrutura: apenas documentados.
- Alterar o contrato OpenAPI além de, no máximo, documentar a resposta `413`/`429` já previstas; sem endpoints novos.
- Dados reais em testes; publicação de imagens.
- SPEC-43 (sucessora, fechamento/documentação final): fica para a etapa 30/31.
- Reescrever o CI da etapa 28.

## Critérios de aceitação
- Tabela SEC-01..SEC-21 completa: nenhum item sem evidência executada; itens sem como executar (ex.: HTTPS real) marcados como documentados, com justificativa.
- Todas as sondagens do item 2 passam contra o Compose; falhas viram correção com teste, ou risco aceito registrado.
- Nenhum segredo no histórico atual nem no bundle; nenhuma vulnerabilidade crítica sem tratamento ou justificativa.
- README com modelo de ameaça, riscos aceitos e recomendação de HTTPS.
- `./mvnw verify`, `npm run lint`, `npm run build` e `npm test` verdes. Se o Docker não estiver disponível, as sondagens ficam bloqueadas e registradas, sem marcar como concluídas.

## Decisões tomadas sem consulta
- **SEC-01..SEC-21 em vez de SEC-01..12:** o arquivo de requisitos evoluiu com o login JWT; a revisão cobre todos, pois a SPEC pede "cada requisito". **[REVISAR]**
- **Correções limitadas a endurecimento:** limite de corpo e `limit_req` entram como configuração; se algum valor alterar comportamento observável de forma relevante, vira risco aceito e não correção. **[REVISAR]**
- **Varredura de dependências:** `npm audit` e a ferramenta já disponível no ambiente (ex.: `docker scout`/`trivy` via Docker, se existir); sem adicionar plugin Maven novo ao build. Se nenhuma ferramenta de imagem estiver disponível, registrar limitação.
- **Sondagens como script:** o script de sondagem fica em `scripts/` (ou `tools/security/`), usa `.env` descartável e não deixa processos rodando.
- **Execução real em HTTPS** não é possível aqui: SEC-12 fica documentada, não executada.
- **Lote 1 · 413 com `code: CORPO_INVALIDO`:** o enum de `code` do contrato não tem valor próprio para corpo grande; reutilizei `CORPO_INVALIDO` em vez de ampliar o contrato (413 e 411 não estão documentados nas operações). **[REVISAR]**
- **Lote 1 · 405/415 sem `code`:** como no 404, o handler não define `code` para não ampliar o enum do contrato.
- **Lote 1 · Nginx por template:** `default.conf` virou `default.conf.template` e entrou `limites.conf.template` (mecanismo `/etc/nginx/templates` com `envsubst` da imagem) para que os limites sejam configuráveis por variável `NGINX_*`; valores padrão no `Dockerfile`. `MONOREPO_STRUCTURE.md` atualizado.
- **Lote 1 · `limit_req` aplicado** (30r/m por IP, burst 10) em vez de risco aceito, por ser só configuração. **[REVISAR]** os valores.
- **Lote 1 · Perfil no Compose:** `SPRING_PROFILES_ACTIVE` passou a ser repassado (padrão `local`, sem mudar o comportamento atual; o e2e depende da seed). A sondagem usa `prod`.
- **Lote 1 · `HDR-03/HDR-04`** (Permissions-Policy, Cache-Control na API) ficaram fora do script até a tarefa 3.3, para não deixar sondagem falhando; entram junto da correção.
- **Lote 1 · Correções fora da lista, achadas pelas sondagens (com teste):** `exp` obrigatório no JWT; 405/415 deixam de responder 500.

### Decisões do lote 2 (sem consulta)
- 3.3: `Cache-Control` da API mantido nos padrões do Spring Security (já `no-store`), apenas coberto por teste e sondagem; acrescentado `Cross-Origin-Opener-Policy` além do `Permissions-Policy` pedido.
- 4.2: o plugin de dependências do Maven não resolveu no ambiente (PKIX); a varredura Maven foi feita consultando o OSV.dev com os jars do artefato. Sem ferramenta de varredura de imagens (4.3): limitação registrada, não bloqueante. **[REVISAR]** decidir se o CI passa a rodar Trivy/OWASP Dependency-Check.
- 4.4: nenhuma atualização (só vulnerabilidade moderada no OpenTelemetry, gerida pelo BOM do Spring Boot).
