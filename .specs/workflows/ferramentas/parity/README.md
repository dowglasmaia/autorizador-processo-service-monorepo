# Paridade de comportamento (golden cases)

Torna executável o baseline da versão original (casos B01–B17) para provar que a nova API
(v1) preserva o comportamento, exceto nas divergências (DIV) aceitas.

- `cases.json`: casos B01–B17 (requisição e resposta da original, `mapeamentoV1`, `divergencia`) e a seção
  `adicionais` (casos só da v1: A01 `sexo:"Masculino"` → 400, DIV-08; A02 rota sem token → 401).
  As convenções de comparação estão no campo `convencoes`.
- `replay.mjs`: Node ≥ 20, sem dependências (`fetch` nativo).

## Subir a versão original

A original usa H2 em memória e **não tem rota de remoção**: cada execução exige um contêiner novo.

```bash
T=$(mktemp -d)
git archive v0-original | tar -x -C "$T"
MSYS_NO_PATHCONV=1 docker run -d --name baseline-original -p 18080:8080 \
  -v "$(cygpath -w "$T"):/app" -w /app maven:3.6.3-jdk-8 \
  mvn -q spring-boot:run -Dspring-boot.run.profiles=test -Dserver.port=8080
# aguarde: curl http://localhost:18080/procedimentos  -> 200 com 1234, 4567, 6789
```

Ao terminar: `docker rm -f baseline-original` e apague o diretório temporário.

## Replicar

```bash
# contra a original
node .specs/workflows/ferramentas/parity/replay.mjs --target original --base-url http://localhost:18080

# contra a v1 (faz login em /api/v1/auth/login e usa o Bearer)
node .specs/workflows/ferramentas/parity/replay.mjs --target v1 --base-url http://localhost:8080 --email <e> --senha <s>
```

Opções: `--only procedimento|autorizacao|infra` limita a um grupo; `--email`/`--senha` também
podem vir de `PARITY_EMAIL`/`PARITY_SENHA`. Saída `0` = todos OK; `1` = falha de caso ou base suja;
`2` = erro de uso ou de conexão.

Os casos rodam na ordem do campo `ordem`; B06, B07 e B12 ficam por último porque sujam a base.
Antes de executar, o script confere a semente; se a base não estiver limpa, avisa e sai com `1`
(na original, reinicie o contêiner). Na v1 ele tenta remover os procedimentos 9999, 9998 e 9997 ao final.

## Política de divergências (DIV)

Cada caso com `divergencia: DIV-xx` tem comportamento intencionalmente diferente na v1
(ver `BASELINE_COMPORTAMENTO_ORIGINAL.md` e `API_CONTRACTS.md` §4). O `mapeamentoV1` já descreve o
resultado esperado **na v1**; a original é validada pelo bloco `original`. Uma diferença nova que não esteja
catalogada como DIV é regressão: corrija a v1 ou registre uma DIV com aprovação.

## Adicionar um caso

1. Sonde a original com `curl` e anote status e campos estáveis (ignore `timestamp`, `trace`, ids, `Location`).
2. Acrescente em `casos` (ou em `adicionais`, com `"apenasV1": true`) o objeto com `id`, `grupo`, `ordem`,
   `descricao` (Dado/quando/então), `original.requisicao/resposta`, `mapeamentoV1` e `divergencia`.
3. Use `ordem` alta se o caso deixar a base suja. Rode contra um contêiner novo e confirme tudo OK.

## Saída resumida (original, contêiner novo)

```
caso         · esperado · obtido · resultado
B01 (DIV-07) · 200      · 200    · OK
B02 (DIV-03) · 201      · 201    · OK
...
B07 (DIV-02) · 500      · 500    · OK

17/17 OK
```

Repetindo sem reiniciar o contêiner: `AVISO: a base não está limpa ...`, B01/B02/B06 em FALHA e saída `1`.

## Uso contra a v1 (`api-autorizador-processo`)

```bash
# .env descartável (fora do git): API_PORT, POSTGRES_PASSWORD, JWT_SECRET, AUTH_BOOTSTRAP_EMAIL, AUTH_BOOTSTRAP_SENHA
docker compose -p paridade --env-file <arquivo.env> up -d --build --wait postgres api   # ou docker-compose
node .specs/workflows/ferramentas/parity/replay.mjs --target v1 --base-url http://localhost:<API_PORT> --email <e> --senha <s>
docker compose -p paridade --env-file <arquivo.env> down -v
```

Sempre rode em base recém-criada (o perfil `local` traz a seed 1234, 4567, 6789). O resultado da SPEC-31 está em `RELATORIO.md`.
O job `paridade` do pipeline de CI **fica para a SPEC-40**; aqui só o comando acima está documentado.
