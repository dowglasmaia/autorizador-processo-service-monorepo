package org.maia.autorizador.autorizacao.infrastructure.persistence;

import org.maia.autorizador.autorizacao.application.port.AutorizacaoRepository;
import org.maia.autorizador.autorizacao.domain.model.Autorizacao;
import org.maia.autorizador.autorizacao.domain.model.Pagina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA da porta {@link AutorizacaoRepository}; o mapeamento entidade↔domínio é manual. */
@Component
@Transactional
class AutorizacaoRepositoryAdapter implements AutorizacaoRepository {

    private final AutorizacaoJpaRepository jpa;

    AutorizacaoRepositoryAdapter(AutorizacaoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Autorizacao salvar(Autorizacao autorizacao) {
        var entidade = new AutorizacaoEntity(autorizacao.procedimentoId(), autorizacao.permitido());
        return paraDominio(jpa.saveAndFlush(entidade));
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<Autorizacao> listar(int pagina, int tamanho) {
        Sort ordem = Sort.by(Sort.Order.desc("criadaEm").nullsLast(), Sort.Order.desc("id"));
        Page<AutorizacaoEntity> resultado = jpa.findAll(PageRequest.of(pagina, tamanho, ordem));
        return new Pagina<>(resultado.getContent().stream().map(AutorizacaoRepositoryAdapter::paraDominio).toList(),
                pagina, tamanho, resultado.getTotalElements());
    }

    private static Autorizacao paraDominio(AutorizacaoEntity e) {
        return Autorizacao.restore(e.getId(), e.getProcedimentoId(), e.isPermitido(), e.getCriadaEm());
    }
}
