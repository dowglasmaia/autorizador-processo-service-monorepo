# NON_FUNCTIONAL_REQUIREMENTS

Metas são **provisórias** (sem linha de base medida na versão original); SPEC-42 mede e ajusta, registrando evidência.

| ID | Categoria | Requisito | Verificação |
|---|---|---|---|
| NFR-01 | Disponibilidade operacional | `GET /actuator/health` `UP` em ≤ 60 s após `compose up`; `app` só inicia com `api` saudável | SPEC-10, 30 |
| NFR-02 | Desempenho API | p95 ≤ 200 ms em `GET /procedimentos` e `POST /autorizacoes` com 1.000 procedimentos e 10.000 autorizações, 20 conexões concorrentes | SPEC-42 |
| NFR-03 | Escala de consulta | Nenhuma consulta N+1 nas listas (contagem de SQL por requisição constante) | SPEC-07, 08, 42 |
| NFR-04 | Desempenho web | Bundle inicial ≤ 250 kB gz (meta), LCP ≤ 2,5 s em perfil móvel simulado | SPEC-42 |
| NFR-05 | Acessibilidade | WCAG 2.2 AA: contraste, foco visível, rótulos, `aria-live` nos resultados, navegação por teclado, `prefers-reduced-motion` | SPEC-15, 21 |
| NFR-06 | Responsividade | 320 px → desktop sem rolagem horizontal da página; tabelas com alternativa móvel | SPEC-15, 17–19, 21 |
| NFR-07 | Observabilidade | Logs JSON com correlação; `traceId` em erros; eventos de negócio logados sem dados sensíveis | SPEC-11 |
| NFR-08 | Configurabilidade | Toda configuração de ambiente por variável (`DB_*`, `APP_ENV`, `LOG_LEVEL_APP`, `CORS_ALLOWED_ORIGINS`, `API_PORT`, `APP_PORT`) | SPEC-03, 10, 11 |
| NFR-09 | Resiliência | Falha de API no frontend → modal "Não foi possível carregar" com nova tentativa; nada é perdido nos formulários | SPEC-16, 17–20 |
| NFR-10 | Manutenibilidade | Arquitetura verificada por testes; contrato único; `doc.md` por módulo do frontend | SPEC-03, 14, 43 |
| NFR-11 | Portabilidade | Build reprodutível por Docker; sem dependência do SO do desenvolvedor | SPEC-10, 13, 40 |
| NFR-12 | Idioma | UI e mensagens em pt-BR consistentes com catálogo de microcopy | SPEC-21 |
