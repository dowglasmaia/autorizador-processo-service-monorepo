package org.maia.autorizador.autorizacao.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface AutorizacaoJpaRepository extends JpaRepository<AutorizacaoEntity, Long> {
}
