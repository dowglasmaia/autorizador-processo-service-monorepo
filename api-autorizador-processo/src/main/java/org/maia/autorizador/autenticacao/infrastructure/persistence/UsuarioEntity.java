package org.maia.autorizador.autenticacao.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Mapeamento JPA da tabela {@code usuario} (V3). */
@Entity
@Table(name = "usuario")
class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected UsuarioEntity() {
    }

    UsuarioEntity(Long id, String email, String senhaHash, boolean ativo) {
        this.id = id;
        this.email = email;
        this.senhaHash = senhaHash;
        this.ativo = ativo;
        this.criadoEm = Instant.now();
    }

    Long getId() {
        return id;
    }

    String getEmail() {
        return email;
    }

    String getSenhaHash() {
        return senhaHash;
    }

    boolean isAtivo() {
        return ativo;
    }
}
