package org.maia.autorizador.procedimento.application.usecase;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.maia.autorizador.procedimento.application.port.ProcedimentoRepository;
import org.maia.autorizador.procedimento.domain.exception.ProcedimentoNaoEncontradoException;
import org.maia.autorizador.procedimento.domain.model.DadosCatalogo;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Obtém um procedimento por identificador ou por número (usado também pela autorização). */
@Service
@Transactional(readOnly = true)
public class ObterProcedimentoUseCase {

    /**
     * Dados de catálogo para as bordas, com os enums como texto (o nome do valor). Todos os campos podem ser nulos
     * em procedimentos anteriores ao catálogo estendido.
     */
    public record Catalogo(String nome, String codigoTuss, String categoria, String especialidade,
            String tipoCobertura, Integer quantidadeMaxima, String periodo, Boolean exigeAutorizacaoPrevia,
            Boolean exigeJustificativaMedica) {

        public static final Catalogo VAZIO = new Catalogo(null, null, null, null, null, null, null, null, null);

        static Catalogo de(DadosCatalogo d) {
            return new Catalogo(d.nome(), d.codigoTuss(), texto(d.categoria()), texto(d.especialidade()),
                    texto(d.tipoCobertura()), d.quantidadeMaxima(), texto(d.periodo()),
                    d.exigeAutorizacaoPrevia(), d.exigeJustificativaMedica());
        }

        private static String texto(Enum<?> valor) {
            return valor == null ? null : valor.name();
        }
    }

    /** Visão do procedimento para as bordas: sexos na ordem M, F e idades em ordem crescente. */
    public record Resultado(long id, int numProcedimento, List<String> sexos, List<Integer> idades,
            Instant criadoEm, Catalogo catalogo) {

        /** Sem dados de catálogo (procedimento anterior ao catálogo estendido). */
        public Resultado(long id, int numProcedimento, List<String> sexos, List<Integer> idades, Instant criadoEm) {
            this(id, numProcedimento, sexos, idades, criadoEm, Catalogo.VAZIO);
        }

        public static Resultado de(Procedimento p) {
            return new Resultado(p.id(), p.numProcedimento(), p.sexos().stream().map(Enum::name).toList(),
                    List.copyOf(p.idades()), p.criadoEm(), Catalogo.de(p.catalogo()));
        }
    }

    private final ProcedimentoRepository procedimentos;

    public ObterProcedimentoUseCase(ProcedimentoRepository procedimentos) {
        this.procedimentos = procedimentos;
    }

    public Resultado executar(long id) {
        return procedimentos.buscarPorId(id).map(Resultado::de)
                .orElseThrow(() -> ProcedimentoNaoEncontradoException.porId(id));
    }

    /** Carrega vários procedimentos de uma vez (sem N+1), indexados por id; ids inexistentes ficam de fora. */
    public Map<Long, Resultado> porIds(Collection<Long> ids) {
        return procedimentos.buscarPorIds(ids).stream().map(Resultado::de)
                .collect(Collectors.toMap(Resultado::id, Function.identity()));
    }

    public Procedimento porNumero(int numProcedimento) {
        return procedimentos.buscarPorNumero(numProcedimento)
                .orElseThrow(() -> ProcedimentoNaoEncontradoException.porNumero(numProcedimento));
    }
}
