package org.maia.autorizador.procedimento.application.port;

import java.util.Optional;
import org.maia.autorizador.procedimento.domain.model.Pagina;
import org.maia.autorizador.procedimento.domain.model.Procedimento;

/** Porta de saída: persistência de procedimentos. */
public interface ProcedimentoRepository {

    /**
     * Grava um procedimento novo e devolve-o com identificador e data. A violação da unicidade do número é traduzida
     * pelo adaptador em {@code ProcedimentoDuplicadoException}.
     */
    Procedimento salvar(Procedimento procedimento);

    Optional<Procedimento> buscarPorId(long id);

    Optional<Procedimento> buscarPorNumero(int numProcedimento);

    /**
     * Página ordenada por número. {@code prefixoNumero} (apenas dígitos, ou nulo para sem filtro) filtra
     * {@code numProcedimento} por prefixo.
     */
    Pagina<Procedimento> listar(String prefixoNumero, boolean crescente, int pagina, int tamanho);
}
