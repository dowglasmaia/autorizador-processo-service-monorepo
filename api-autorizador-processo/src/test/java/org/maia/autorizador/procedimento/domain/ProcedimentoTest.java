package org.maia.autorizador.procedimento.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.maia.autorizador.procedimento.domain.model.Categoria;
import org.maia.autorizador.procedimento.domain.model.DadosCatalogo;
import org.maia.autorizador.procedimento.domain.model.Especialidade;
import org.maia.autorizador.procedimento.domain.model.Periodo;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.maia.autorizador.procedimento.domain.model.TipoCobertura;
import org.maia.autorizador.procedimento.domain.model.Sexo;

class ProcedimentoTest {

    @Test
    void criaComSexosEmOrdemMFEIdadesAscendentes() {
        Procedimento p = Procedimento.create(10101, List.of(Sexo.F, Sexo.M), List.of(30, 18, 0));

        assertThat(p.id()).isNull();
        assertThat(p.numProcedimento()).isEqualTo(10101);
        assertThat(p.sexos()).containsExactly(Sexo.M, Sexo.F);
        assertThat(p.idades()).containsExactly(0, 18, 30);
    }

    @Test
    void aceitaListasVazias() {
        Procedimento p = Procedimento.create(1, List.of(), List.of());

        assertThat(p.sexos()).isEmpty();
        assertThat(p.idades()).isEmpty();
        assertThat(p.avaliar(10, Sexo.M)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejeitaNumeroMenorQueUm(int numero) {
        assertThatThrownBy(() -> Procedimento.create(numero, List.of(Sexo.M), List.of(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 151})
    void rejeitaIdadeForaDaFaixa(int idade) {
        assertThatThrownBy(() -> Procedimento.create(1, List.of(Sexo.M), List.of(idade)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aceitaOsLimitesDaFaixaDeIdade() {
        assertThat(Procedimento.create(1, List.of(), List.of(0, 150)).idades()).containsExactly(0, 150);
    }

    @Test
    void rejeitaDuplicatas() {
        assertThatThrownBy(() -> Procedimento.create(1, List.of(Sexo.M, Sexo.M), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Procedimento.create(1, List.of(), List.of(5, 5)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejeitaColecoesNulas() {
        assertThatThrownBy(() -> Procedimento.create(1, null, List.of())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Procedimento.create(1, List.of(), null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void avaliaExigindoSexoEIdade() {
        Procedimento p = Procedimento.create(1, List.of(Sexo.M), List.of(18, 30));

        assertThat(p.avaliar(18, Sexo.M)).isTrue();
        assertThat(p.avaliar(18, Sexo.F)).isFalse();
        assertThat(p.avaliar(19, Sexo.M)).isFalse();
        assertThat(p.avaliar(19, Sexo.F)).isFalse();
    }

    @Test
    void avaliaSexoSemDiferencaDeCaixa() {
        Procedimento p = Procedimento.create(1, List.of(Sexo.F), List.of(40));

        assertThat(p.avaliar(40, "f")).isTrue();
        assertThat(p.avaliar(40, "F")).isTrue();
        assertThat(p.avaliar(40, "m")).isFalse();
    }

    @Test
    void avaliarComSexoInvalidoFalha() {
        Procedimento p = Procedimento.create(1, List.of(Sexo.F), List.of(40));

        assertThatThrownBy(() -> p.avaliar(40, "X")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void restauraSemRevalidarIdentidade() {
        Instant agora = Instant.parse("2026-01-01T00:00:00Z");
        Procedimento p = Procedimento.restore(7L, 99, Set.of(Sexo.F, Sexo.M), Set.of(3, 1), agora);

        assertThat(p.id()).isEqualTo(7L);
        assertThat(p.sexos()).containsExactly(Sexo.M, Sexo.F);
        assertThat(p.idades()).containsExactly(1, 3);
        assertThat(p.criadoEm()).isEqualTo(agora);
    }

    @Test
    void colecoesExpostasSaoImutaveis() {
        Procedimento p = Procedimento.create(1, List.of(Sexo.M), List.of(1));

        assertThatThrownBy(() -> p.sexos().add(Sexo.F)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> p.idades().add(2)).isInstanceOf(UnsupportedOperationException.class);
    }

    private static DadosCatalogo catalogo(String nome, Integer quantidade, Periodo periodo) {
        return DadosCatalogo.validado(nome, "40101010", Categoria.CONSULTAS, Especialidade.CARDIOLOGIA,
                TipoCobertura.REGRAS, quantidade, periodo, true, false);
    }

    @Test
    void criaComDadosDeCatalogoEAparaONome() {
        Procedimento p = Procedimento.create(1, List.of(Sexo.M), List.of(20),
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
        Procedimento p = Procedimento.restore(7L, 99, Set.of(Sexo.M), Set.of(1), Instant.EPOCH,
                catalogo("Eco", 1, Periodo.MES));

        assertThat(p.catalogo().nome()).isEqualTo("Eco");
        assertThat(Procedimento.restore(7L, 99, Set.of(), Set.of(), Instant.EPOCH).catalogo())
                .isEqualTo(DadosCatalogo.VAZIO);
    }

    @Test
    void avaliarIgnoraTodosOsCamposDeCatalogo() {
        Procedimento sem = Procedimento.create(1, List.of(Sexo.F), List.of(40));
        Procedimento com = Procedimento.create(1, List.of(Sexo.F), List.of(40),
                DadosCatalogo.validado("Nome", "T", Categoria.CIRURGIAS, Especialidade.NEUROLOGIA,
                        TipoCobertura.NAO_COBERTO, 1, Periodo.MES, true, true));

        for (int idade : new int[] {39, 40, 41}) {
            for (Sexo sexo : Sexo.values()) {
                assertThat(com.avaliar(idade, sexo)).isEqualTo(sem.avaliar(idade, sexo));
            }
        }
        assertThat(com.avaliar(40, Sexo.F)).isTrue();
        assertThat(com.avaliar(40, "m")).isFalse();
        assertThat(com.avaliar(41, Sexo.F)).isFalse();
    }
}
