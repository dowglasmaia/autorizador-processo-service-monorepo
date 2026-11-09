package org.maia.autorizador.procedimento.application.usecase;

import java.util.Collection;
import org.maia.autorizador.procedimento.application.port.ProcedimentoRepository;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Catalogo;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Resultado;
import org.maia.autorizador.procedimento.domain.exception.ProcedimentoDuplicadoException;
import org.maia.autorizador.procedimento.domain.model.Categoria;
import org.maia.autorizador.procedimento.domain.model.DadosCatalogo;
import org.maia.autorizador.procedimento.domain.model.Especialidade;
import org.maia.autorizador.procedimento.domain.model.FaixaEtaria;
import org.maia.autorizador.procedimento.domain.model.Periodo;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.maia.autorizador.procedimento.domain.model.Sexo;
import org.maia.autorizador.procedimento.domain.model.TipoCobertura;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastra um procedimento. A checagem prévia dá a resposta rápida; a garantia contra a corrida é o UNIQUE do banco,
 * cuja violação o adaptador da porta traduz em {@link ProcedimentoDuplicadoException}.
 */
@Service
@Transactional
public class CadastrarProcedimentoUseCase {

    private final ProcedimentoRepository procedimentos;

    public CadastrarProcedimentoUseCase(ProcedimentoRepository procedimentos) {
        this.procedimentos = procedimentos;
    }

    /** Cadastro sem dados de catálogo; os padrões de catálogo são aplicados mesmo assim. */
    public Resultado executar(int numProcedimento, Collection<String> sexos,
            Collection<String> faixasEtarias) {
        return executar(numProcedimento, sexos, faixasEtarias, Catalogo.VAZIO);
    }

    /**
     * Cadastra com os dados de catálogo. Omitidos, o tipo de cobertura vira {@code PLANO} e os indicadores
     * {@code exigeAutorizacaoPrevia} e {@code exigeJustificativaMedica} viram {@code false}.
     *
     * @param sexos {@code M} ou {@code F}, sem diferença de caixa; valor inválido lança {@link IllegalArgumentException}; idem para {@code faixasEtarias}
     */
    public Resultado executar(int numProcedimento, Collection<String> sexos,
            Collection<String> faixasEtarias,
            Catalogo catalogo) {
        Procedimento novo = Procedimento.create(numProcedimento, sexos.stream().map(Sexo::parse).toList(),
                faixasEtarias.stream().map(FaixaEtaria::valueOf).toList(),
                paraDominio(catalogo));
        if (procedimentos.buscarPorNumero(numProcedimento).isPresent()) {
            throw new ProcedimentoDuplicadoException(numProcedimento);
        }
        return Resultado.de(procedimentos.salvar(novo));
    }

    private static DadosCatalogo paraDominio(Catalogo c) {
        return DadosCatalogo.validado(c.nome(), c.codigoTuss(), valor(Categoria.class, c.categoria()),
                valor(Especialidade.class, c.especialidade()),
                c.tipoCobertura() == null ? TipoCobertura.PLANO : valor(TipoCobertura.class, c.tipoCobertura()),
                c.quantidadeMaxima(), valor(Periodo.class, c.periodo()),
                Boolean.TRUE.equals(c.exigeAutorizacaoPrevia()), Boolean.TRUE.equals(c.exigeJustificativaMedica()));
    }

    private static <E extends Enum<E>> E valor(Class<E> tipo, String nome) {
        return nome == null ? null : Enum.valueOf(tipo, nome);
    }
}
