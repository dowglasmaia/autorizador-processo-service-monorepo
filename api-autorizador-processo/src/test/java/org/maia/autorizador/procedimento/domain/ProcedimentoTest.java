package org.maia.autorizador.procedimento.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.maia.autorizador.procedimento.domain.model.Categoria;
import org.maia.autorizador.procedimento.domain.model.DadosCatalogo;
import org.maia.autorizador.procedimento.domain.model.Especialidade;
import org.maia.autorizador.procedimento.domain.model.FaixaEtaria;
import org.maia.autorizador.procedimento.domain.model.Periodo;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.maia.autorizador.procedimento.domain.model.TipoCobertura;
import org.maia.autorizador.procedimento.domain.model.Sexo;

class ProcedimentoTest {

    @Test
    void criaComSexosEmOrdemMFEFaixasDaMaisNovaParaAMaisVelha() {
        Procedimento p = Procedimento.create(10101, List.of(Sexo.F, Sexo.M),
                List.of(FaixaEtaria.IDOSO, FaixaEtaria.CRIANCA));

        assertThat(p.id()).isNull();
        assertThat(p.numProcedimento()).isEqualTo(10101);
        assertThat(p.sexos()).containsExactly(Sexo.M, Sexo.F);
        assertThat(p.faixasEtarias()).containsExactly(FaixaEtaria.CRIANCA, FaixaEtaria.IDOSO);
    }

    @Test
    void aceitaListasVazias() {
        Procedimento p = Procedimento.create(1, List.of(), List.of());

        assertThat(p.sexos()).isEmpty();
        assertThat(p.faixasEtarias()).isEmpty();
        assertThat(p.avaliar(10, Sexo.M)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejeitaNumeroMenorQueUm(int numero) {
        assertThatThrownBy(() -> Procedimento.create(numero, List.of(Sexo.M), List.of(FaixaEtaria.ADULTO)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 151})
    void avaliarRejeitaIdadeForaDe0a150(int idade) {
        Procedimento p = Procedimento.create(1, List.of(Sexo.M), List.of(FaixaEtaria.values()));

        assertThatThrownBy(() -> p.avaliar(idade, Sexo.M)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejeitaDuplicatas() {
        assertThatThrownBy(() -> Procedimento.create(1, List.of(Sexo.M, Sexo.M), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Procedimento.create(1, List.of(), List.of(FaixaEtaria.ADULTO, FaixaEtaria.ADULTO)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejeitaColecoesNulas() {
        assertThatThrownBy(() -> Procedimento.create(1, null, List.of())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Procedimento.create(1, List.of(), null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void avaliaExigindoSexoEFaixaEtaria() {
        Procedimento p = Procedimento.create(1, List.of(Sexo.M), List.of(FaixaEtaria.ADOLESCENTE, FaixaEtaria.IDOSO));

        assertThat(p.avaliar(15, Sexo.M)).isTrue();
        assertThat(p.avaliar(15, Sexo.F)).isFalse();
        assertThat(p.avaliar(30, Sexo.M)).isFalse();
        assertThat(p.avaliar(30, Sexo.F)).isFalse();
        assertThat(p.avaliar(70, Sexo.M)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"0,CRIANCA", "12,CRIANCA", "13,ADOLESCENTE", "20,ADOLESCENTE", "21,ADULTO", "59,ADULTO",
        "60,IDOSO", "150,IDOSO"})
    void faixaEtariaTemLimitesInclusivosEContiguos(int idade, FaixaEtaria esperada) {
        assertThat(FaixaEtaria.de(idade)).isEqualTo(esperada);
        assertThat(esperada.contem(idade)).isTrue();
    }

    @Test
    void cadaIdadePertenceAExatamenteUmaFaixa() {
        for (int idade = 0; idade <= 150; idade++) {
            int i = idade;
            assertThat(java.util.Arrays.stream(FaixaEtaria.values()).filter(f -> f.contem(i))).hasSize(1);
        }
    }

    @Test
    void avaliaSexoSemDiferencaDeCaixa() {
        Procedimento p = Procedimento.create(1, List.of(Sexo.F), List.of(FaixaEtaria.ADULTO));

        assertThat(p.avaliar(40, "f")).isTrue();
        assertThat(p.avaliar(40, "F")).isTrue();
        assertThat(p.avaliar(40, "m")).isFalse();
    }

    @Test
    void avaliarComSexoInvalidoFalha() {
        Procedimento p = Procedimento.create(1, List.of(Sexo.F), List.of(FaixaEtaria.ADULTO));

        assertThatThrownBy(() -> p.avaliar(40, "X")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void restauraSemRevalidarIdentidade() {
        Instant agora = Instant.parse("2026-01-01T00:00:00Z");
        Procedimento p = Procedimento.restore(7L, 99, Set.of(Sexo.F, Sexo.M),
                Set.of(FaixaEtaria.IDOSO, FaixaEtaria.CRIANCA), agora);

        assertThat(p.id()).isEqualTo(7L);
        assertThat(p.sexos()).containsExactly(Sexo.M, Sexo.F);
        assertThat(p.faixasEtarias()).containsExactly(FaixaEtaria.CRIANCA, FaixaEtaria.IDOSO);
        assertThat(p.criadoEm()).isEqualTo(agora);
    }

    @Test
    void colecoesExpostasSaoImutaveis() {
        Procedimento p = Procedimento.create(1, List.of(Sexo.M), List.of(FaixaEtaria.CRIANCA));

        assertThatThrownBy(() -> p.sexos().add(Sexo.F)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> p.faixasEtarias().add(FaixaEtaria.IDOSO))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static DadosCatalogo catalogo(String nome, Integer quantidade, Periodo periodo) {
        return DadosCatalogo.validado(nome, "40101010", Categoria.CONSULTAS, Especialidade.CARDIOLOGIA,
                TipoCobertura.REGRAS, quantidade, periodo, true, false);
    }

    @Test
    void criaComDadosDeCatalogoEAparaONome() {
        Procedimento p = Procedimento.create(1, List.of(Sexo.M), List.of(FaixaEtaria.ADULTO),
                catalogo("  Ressonância  ", 2, Periodo.ANO));

        assertThat(p.catalogo().nome()).isEqualTo("Ressonância");
        assertThat(p.catalogo().categoria()).isEqualTo(Categoria.CONSULTAS);
        assertThat(p.catalogo().especialidade()).isEqualTo(Especialidade.CARDIOLOGIA);
        assertThat(p.catalogo().tipoCobertura()).isEqualTo(TipoCobertura.REGRAS);
        assertThat(p.catalogo().quantidadeMaxima()).isEqualTo(2);
        assertThat(p.catalogo().periodo()).isEqualTo(Periodo.ANO);
        assertThat(p.catalogo().exigeAutorizacaoPrevia()).isTrue();
        assertThat(p.catalogo().exigeJustificativaMedica()).isFalse();
    }

    @Test
    void semCatalogoOsCamposSaoNulos() {
        DadosCatalogo vazio = Procedimento.create(1, List.of(), List.of()).catalogo();

        assertThat(vazio).isEqualTo(DadosCatalogo.VAZIO);
        assertThat(vazio.nome()).isNull();
        assertThat(vazio.quantidadeMaxima()).isNull();
    }

    @Test
    void aceitaNomeComOsLimitesDe1A200Caracteres() {
        assertThat(catalogo("a", null, null).nome()).isEqualTo("a");
        assertThat(catalogo("a".repeat(200), null, null).nome()).hasSize(200);
        assertThat(catalogo(" " + "a".repeat(200) + " ", null, null).nome()).hasSize(200);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejeitaNomeVazioAposTrim(String nome) {
        assertThatThrownBy(() -> catalogo(nome, null, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejeitaNomeComMaisDe200Caracteres() {
        assertThatThrownBy(() -> catalogo("a".repeat(201), null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void quantidadeMaximaEPeriodoAndamJuntos() {
        assertThatThrownBy(() -> catalogo("x", 2, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> catalogo("x", null, Periodo.MES)).isInstanceOf(IllegalArgumentException.class);
        assertThat(catalogo("x", 3, Periodo.VIDA).periodo()).isEqualTo(Periodo.VIDA);
        assertThat(catalogo("x", null, null).periodo()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejeitaQuantidadeMaximaMenorQueUm(int quantidade) {
        assertThatThrownBy(() -> catalogo("x", quantidade, Periodo.MES))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejeitaCodigoTussComMaisDe30Caracteres() {
        assertThatThrownBy(() -> DadosCatalogo.validado("x", "1".repeat(31), null, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void enumsTemOsValoresDoContrato() {
        assertThat(Categoria.values()).extracting(Enum::name).containsExactly("EXAMES_IMAGEM", "CONSULTAS",
                "CIRURGIAS", "TERAPIAS", "EXAMES_LABORATORIAIS");
        assertThat(Especialidade.values()).extracting(Enum::name).containsExactly("CARDIOLOGIA", "ORTOPEDIA",
                "PEDIATRIA", "GINECOLOGIA", "NEUROLOGIA");
        assertThat(TipoCobertura.values()).extracting(Enum::name).containsExactly("PLANO", "REGRAS", "NAO_COBERTO");
        assertThat(Periodo.values()).extracting(Enum::name).containsExactly("MES", "ANO", "VIDA");
    }

    @Test
    void restauraComCatalogo() {
        Procedimento p = Procedimento.restore(7L, 99, Set.of(Sexo.M), Set.of(FaixaEtaria.CRIANCA), Instant.EPOCH,
                catalogo("Eco", 1, Periodo.MES));

        assertThat(p.catalogo().nome()).isEqualTo("Eco");
        assertThat(Procedimento.restore(7L, 99, Set.of(), Set.of(), Instant.EPOCH).catalogo())
                .isEqualTo(DadosCatalogo.VAZIO);
    }

    @Test
    void avaliarIgnoraTodosOsCamposDeCatalogo() {
        Procedimento sem = Procedimento.create(1, List.of(Sexo.F), List.of(FaixaEtaria.ADULTO));
        Procedimento com = Procedimento.create(1, List.of(Sexo.F), List.of(FaixaEtaria.ADULTO),
                DadosCatalogo.validado("Nome", "T", Categoria.CIRURGIAS, Especialidade.NEUROLOGIA,
                        TipoCobertura.NAO_COBERTO, 1, Periodo.MES, true, true));

        for (int idade : new int[] {20, 21, 59, 60}) {
            for (Sexo sexo : Sexo.values()) {
                assertThat(com.avaliar(idade, sexo)).isEqualTo(sem.avaliar(idade, sexo));
            }
        }
        assertThat(com.avaliar(40, Sexo.F)).isTrue();
        assertThat(com.avaliar(40, "m")).isFalse();
        assertThat(com.avaliar(60, Sexo.F)).isFalse();
    }
}
