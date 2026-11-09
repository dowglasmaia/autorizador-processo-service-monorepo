# Padrão da ferramenta: loops, planos de execução e organização (CLAUDE.md)

> Este é o **padrão único da ferramenta** para criar loops e planos de execução, igual em qualquer projeto. Ela o entrega a **toda
> sessão do Claude** que aciona (planejar, propor, aplicar, corrigir, recuperar). Fica em `.specs/workflows/CLAUDE.md`, criado com o
> primeiro plano a partir do modelo que acompanha a ferramenta. Um projeto pode ajustá-lo: a versão do projeto é a que vale.

## 1. Estrutura padrão

```
.specs/
  memory/                      conhecimento acumulado do projeto
  shared/                      instruções, modelos e padrões (SPEC_TEMPLATE.md, como-executar.md)
  <specs>                      as especificações do projeto
  workflows/
    CLAUDE.md                  este padrão
    ferramentas/<assunto>/     material de apoio compartilhado (scripts, baselines, relatórios, dados)
    <loop>/                    um loop: loop.json, plano.md, historico.jsonl, specs/NN-assunto/, specs/arquivo/
      .execucao/               efêmero e fora do git (inclui tmp/)
```

Nada de planejamento, controle ou apoio fora de `.specs/`. Só código e configuração **do produto** ficam fora dela, onde o projeto já
os mantém.

## 2. Como criar um loop e seu plano

- **Nome do loop:** kebab-case, curto, descreve o objetivo (ex.: `app-mobile`). `ferramentas` é reservado.
- **Um loop, um objetivo.** O plano divide o objetivo em **fases** (2 a 5) e **etapas** (em geral 3 a 20), em ordem de dependência:
  fundamentos primeiro, acabamento por último.
- **Uma spec por etapa.** Cada etapa tem `titulo` curto, `assunto` (2 a 4 palavras em kebab-case, sem acentos) e `descricao` de 2 a 5
  linhas (o que entrega, referências e limites). Cada etapa cabe numa entrega de até umas 30 tarefas pequenas.
- **Verificações:** os comandos que o próprio projeto já usa (build, lint, tipos, testes), cada um com a pasta de onde roda.
- **Plano único por objetivo.** Antes de planejar, procure um plano existente (mesmo nome ou mesmo objetivo) e **reaproveite-o ou
  atualize-o** (replanejar) em vez de criar outro. A ferramenta já faz essa checagem.

## 3. Material de apoio: `.specs/workflows/ferramentas/<assunto>/`

- Uma subpasta por assunto, em kebab-case (ex.: `contrato/`, `paridade/`, `desempenho/`, `docker/`).
- Cada subpasta tem um `README.md` com: para que serve, **o comando completo para rodar a partir da raiz do projeto**, o que produz
  e quem a usa (CI, outros scripts).
- Os scripts descobrem a raiz do projeto pelo próprio caminho (relativo ao arquivo), nunca pelo diretório de onde são chamados.
- Se o CI ou outro script usa um desses arquivos, ele referencia o caminho completo em `.specs/workflows/ferramentas/...`, e quem
  mover um arquivo atualiza todas as referências.
- Material de uma spec só fica na pasta dessa spec.

## 4. Temporários

- Dumps, saídas de comandos, rascunhos e bancos descartáveis vão em `.specs/workflows/<loop>/.execucao/tmp`. A pasta não é
  versionada e a ferramenta a apaga a cada iteração.
- O que criar temporariamente em outro lugar, apague antes de terminar.

## 5. O que nunca fazer

- **Não crie `tools/`, `scripts/` nem outra pasta de apoio na raiz do repositório**, nem scripts soltos na raiz. Se uma spec ou o
  plano indicar um caminho assim, use o equivalente em `.specs/workflows/ferramentas/<assunto>/` e registre a troca em "Decisões
  tomadas sem consulta". Scripts que fazem parte do **produto** (ex.: um `npm run` dentro do próprio projeto Angular) ficam no
  projeto que os usa.
- Não dê a um loop o nome `ferramentas`.
- Não escreva em `loop.json` nem em `.execucao/`, exceto em `tmp/`.

## 6. Reaproveitar antes de criar

- Antes de criar um plano, spec, relatório ou script, procure em `.specs/` se já existe um equivalente e **atualize-o** em vez de
  gerar um segundo.
