# Tela de procedimentos

Etapa 20 de 31 · Fase 3 · derivada da `SPEC-17` (`.specs/03-frontend/SPEC-17-feature-procedimentos.md`). Escrita antes da entrega da etapa 19; será revisada contra o que ela entregar.

## Objetivo
Entregar `/procedimentos` (lista) e `/procedimentos/novo` (cadastro em 4 etapas) com todos os campos do catálogo estendido persistidos pelo backend, no fluxo: cadastrar → voltar à lista com o novo item e toast.

## Ponto de partida (conferido no repositório; etapa 19 ainda em curso)
- `features/procedimentos/` tem só `procedimentos.routes.ts` (rota `''`, lazy) e `pages/procedimentos-page.ts`, uma página-esqueleto com `EmptyState` "Em construção". Não há serviço, `doc.md` nem testes.
- `app.routes.ts` já protege `procedimentos` com `authGuard` (etapa 18), dentro do `Shell`.
- Núcleo da etapa 17 disponível: `ApiClient`, `AppError` (`kind`, `code`, `campos`), `errorInterceptor`, `NotificationService`, `ErrorDialogService.show(msg, retry?)`, `LoadingService`; tipos gerados em `shared/api` (`ProcedimentoRequest`, `ProcedimentoDto`, `PaginaProcedimentos`).
- `shared/ui` (etapa 16): Button, Field, Card, Chip, Tag, DataTable, Dialog, EmptyState, PageHeader, Skeleton, Icon.
- Contrato: `listarProcedimentos` (paginado, `q` por prefixo de `numProcedimento`) e `cadastrarProcedimento` (`nome` e `categoria` obrigatórios; enums `Categoria`, `Especialidade`, `TipoCobertura`, `Periodo`, `Sexo`; campos do catálogo ausentes em registros antigos).
- A etapa 19 (Nginx) não toca código Angular; espera-se nenhum impacto aqui. `.specs/06-execution-plan/` não existe; desvio a registrar na evidência.

## Escopo
1. Rotas lazy `''` e `novo` e `doc.md` do módulo (padrão `angular-module-scaffold`).
2. `data-access/procedimentos.service.ts` (`listarProcedimentos`, `cadastrarProcedimento`) com Signals `itens`, `carregando`, `erro`, paginação e busca `q`.
3. Página de lista: esqueleto, vazio com CTA, erro, sucesso; tabela responsiva (Nº + nome, Categoria, Sexos em tags, Idades "N anos", ID); "Sem nome" quando `nome` nulo; "Solicitar →" para `/solicitar?numero=n`; botão "Novo procedimento".
4. Página de cadastro em 4 etapas com chips de navegação, Voltar/Avançar, `FormGroup` tipado por etapa, validação por etapa e resumo antes de salvar: 1 Identificação · 2 Cobertura · 3 Elegibilidade · 4 Utilização e autorização (as duas flags pré-selecionadas).
5. Funções puras testadas: `faixaParaIdades(min, max)` (0..150), `ambosParaSexos`, validação de `quantidadeMaxima`/`periodo`; chips removíveis para ajuste individual de idades; aviso (não bloqueio) sem idade ou sexo (RN9); texto de apoio de que cobertura/quantidade/prévia/justificativa não alteram a autorização (PEN-02); sem a nota "conceituais".
6. Erros: `PROCEDIMENTO_DUPLICADO` no campo número; `VALIDACAO_INVALIDA` mapeia `campos` e leva à etapa do campo; rede/servidor → modal com "Tentar novamente" e formulário preservado; toast "Procedimento n cadastrado" e redirecionamento à lista.
7. Testes de serviço e componentes (todos os estados e erros) e `doc.md` com as operações consumidas.

## Fora do escopo (fica para outras etapas)
- Tela de solicitação (etapa 21), autorizações e painel; microcopy/a11y global (SPEC-21); e2e e capturas finais contra o Compose (SPEC-30).
- Edição/exclusão de procedimento, regra de autorização no cliente, alterações no backend ou no contrato.

## Critérios de aceitação
- *Given* a API com seed · *When* abrir `/procedimentos` · *Then* 3 linhas.
- *When* cadastrar "Ressonância magnética do crânio", código 9999, Exames de imagem, idades 18–20, sexo Ambos · *Then* o POST envia `idades=[18,19,20]`, `sexos=["M","F"]` e a lista mostra o item com toast.
- *When* repetir o código · *Then* erro no campo código.
- *Given* API fora do ar · *Then* modal com "Tentar novamente" e formulário preservado.
- `npm run lint`, `npm run build` e `npm test` verdes; sem `any`.

## Decisões tomadas sem consulta
- **Rótulos dos enums:** o protótipo lista as categorias/especialidades; os rótulos em português vêm de um mapa local sobre os enums gerados (sem duplicar DTOs). [REVISAR]
- **Campos opcionais:** "Código TUSS", "especialidade", "cobertura", "quantidade/período" ficam opcionais; só nome, número e categoria são obrigatórios, como no contrato.
- **Limite de idades 0..150**, alinhado ao backend (o protótipo usava 120), conforme a SPEC.
- **Execução contra o Compose:** adiada para a SPEC-30; aqui valem testes automatizados com `HttpTestingController`; captura em 320/1280 px só se o ambiente permitir, registrando o resultado. [REVISAR]
- **Estado:** formulário local ao componente; lista no serviço da funcionalidade, recarregada ao voltar do cadastro.
- **Dependência da etapa 19:** nenhuma de código; a revisão confirmará que o front segue intacto.
