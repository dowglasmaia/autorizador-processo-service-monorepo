# CURRENT_STATE_ANALYSIS — diagnóstico do backend original e do protótipo

Evidências: [EXEC] sondagem em JDK 8 (ver `BASELINE_COMPORTAMENTO_ORIGINAL.md`) · [CÓDIGO] leitura · [PROTÓTIPO] leitura de `.specs/design/prototype/Autorizador.dc.html` e das capturas.

## 1. Backend original

### 1.1 Stack e estrutura [CÓDIGO]
Spring Boot 2.2.6 · Java 8 · WAR (`ServletInitializer`) · Spring Data JPA · Lombok · ModelMapper 2.3.0 (declarado, **não usado**) · springfox 2.6.1 · Hibernate Envers · MySQL (dev) e H2 em arquivo `~/test` (perfil ativo padrão `test`) · `spring-boot-devtools`. Pacote `org.maia.procedimentosapi`: `resources` (controllers), `services` (+`impl`, `interfaces`, `execeptions`), `repository`, `domain` (+`enums`), `config`, `resource.exeception`. Camadas técnicas clássicas.

### 1.2 Fluxo real
- `ProcedimentoResources.save` → `ProcedimentoServiceImpl.save`: `findByNumProcedimento` (check-then-insert, **sujeito a corrida**; a restrição UNIQUE do banco é a única garantia) → `403` se existe.
- `SolicitarProcedimentoService.save`: carrega procedimento; **NPE** quando nulo (B12); percorre `getSexos()` e `getIdades()`; grava `SolicitarProcedimento(procedimento, true)` ou lança `RequestNotAutorizationExecption` (`403`).
- `ObjectNotFoundExecption` (`404`) existe e **nunca é lançada**. `ProcedimentoService.getById` lança `RuntimeException` genérica (não exposta).

### 1.3 Defeitos e riscos confirmados
| ID | Achado | Evidência | Tratamento |
|---|---|---|---|
| D1 | `500`/NPE para procedimento inexistente; vaza stack trace | EXEC B12 | DIV-01, DIV-04 |
| D2 | Código de sexo inválido aceito e **quebra `GET /procedimentos`** | EXEC B06/B07 | DIV-02 |
| D3 | Entrada de sexo numérica × saída textual | EXEC B03 | DIV-02 |
| D4 | Erros com formatos distintos e erros de grafia nas mensagens | EXEC | DIV-04 (mensagens corrigidas, `code` estável) |
| D5 | Sem CORS | EXEC B16 | SPEC-06 |
| D6 | Sem health/actuator | EXEC B17 | SPEC-11 |
| D7 | `idades`/`sexos` sem PK/UNIQUE (duplicatas possíveis no banco) | EXEC DDL | DATABASE_DESIGN (PK composta) |
| D8 | Corrida na checagem de duplicidade | CÓDIGO | capturar violação de UNIQUE → `403 PROCEDIMENTO_DUPLICADO` |
| D9 | Credenciais MySQL versionadas em `application-dev.properties` | CÓDIGO | não migrar; variáveis de ambiente |
| D10 | `id` no corpo do POST pode virar merge | INFERÊNCIA | DTO de entrada sem `id` |
| D11 | `@Entity` usada como DTO e serializada; `spring.jpa.open-in-view=true` (dev) | CÓDIGO | DTOs gerados; `open-in-view=false` |
| D12 | Seed de teste (`DBServiceTests`) em `src/main`, ativado por perfil | CÓDIGO | seed só em perfil `local`, fora do código de produção |
| D13 | Testes: `@ActiveProfiles("tests")` (perfil inexistente), dependem de estado compartilhado | CÓDIGO | reescritos |

### 1.4 Dependências sem uso / obsoletas
ModelMapper (sem uso), devtools, springfox (incompatível com Boot ≥ 2.6), driver MySQL (deixa de existir), H2 (deixa de existir em testes: usar PostgreSQL com Testcontainers).

## 2. Banco
Ver §2 do baseline. Hoje: coleções `sexos`/`idades` como tabelas sem chave; `solicitar_procedimento` sem FK nem UNIQUE; auditoria Envers com `revinfo` própria. Não há migrations, nem dados persistentes comprovados (dev usa `create-drop`; teste usa H2 seedado). **Não há evidência de dados de produção** → migração de dados é condicional (SPEC-50).

## 3. Protótipo [PROTÓTIPO]
Artefato `Autorizador.dc.html` (+ `support.js`, design system "Broadsheet", 2 capturas). É um protótipo **com estado em memória e latência simulada** (`loadingMs`, `simulateError`, `startEmpty`), não um cliente da API.

### 3.1 Layout e navegação
Barra lateral escura 250px (marca "Autorizador · Procedimentos médicos", 4 itens: Painel, Procedimentos, Solicitar, Autorizações; rodapé "API conectada · localhost:8080 · perfil test"). Conteúdo à direita com gradientes decorativos. Captura 1 mostra variante com 5 itens (inclui **Configurações**) e KPIs diferentes (**sem equivalente no HTML** → PEN-06).

### 3.2 Telas
1. **Painel:** cabeçalho com data por extenso; hero escuro "Autorize um procedimento em segundos" + botão "Nova solicitação"; 4 KPIs (Procedimentos, Autorizações, Idades cobertas, Aprovação %—contagem **da sessão**); "Cobertura por idade" (barras: nº de procedimentos que aceitam cada idade); "Sexos aceitos" (rosca M × F); "Últimas autorizações" (4, link "Ver todas").
2. **Procedimentos:** tabela Nº (+ nome opcional) · Sexos (tags) · Idades (tags "N anos") · ID · botão "Solicitar →"; botão "Novo procedimento"; estado vazio com CTA.
3. **Cadastrar procedimento (4 etapas, chips clicáveis):** (1) **Identificação:** nome*, código interno* (= `numProcedimento`), código TUSS, categoria*, especialidade; (2) **Cobertura:** tipo (plano / regras / não coberto); (3) **Elegibilidade:** idade mínima, máxima, sexo (Masculino/Feminino/Ambos); (4) **Utilização e autorização:** quantidade máxima, período (mês/ano/vida), exige autorização prévia, exige justificativa médica. Validações: nome, número, duplicidade ("Procedimento já cadastrado na base de dados: Nº n"). O intervalo de idades é **expandido em idades exatas** (limite 120). Nota do próprio protótipo: demais campos são "conceituais".
4. **Solicitar autorização:** select de procedimento ("Nº n"), idade, sexo (chips M/F); resultado ao lado: **Autorizado** (ícone, texto "Procedimento n liberado para paciente … de N anos", link histórico) ou **Negado** ("A API não informa se falhou o sexo ou a idade"); estado "O resultado aparece aqui."; botões Solicitar / Limpar.
5. **Autorizações:** tabela ID · Procedimento · Regras · Status "Autorizado"; vazio "Nada por aqui ainda" + CTA.

### 3.3 Estados transversais
Overlay de carregamento por navegação/ação (animação de 3 círculos, texto "Carregando painel/procedimentos…", "Salvando procedimento", "Verificando elegibilidade"); **modal de erro** "Não foi possível carregar — O servidor não respondeu. Seus dados não foram alterados." com Fechar/Tentar novamente; **toast** de sucesso (3,2 s); animações com `prefers-reduced-motion`.

### 3.4 Identidade visual (efetiva)
Os tokens locais **sobrescrevem** o Broadsheet: fonte **Manrope** (400–800); fundo `#f4fafa`, texto `#0b2a2f`, acento teal `#0fa88c` (rampa 100–900), acento 2 coral `#e5604a`; sidebar gradiente `#03242b→#021a20`; cartões brancos com raio 20, botões pílula, inputs raio 10, diálogos raio 22; ícones Phosphor duotone. A prosa do Broadsheet (serifa, raio 2, "sem caixas") **não** vale onde o protótipo a contradiz. `[DECISÃO]` tokens do protótipo prevalecem (SPEC-15).

### 3.5 Responsividade e acessibilidade observadas
Grid `auto-fit`, `flex-wrap`; **sem** breakpoint para a sidebar fixa (250px) → a v1 precisa definir menu recolhível (SPEC-15). Chips são `button`; faltam `aria-live`, foco visível padronizado, semântica de tabela responsiva → tratados em SPEC-15/21.

### 3.6 Artefatos do protótipo sem correspondência funcional
Latência simulada, `simulateError`, contador de negativas em memória, `id` calculado no cliente, mapa `nome` (não existe no backend) → descartar ou substituir por dados reais.
