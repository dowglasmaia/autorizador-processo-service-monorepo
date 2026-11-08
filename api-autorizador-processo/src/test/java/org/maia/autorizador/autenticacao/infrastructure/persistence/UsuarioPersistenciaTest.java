package org.maia.autorizador.autenticacao.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.maia.autorizador.autenticacao.application.port.UsuarioRepository;
import org.maia.autorizador.autenticacao.domain.model.Usuario;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

/** Persistência de usuários em PostgreSQL 16 real (Testcontainers). */
class UsuarioPersistenciaTest extends IntegrationTestBase {

    @Autowired
    UsuarioRepository usuarios;

    @Test
    void salvaEBuscaPorEmailNormalizadoEPorId() {
        Usuario salvo = usuarios.salvar(Usuario.novo("Maria@Ex.com", "hash"));

        assertThat(salvo.id()).isNotNull();
        assertThat(usuarios.buscarPorEmail("  MARIA@ex.COM ")).get().extracting(Usuario::id).isEqualTo(salvo.id());
        assertThat(usuarios.buscarPorId(salvo.id())).get().extracting(Usuario::email).isEqualTo("maria@ex.com");
        assertThat(usuarios.contar()).isEqualTo(1);
    }

    @Test
    void emailDuplicadoSemDiferenciarCaixaEhRejeitado() {
        usuarios.salvar(Usuario.novo("dup@ex.com", "hash"));

        assertThatThrownBy(() -> usuarios.salvar(Usuario.novo("DUP@ex.com", "outro")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void bancoRejeitaEmailComMaiusculasMesmoForaDaAplicacao() {
        assertThatThrownBy(() -> jdbc.update("insert into usuario (email, senha_hash) values ('Abc@ex.com', 'h')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
