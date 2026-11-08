# FUNCTIONAL_TRACEABILITY — matriz de rastreabilidade funcional

Classificação: **Fato** (evidência executada/lida) · **Inferência** · **Recomendação**. Destino: Preservar · Modernizar · Completar · Desenvolver (nova feature) · Fora de escopo.

| ID | Funcionalidade | Protótipo (evidência) | Backend original (evidência) | Regra / comportamento | Destino | Specs |
|---|---|---|---|---|---|---|
| F01 | Listar procedimentos | Tela Procedimentos: tabela Nº/Sexos/Idades/ID (Fato) | `GET /procedimentos` `200` (EXEC B01) | idades sem ordem → ordenar | Modernizar | 07, 17 |
| F02 | Cadastrar procedimento (nº, sexos, idades) | Etapas 1 e 3 do formulário (Fato) | `POST /procedimentos` `201` (EXEC B02) | RN1, RN3, RN4 | Modernizar | 07, 17 |
| F03 | Rejeitar nº duplicado | Mensagem "já cadastrado" (Fato) | `403` (EXEC B04); corrida possível (Fato) | RN2 | Preservar (+UNIQUE como garantia) | 07, 17 |
| F04 | Dados de identificação: nome, TUSS, categoria, especialidade | Etapa 1 (Fato) | **Ausente** (Fato) | só catálogo | **Desenvolver** (NEW-01) | 12, 17 |
| F05 | Tipo de cobertura (plano/regras/não coberto) | Etapa 2 (Fato) | Ausente | só catálogo; não decide (PEN-02) | **Desenvolver** | 12, 17 |
| F06 | Utilização: quantidade máxima, período, exige prévia, exige justificativa | Etapa 4 (Fato) | Ausente | só catálogo; não decide (PEN-02) | **Desenvolver** | 12, 17 |
| F07 | Elegibilidade por faixa de idade + sexo (M/F/Ambos) | Etapa 3 (Fato) | Só idades **exatas** e sexos (Fato) | RN4, RN6 | Preservar regra; UI expande faixa em idades exatas (limite 120 no protótipo; backend 0..150) | 17 |
| F08 | Solicitar autorização | Tela Solicitar (Fato) | `POST /autorizadores/{n}` `200` vazio (EXEC B09) | RN5–RN7, RN8 | Modernizar (DIV-05) | 08, 18 |
| F09 | Resultado "Negado" | Cartão Negado (Fato) | `403` negativa de negócio (EXEC B11) | RN5; negativa **não** é falha técnica | Preservar | 08, 18 |
| F10 | Procedimento inexistente na solicitação | Protótipo não trata (Fato) | `500` NPE (EXEC B12) | — | Corrigir → `404` (DIV-01) | 08, 18 |
| F11 | Histórico de autorizações | Tela Autorizações (Fato) | `GET /autorizadores` (EXEC B15) | RN8, RN11 | Modernizar | 08, 19 |
| F12 | Painel: totais, idades cobertas, cobertura por idade, rosca M/F, últimas autorizações | Tela Painel (Fato) | **Ausente** (Fato); derivável dos dados existentes (Inferência) | agregações sobre catálogo e autorizações | **Desenvolver** (NEW-02) | 09, 20 |
| F13 | KPI "Aprovação %" | Contagem **em memória da sessão** (Fato) | Negativas não persistidas (RN8) | sem fonte confiável | Manter como indicador **da sessão** no frontend; persistir = PEN-01 | 20 |
| F14 | Estados carregando / vazio / erro / sucesso (overlay, modal, toast) | Presentes (Fato) | n/a | UX | Modernizar (sem latência simulada) | 15, 16, 21 |
| F15 | Atalho "Solicitar →" a partir da lista | Presente (Fato) | n/a | UX | Preservar | 17, 18 |
| F16 | Menu "Configurações" | Só na captura 1, ausente no HTML (Fato) | Ausente | — | **Fora de escopo** (PEN-06) | — |
| F17 | Auditoria de alterações (Envers) | — | `@Audited` (Fato) | RN10 | Preservar | 04, 07, 08 |
| F18 | CORS, health, logs, observabilidade | Rodapé "API conectada" sugere checagem de saúde (Inferência) | Ausentes (EXEC B16/B17) | NFR | Desenvolver | 06, 11, 21 |
| F19 | Documentação da API | — | springfox (Fato) | — | OpenAPI contract-first + UI | 05 |
| F20 | Dados de exemplo no ambiente local | Mesmas 3 linhas (Fato) | `DBServiceTests` no perfil `test` (Fato) | só desenvolvimento | Preservar (perfil `local`, fora do código de produção) | 04 |
| F21 | Login por e-mail e senha (JWT) | **Ausente** no protótipo (design derivado dos tokens e do shell) | Ausente (Fato) | acesso autenticado a todas as rotas — decisão do produto em 2026-11-08 | **Desenvolver** (NEW-04) | 22, 23 |
| F23 | Perfis / papéis | Ausente | Ausente | — | Fora de escopo (PEN-11) | — |
| F22 | Editar / excluir procedimento | Ausente | Ausente | — | Fora de escopo (PEN-05) | — |

## Cobertura inversa (toda spec rastreia a ≥ 1 funcionalidade)
01, 02, 03, 10, 13, 14, 40–43 → fundação/NFR · 04 → F17,F20 · 22, 23 → F21 · 05 → F19 + contratos de F01–F12 · 06, 11 → F18 · 07 → F01–F03 · 08 → F08–F11 · 09 → F12 · 12 → F04–F06 · 15, 16 → F14 · 17 → F01–F07, F15 · 18 → F08–F10, F15 · 19 → F11 · 20 → F12, F13 · 21 → F14 · 30–32 → todas · 50 → dados existentes (condicional).
