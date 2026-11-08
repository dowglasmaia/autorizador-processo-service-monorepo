package org.maia.autorizador.procedimento.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ProcedimentoJpaRepository extends JpaRepository<ProcedimentoEntity, Long> {

    Optional<ProcedimentoEntity> findByNumeroProcedimento(int numeroProcedimento);

    /** O prefixo contém apenas dígitos (validado no caso de uso), então não precisa de escape do LIKE. */
    @Query(value = "select p from ProcedimentoEntity p where cast(p.numeroProcedimento as string) like concat(:prefixo, '%')",
            countQuery = "select count(p) from ProcedimentoEntity p where cast(p.numeroProcedimento as string) like concat(:prefixo, '%')")
    Page<ProcedimentoEntity> buscarPorPrefixo(@Param("prefixo") String prefixo, Pageable pageable);
}
