package org.maia.autorizador.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.procedimento.application.port.ProcedimentoRepository;
import org.maia.autorizador.procedimento.domain.model.Categoria;
import org.maia.autorizador.procedimento.domain.model.DadosCatalogo;
import org.maia.autorizador.procedimento.domain.model.Especialidade;
import org.maia.autorizador.procedimento.domain.model.Periodo;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.maia.autorizador.procedimento.domain.model.FaixaEtaria;
import org.maia.autorizador.procedimento.domain.model.Sexo;
import org.maia.autorizador.procedimento.domain.model.TipoCobertura;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;

/** Auditoria Envers (RN10) dos campos do catálogo estendido (NEW-01) em {@code procedimento_audit}. */
class AuditoriaCatalogoTest extends IntegrationTestBase {

    @Autowired
    ProcedimentoRepository procedimentos;

    @Test
    void insertRegistraOsNovosCamposNaAuditoria() {
        DadosCatalogo catalogo = DadosCatalogo.validado("Ressonância magnética", "40101010", Categoria.EXAMES_IMAGEM,
                Especialidade.NEUROLOGIA, TipoCobertura.REGRAS, 2, Periodo.ANO, true, false);
        Procedimento salvo = procedimentos.salvar(Procedimento.create(4322, List.of(Sexo.M), List.of(FaixaEtaria.ADULTO), catalogo));

        Map<String, Object> linha = jdbc.queryForMap("""
                select nome, codigo_tuss, categoria, especialidade, tipo_cobertura, quantidade_maxima, periodo,
                       exige_autorizacao_previa, exige_justificativa_medica
                from procedimento_audit where id = ? and revtype = 0
                """, salvo.id());

        assertThat(linha).containsEntry("nome", "Ressonância magnética")
                .containsEntry("codigo_tuss", "40101010")
                .containsEntry("categoria", "EXAMES_IMAGEM")
                .containsEntry("especialidade", "NEUROLOGIA")
                .containsEntry("tipo_cobertura", "REGRAS")
                .containsEntry("quantidade_maxima", 2)
                .containsEntry("periodo", "ANO")
                .containsEntry("exige_autorizacao_previa", true)
                .containsEntry("exige_justificativa_medica", false);
    }

    @Test
    void procedimentoSemCatalogoGravaAuditoriaComNovosCamposNulos() {
        Procedimento salvo = procedimentos.salvar(Procedimento.create(4323, List.of(Sexo.F), List.of(FaixaEtaria.CRIANCA)));

        assertThat(jdbc.queryForObject("""
                select count(*) from procedimento_audit
                where id = ? and revtype = 0 and nome is null and categoria is null and tipo_cobertura is null
                  and quantidade_maxima is null and periodo is null
                """, Integer.class, salvo.id())).isEqualTo(1);
    }
}
