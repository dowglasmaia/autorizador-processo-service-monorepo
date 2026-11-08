# SECURITY_REQUIREMENTS

Contexto: a versão original **não tinha autenticação nem autorização** [EXEC]. Decisão do produto (2026-11-08): a nova versão exige **login por e-mail e senha com JWT** em todas as rotas, exceto o login e o health (ADR-16). Perfis/papéis ficam fora da v1 (PEN-11).

| ID | Requisito | Spec |
|---|---|---|
| SEC-01 | Nenhum segredo versionado (a senha MySQL da versão original **não** migra). Credenciais e `JWT_SECRET` só por variável de ambiente/`.env` ignorado pelo git | 03, 10, 22 |
| SEC-02 | Erros nunca expõem stack trace, SQL ou nomes de classe; `ERRO_INTERNO` genérico + `traceId` em log | 06 |
| SEC-03 | Validação de entrada em toda borda (tipos, faixas, tamanhos, enums); corpo com `id` ignorado/rejeitado | 05, 06, 07 |
| SEC-04 | Dados do paciente (idade/sexo) **fora da URL** (DIV-05) e fora de logs de acesso; não há identificação de paciente | 08 |
| SEC-05 | CORS por allowlist configurável; sem `*`; `Authorization` permitido; desativado por padrão fora de `local` | 06 |
| SEC-06 | Cabeçalhos de segurança no Nginx (`X-Content-Type-Options`, `Referrer-Policy`, `X-Frame-Options`/`frame-ancestors`, CSP) | 13, 41 |
| SEC-07 | Actuator expõe somente `health` e `info`; swagger UI só no perfil `local` | 11 |
| SEC-08 | Contêineres sem root, imagens mínimas, sem ferramentas de build na imagem final | 10, 13 |
| SEC-09 | Dependências sem vulnerabilidades críticas conhecidas (varredura no CI) | 40, 41 |
| SEC-10 | SQL só parametrizado (JPA/Flyway); sem concatenação | 07, 08, 09, 22 |
| SEC-11 | Frontend sem `innerHTML` com dados externos; sanitização padrão do Angular; sem segredos no bundle | 14–23 |
| SEC-12 | A aplicação deve ser exposta apenas atrás de HTTPS (terminação TLS fora do escopo, documentada no README); sem tráfego de credenciais em texto claro | 41, 43 |
| SEC-13 | **Todas** as rotas `/api/v1/**` exigem JWT válido, exceto `POST /api/v1/auth/login`; `actuator/health*` público. Teste que varre os mapeamentos falha se algo de negócio responder sem token | 22 |
| SEC-14 | JWT HS256 com `JWT_SECRET` ≥ 32 bytes, **obrigatório e sem padrão**: a aplicação não inicia sem ele; validação de `exp`, `iss` e assinatura; claims mínimas (`sub`, `email`, `iat`, `exp`, `iss`); nada sensível no token; `alg=none` e algoritmo trocado rejeitados | 22, 41 |
| SEC-15 | Senhas guardadas **somente** como hash BCrypt (força ≥ 10); nunca registradas em log nem retornadas; e-mail único sem diferenciar caixa | 22 |
| SEC-16 | Login sem enumeração de usuários: mesma resposta (`CREDENCIAIS_INVALIDAS`) e tempo equivalente para e-mail inexistente, senha errada ou usuário inativo | 22, 41 |
| SEC-17 | Limitador de tentativas de login: 5 falhas por (e-mail, IP) em 15 min → `429` com `Retry-After` (PRM-09); log de falhas com e-mail mascarado | 22 |
| SEC-18 | Sessão stateless, sem cookies (sem CSRF); expiração `JWT_TTL_MINUTOS` (60); sem refresh token na v1 (PEN-10) | 22 |
| SEC-19 | Frontend: token só em `sessionStorage`, nunca em `localStorage`/URL/log; anexado apenas a requisições da API; `retorno` do login aceita só caminho interno (anti *open redirect*); `401` limpa sessão e estado em memória | 23 |
| SEC-20 | Logs e métricas nunca contêm senha, token ou `JWT_SECRET`; respostas `401/403` em `problem+json` sem detalhes internos | 22, 41 |
| SEC-21 | Riscos aceitos registrados no README: sem refresh token, limitador em memória (uma instância), sem papéis (PEN-10/11/12) | 41, 43 |
