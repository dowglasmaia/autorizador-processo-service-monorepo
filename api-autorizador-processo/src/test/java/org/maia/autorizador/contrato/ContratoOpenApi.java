package org.maia.autorizador.contrato;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.Yaml;

/**
 * Validador mínimo de respostas contra o contrato OpenAPI 3.1 versionado em {@code openapi/}.
 *
 * <p>Evita nova dependência: o contrato usa um subconjunto pequeno do JSON Schema ({@code type} inclusive em lista,
 * {@code enum}, {@code required}, {@code properties}, {@code items}, {@code allOf}, limites numéricos e de tamanho,
 * {@code pattern}, {@code uniqueItems} e os formatos {@code date-time}, {@code uri}, {@code email}, {@code int32}).
 * Resolve {@code $ref} locais e relativos entre arquivos. Qualquer palavra-chave de esquema desconhecida faz o
 * validador falhar, para que um recurso novo do contrato não passe sem verificação.
 */
public final class ContratoOpenApi {

    private static final List<String> IGNORADAS = List.of("description", "examples", "example", "default", "title",
            "summary", "deprecated", "format", "xml", "externalDocs");
    private static final List<String> CONHECIDAS = List.of("type", "enum", "required", "properties", "items",
            "allOf", "minimum", "maximum", "minLength", "maxLength", "minItems", "maxItems", "pattern",
            "uniqueItems", "$ref", "additionalProperties");

    private final Map<String, Map<String, Object>> documentos = new HashMap<>();

    /** Referência resolvida: nó e o arquivo em que ele vive (para resolver $ref relativos aninhados). */
    private record No(String arquivo, Object valor) {
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> carregar(String arquivo) {
        return documentos.computeIfAbsent(arquivo, a -> {
            try (InputStream in = getClass().getResourceAsStream("/openapi/" + a)) {
                if (in == null) {
                    throw new IllegalStateException("Arquivo do contrato não encontrado: " + a);
                }
                return (Map<String, Object>) new Yaml().load(in);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private No resolver(String arquivo, Object valor) {
        No atual = new No(arquivo, valor);
        while (atual.valor() instanceof Map<?, ?> m && m.containsKey("$ref")) {
            String ref = (String) m.get("$ref");
            int cerquilha = ref.indexOf('#');
            String caminhoArquivo = cerquilha == 0 ? atual.arquivo()
                    : URI.create("/" + atual.arquivo()).resolve(ref.substring(0, cerquilha)).getPath().substring(1);
            Object alvo = carregar(caminhoArquivo);
            for (String parte : ref.substring(cerquilha + 2).split("/")) {
                alvo = ((Map<String, Object>) alvo).get(parte);
                if (alvo == null) {
                    throw new IllegalStateException("$ref sem destino: " + ref);
                }
            }
            atual = new No(caminhoArquivo, alvo);
        }
        return atual;
    }

    /** Localiza a operação declarada para o método e o caminho (com {@code {id}} no lugar dos parâmetros). */
    @SuppressWarnings("unchecked")
    private No operacao(String metodo, String rota) {
        for (String modulo : List.of("auth.yaml", "autorizacao.yaml", "painel.yaml", "procedimento.yaml")) {
            String arquivo = "modules/" + modulo;
            Map<String, Object> paths = (Map<String, Object>) carregar(arquivo).get("paths");
            Object item = paths.get(rota);
            if (item != null) {
                Object op = ((Map<String, Object>) item).get(metodo.toLowerCase());
                if (op != null) {
                    return new No(arquivo, op);
                }
            }
        }
        throw new AssertionError("Operação não declarada no contrato: " + metodo + " " + rota);
    }

    /**
     * Valida status, cabeçalhos exigidos, tipo de conteúdo e corpo de uma resposta.
     *
     * @return lista de violações; vazia quando a resposta respeita o contrato
     */
    @SuppressWarnings("unchecked")
    public List<String> validarResposta(String metodo, String rota, int status, String contentType,
            Map<String, String> cabecalhos, Object corpo) {
        List<String> erros = new ArrayList<>();
        No op = operacao(metodo, rota);
        Map<String, Object> respostas = (Map<String, Object>) ((Map<String, Object>) op.valor()).get("responses");
        Object declarada = respostas.get(String.valueOf(status));
        if (declarada == null) {
            return List.of("status " + status + " não declarado em " + metodo + " " + rota + " (declarados: "
                    + respostas.keySet() + ")");
        }
        No resposta = resolver(op.arquivo(), declarada);
        Map<String, Object> mapa = (Map<String, Object>) resposta.valor();

        Map<String, Object> exigidos = (Map<String, Object>) mapa.getOrDefault("headers", Map.of());
        for (String nome : exigidos.keySet()) {
            boolean presente = cabecalhos.keySet().stream().anyMatch(c -> c.equalsIgnoreCase(nome));
            if (!presente) {
                erros.add("cabeçalho declarado ausente: " + nome);
            }
        }

        Map<String, Object> conteudo = (Map<String, Object>) mapa.get("content");
        if (conteudo == null) {
            if (corpo != null) {
                erros.add("a resposta " + status + " não declara corpo, mas retornou um");
            }
            return erros;
        }
        String tipo = contentType == null ? "" : contentType.split(";")[0].trim();
        Object midia = conteudo.get(tipo);
        if (midia == null) {
            erros.add("content-type '" + tipo + "' não declarado (declarados: " + conteudo.keySet() + ")");
            return erros;
        }
        Object esquema = ((Map<String, Object>) midia).get("schema");
        validar(resposta.arquivo(), esquema, corpo, "$", erros);
        return erros;
    }

    /** Valores do enum {@code code} de {@code ProblemDetail}: a lista oficial de códigos de erro do contrato. */
    @SuppressWarnings("unchecked")
    public List<String> codigosDeErro() {
        Map<String, Object> esquemas = (Map<String, Object>) ((Map<String, Object>) carregar("common/responses.yaml")
                .get("components")).get("schemas");
        Map<String, Object> problema = (Map<String, Object>) esquemas.get("ProblemDetail");
        Map<String, Object> code = (Map<String, Object>) ((Map<String, Object>) problema.get("properties")).get("code");
        return (List<String>) code.get("enum");
    }

    /** Valida um valor diretamente contra um esquema nomeado de {@code common/responses.yaml}. */
    public List<String> validarContraEsquemaComum(String nomeEsquema, Object corpo) {
        List<String> erros = new ArrayList<>();
        validar("common/responses.yaml", Map.of("$ref", "#/components/schemas/" + nomeEsquema), corpo, "$", erros);
        return erros;
    }

    @SuppressWarnings("unchecked")
    private void validar(String arquivo, Object esquemaBruto, Object valor, String caminho, List<String> erros) {
        No no = resolver(arquivo, esquemaBruto);
        Map<String, Object> esquema = (Map<String, Object>) no.valor();
        String origem = no.arquivo();
        for (String chave : esquema.keySet()) {
            if (!CONHECIDAS.contains(chave) && !IGNORADAS.contains(chave)) {
                throw new IllegalStateException("Palavra-chave de esquema sem suporte no validador: " + chave
                        + " (" + caminho + "). Estenda ContratoOpenApi.");
            }
        }
        if (esquema.containsKey("allOf")) {
            for (Object parte : (List<Object>) esquema.get("allOf")) {
                validar(origem, parte, valor, caminho, erros);
            }
        }
        if (esquema.containsKey("type") && !tipoConfere(esquema.get("type"), valor)) {
            erros.add(caminho + ": tipo esperado " + esquema.get("type") + " mas veio " + descrever(valor));
            return;
        }
        if (valor == null) {
            return;
        }
        if (esquema.containsKey("enum") && !((List<Object>) esquema.get("enum")).contains(valor)) {
            erros.add(caminho + ": valor '" + valor + "' fora do enum " + esquema.get("enum"));
        }
        if (valor instanceof Number n) {
            comparar(esquema, "minimum", n, caminho, erros, true);
            comparar(esquema, "maximum", n, caminho, erros, false);
            formatoInteiro(esquema, n, caminho, erros);
        }
        if (valor instanceof String s) {
            validarTexto(esquema, s, caminho, erros);
        }
        if (valor instanceof List<?> lista) {
            validarLista(origem, esquema, lista, caminho, erros);
        }
        if (valor instanceof Map<?, ?> objeto) {
            validarObjeto(origem, esquema, (Map<String, Object>) objeto, caminho, erros);
        }
    }

    private void validarTexto(Map<String, Object> esquema, String s, String caminho, List<String> erros) {
        if (esquema.get("minLength") instanceof Number min && s.length() < min.intValue()) {
            erros.add(caminho + ": menor que minLength " + min);
        }
        if (esquema.get("maxLength") instanceof Number max && s.length() > max.intValue()) {
            erros.add(caminho + ": maior que maxLength " + max);
        }
        if (esquema.get("pattern") instanceof String p && !Pattern.compile(p).matcher(s).find()) {
            erros.add(caminho + ": '" + s + "' não casa com o padrão " + p);
        }
        String formato = (String) esquema.get("format");
        if ("date-time".equals(formato)) {
            try {
                Instant.parse(s);
            } catch (RuntimeException e) {
                erros.add(caminho + ": '" + s + "' não é date-time RFC 3339 em UTC");
            }
        } else if ("uri".equals(formato)) {
            try {
                URI.create(s);
            } catch (RuntimeException e) {
                erros.add(caminho + ": '" + s + "' não é URI");
            }
        } else if ("email".equals(formato) && !s.matches("[^@\\s]+@[^@\\s]+")) {
            erros.add(caminho + ": '" + s + "' não é e-mail");
        }
    }

    private void validarLista(String origem, Map<String, Object> esquema, List<?> lista, String caminho,
            List<String> erros) {
        if (esquema.get("minItems") instanceof Number min && lista.size() < min.intValue()) {
            erros.add(caminho + ": menos itens que minItems " + min);
        }
        if (esquema.get("maxItems") instanceof Number max && lista.size() > max.intValue()) {
            erros.add(caminho + ": mais itens que maxItems " + max);
        }
        if (Boolean.TRUE.equals(esquema.get("uniqueItems")) && lista.stream().distinct().count() != lista.size()) {
            erros.add(caminho + ": itens repetidos com uniqueItems");
        }
        if (esquema.containsKey("items")) {
            for (int i = 0; i < lista.size(); i++) {
                validar(origem, esquema.get("items"), lista.get(i), caminho + "[" + i + "]", erros);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void validarObjeto(String origem, Map<String, Object> esquema, Map<String, Object> objeto,
            String caminho, List<String> erros) {
        for (Object obrigatorio : (List<Object>) esquema.getOrDefault("required", List.of())) {
            if (!objeto.containsKey(obrigatorio)) {
                erros.add(caminho + ": propriedade obrigatória ausente '" + obrigatorio + "'");
            }
        }
        Map<String, Object> declaradas = (Map<String, Object>) esquema.get("properties");
        if (declaradas == null) {
            return;
        }
        for (Map.Entry<String, Object> campo : objeto.entrySet()) {
            Object sub = declaradas.get(campo.getKey());
            if (sub == null) {
                // Propriedade fora do contrato quebraria a promessa dos tipos gerados: falha.
                if (!Boolean.TRUE.equals(esquema.get("additionalProperties"))) {
                    erros.add(caminho + ": propriedade não declarada no contrato '" + campo.getKey() + "'");
                }
            } else if (campo.getValue() == null && !obrigatorias(esquema).contains(campo.getKey())) {
                // O contrato descreve propriedades opcionais do catálogo como "ausente (nulo)" (procedimento.yaml,
                // Procedimento.nome): a API as serializa como null. Aceito como ausência; ver proposta da etapa 27.
                continue;
            } else {
                validar(origem, sub, campo.getValue(), caminho + "." + campo.getKey(), erros);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Object> obrigatorias(Map<String, Object> esquema) {
        return (List<Object>) esquema.getOrDefault("required", List.of());
    }

    private static void comparar(Map<String, Object> esquema, String chave, Number valor, String caminho,
            List<String> erros, boolean minimo) {
        if (esquema.get(chave) instanceof Number limite) {
            double v = valor.doubleValue();
            if (minimo ? v < limite.doubleValue() : v > limite.doubleValue()) {
                erros.add(caminho + ": " + valor + (minimo ? " abaixo de " : " acima de ") + chave + " " + limite);
            }
        }
    }

    private static void formatoInteiro(Map<String, Object> esquema, Number n, String caminho, List<String> erros) {
        if ("int32".equals(esquema.get("format")) && (n.longValue() < Integer.MIN_VALUE
                || n.longValue() > Integer.MAX_VALUE)) {
            erros.add(caminho + ": " + n + " não cabe em int32");
        }
    }

    private static boolean tipoConfere(Object tipo, Object valor) {
        if (tipo instanceof List<?> tipos) {
            return tipos.stream().anyMatch(t -> tipoConfere(t, valor));
        }
        return switch ((String) tipo) {
            case "null" -> valor == null;
            case "string" -> valor instanceof String;
            case "integer" -> valor instanceof Integer || valor instanceof Long
                    || valor instanceof java.math.BigInteger;
            case "number" -> valor instanceof Number;
            case "boolean" -> valor instanceof Boolean;
            case "array" -> valor instanceof List<?>;
            case "object" -> valor instanceof Map<?, ?>;
            default -> throw new IllegalStateException("Tipo sem suporte: " + tipo);
        };
    }

    private static String descrever(Object valor) {
        return valor == null ? "null" : valor.getClass().getSimpleName() + "(" + valor + ")";
    }
}
