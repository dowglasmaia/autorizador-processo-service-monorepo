package org.maia.autorizador.procedimento.application.usecase;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.maia.autorizador.procedimento.application.port.ProcedimentoRepository;
import org.maia.autorizador.procedimento.domain.exception.ProcedimentoDuplicadoException;
import org.maia.autorizador.procedimento.domain.model.Pagina;
import org.maia.autorizador.procedimento.domain.model.Procedimento;

/** Repositório em memória para os testes dos casos de uso; reproduz o UNIQUE do número ao salvar. */
class ProcedimentoRepositoryFake implements ProcedimentoRepository {

    private final Map<Long, Procedimento> porId = new TreeMap<>();
    private long proximoId = 1;
    /** Quando verdadeiro, a busca por número não enxerga o existente (simula a corrida: o UNIQUE é que barra). */
    boolean ocultarNaBusca;

    @Override
    public Procedimento salvar(Procedimento p) {
        boolean existe = porId.values().stream().anyMatch(o -> o.numProcedimento() == p.numProcedimento());
        if (existe) {
            throw new ProcedimentoDuplicadoException(p.numProcedimento());
        }
        long id = proximoId++;
        Procedimento salvo = Procedimento.restore(id, p.numProcedimento(), p.sexos(), p.idades(), Instant.now(), p.catalogo());
        porId.put(id, salvo);
        return salvo;
    }

    @Override
    public Optional<Procedimento> buscarPorId(long id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public Optional<Procedimento> buscarPorNumero(int numProcedimento) {
        if (ocultarNaBusca) {
            return Optional.empty();
        }
        return porId.values().stream().filter(p -> p.numProcedimento() == numProcedimento).findFirst();
    }

    @Override
    public Pagina<Procedimento> listar(String prefixoNumero, boolean crescente, int pagina, int tamanho) {
        Comparator<Procedimento> ordem = Comparator.comparingInt(Procedimento::numProcedimento);
        List<Procedimento> filtrados = porId.values().stream()
                .filter(p -> prefixoNumero == null || String.valueOf(p.numProcedimento()).startsWith(prefixoNumero))
                .sorted(crescente ? ordem : ordem.reversed())
                .toList();
        int de = Math.min(pagina * tamanho, filtrados.size());
        int ate = Math.min(de + tamanho, filtrados.size());
        return new Pagina<>(filtrados.subList(de, ate), pagina, tamanho, filtrados.size());
    }
}
