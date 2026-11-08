package org.maia.autorizador.procedimento.infrastructure.persistence;

import java.util.Optional;
import org.maia.autorizador.procedimento.application.port.ProcedimentoRepository;
import org.maia.autorizador.procedimento.domain.exception.ProcedimentoDuplicadoException;
import org.maia.autorizador.procedimento.domain.model.Pagina;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador JPA da porta {@link ProcedimentoRepository}. O mapeamento entidade↔domínio é manual e acontece dentro
 * da transação, porque as coleções são preguiçosas. A violação do UNIQUE do número vira
 * {@link ProcedimentoDuplicadoException}.
 */
@Component
@Transactional
class ProcedimentoRepositoryAdapter implements ProcedimentoRepository {

    private static final String RESTRICAO_NUMERO_UNICO = "uq_procedimento_numero";

    private final ProcedimentoJpaRepository jpa;

    ProcedimentoRepositoryAdapter(ProcedimentoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Procedimento salvar(Procedimento procedimento) {
        var entidade = new ProcedimentoEntity(procedimento.numProcedimento(), procedimento.sexos(),
                procedimento.idades(), procedimento.catalogo());
        try {
            return paraDominio(jpa.saveAndFlush(entidade));
        } catch (DataIntegrityViolationException ex) {
            Throwable causa = ex.getMostSpecificCause();
            String mensagem = causa.getMessage() == null ? "" : causa.getMessage();
            if (mensagem.contains(RESTRICAO_NUMERO_UNICO)) {
                throw new ProcedimentoDuplicadoException(procedimento.numProcedimento());
            }
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Procedimento> buscarPorId(long id) {
        return jpa.findById(id).map(ProcedimentoRepositoryAdapter::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Procedimento> buscarPorNumero(int numProcedimento) {
        return jpa.findByNumeroProcedimento(numProcedimento).map(ProcedimentoRepositoryAdapter::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<Procedimento> listar(String prefixoNumero, boolean crescente, int pagina, int tamanho) {
        Sort ordem = Sort.by(crescente ? Sort.Direction.ASC : Sort.Direction.DESC, "numeroProcedimento");
        var pageable = PageRequest.of(pagina, tamanho, ordem);
        Page<ProcedimentoEntity> resultado = prefixoNumero == null
                ? jpa.findAll(pageable)
                : jpa.buscarPorPrefixo(prefixoNumero, pageable);
        return new Pagina<>(resultado.getContent().stream().map(ProcedimentoRepositoryAdapter::paraDominio).toList(),
                pagina, tamanho, resultado.getTotalElements());
    }

    private static Procedimento paraDominio(ProcedimentoEntity e) {
        return Procedimento.restore(e.getId(), e.getNumeroProcedimento(), e.getSexos(), e.getIdades(),
                e.getCriadoEm(), e.catalogo());
    }
}
