# SPEC-17 — Funcionalidade Procedimentos (lista e cadastro em 4 etapas)

| Campo | Valor |
|---|---|
| Domínio / projeto | Frontend · `features/procedimentos` |
| Prioridade | P1 |
| Depende de | SPEC-15, SPEC-16, SPEC-23, SPEC-07, SPEC-12 |
| Sucessoras | SPEC-21, SPEC-30 |
| Paralelismo | junto com SPEC-18, SPEC-19 |

## 1. Objetivo
Entregar a lista de procedimentos e o formulário **Cadastrar procedimento** completo do protótipo, agora com **todos os campos persistidos** pelo backend (SPEC-12). Fluxo funcional completo: cadastrar → voltar à lista com o novo item e toast.
**Telas (protótipo):** `/procedimentos` — tabela Nº (+nome), Categoria, Sexos (tags), Idades ("N anos"), ID, ação "Solicitar →"; botão "Novo procedimento"; vazio com CTA. `/procedimentos/novo` — 4 etapas com chips de navegação: **1 Identificação** (nome*, código interno* = `numProcedimento`, código TUSS, categoria*, especialidade) · **2 Cobertura** (plano / regras / não coberto) · **3 Elegibilidade** (idade mínima, máxima, sexo Masculino/Feminino/Ambos) · **4 Utilização e autorização** (quantidade máxima, período, exige autorização prévia, exige justificativa médica; as duas últimas pré-selecionadas como no protótipo).
**Regras de UI (sem alterar regra de negócio):** faixa mínima–máxima é **expandida em idades exatas** (RN6/ADR-12; limites 0..150; o protótipo usava 120 — alinhado ao backend), permitindo também ajuste individual por chips removíveis; "Ambos" → `["M","F"]`; aviso (não bloqueio) quando nenhuma idade ou sexo (RN9); a nota "demais campos são conceituais" do protótipo **é removida** (agora são reais); informar que cobertura/quantidade/prévia/justificativa **não alteram a autorização** (PEN-02) em texto de apoio.
**Erros:** `PROCEDIMENTO_DUPLICADO` junto ao campo número (mensagem do backend por `code`); `VALIDACAO_INVALIDA` mapeia `errors[campo]` aos campos e leva à etapa correspondente; rede/servidor → modal "Não foi possível…" com nova tentativa e **dados do formulário preservados**.
**Restrições:** Reactive Forms tipados; estado de formulário local, dados da lista no serviço da funcionalidade; sem lógica de autorização no cliente.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** protótipo (telas Procedimentos/Novo e `uploads/pasted-1793970857591-0.png`), `API_CONTRACTS.md` (procedimento + NEW-01), `FUNCTIONAL_TRACEABILITY.md` F01–F07, F15; skill `angular-module-scaffold` (`module-structure.md`, `module-example.md`)

## 3. Tarefas
- [ ] **T1.** Rotas `procedimentos.routes.ts` (lazy) e `doc.md` do módulo.
- [ ] **T2.** `data-access/procedimentos.service.ts` (`listarProcedimentos`, `cadastrarProcedimento`) com Signals (`itens`, `carregando`, `erro`), paginação e busca `q` por número.
- [ ] **T3.** Página de lista: estados carregando (esqueleto), vazio, erro, sucesso; tabela responsiva; exibir "Sem nome" quando `nome` nulo; atalho "Solicitar →" navegando para `/solicitar?numero=n`.
- [ ] **T4.** Página de cadastro em 4 etapas (`FormGroup` tipado por etapa), navegação por chips/Voltar/Avançar, validação por etapa, resumo antes de salvar na etapa 4.
- [ ] **T5.** Funções puras testadas: `faixaParaIdades(min, max)`, `ambosParaSexos`, validações de combinação `quantidadeMaxima/periodo`.
- [ ] **T6.** Tratamento de erros conforme §1; toast "Procedimento n cadastrado"; redirecionar à lista.
- [ ] **T7.** Testes de componentes/serviço (todos os estados e erros) e atualização do `doc.md` com operações consumidas (`listarProcedimentos`, `cadastrarProcedimento`).

**Aceite (G/W/T):** *Given* a API com seed · *When* abrir `/procedimentos` · *Then* 3 linhas. *When* cadastrar nome "Ressonância magnética do crânio", código 9999, categoria Exames de imagem, idades 18–20, sexo Ambos · *Then* POST envia `idades=[18,19,20]`,`sexos=["M","F"]` e a lista exibe o item. *When* repetir o código · *Then* erro no campo código. *Given* API fora do ar · *Then* modal com "Tentar novamente" e formulário preservado.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T7 | `angular-module-scaffold` (obrigatória), `angular-architect` |
| T3–T4 (visual) | `angular-tailwind-ui-specialist`, `ui-ux-pro-max` |
| rótulos, ajudas e mensagens | `copywriting` |

## 5. Verificação obrigatória
- `npm run build && npm test && npm run lint` → verdes
- Execução manual/automatizada contra `docker compose up` (SPEC-10/13): fluxo do aceite, com capturas em 320 e 1280 px
- Definition of Done: operações do contrato cobertas; estados explícitos; `doc.md` atualizado; sem `any`.

## 6. Resultado
_Preencher ao concluir._
