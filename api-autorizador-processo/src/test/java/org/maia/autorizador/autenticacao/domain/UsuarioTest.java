package org.maia.autorizador.autenticacao.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.maia.autorizador.autenticacao.domain.model.Usuario;

class UsuarioTest {

    @Test
    void normalizaOEmail() {
        assertThat(new Usuario(null, "  Maria@Ex.COM ", "h", true).email()).isEqualTo("maria@ex.com");
        assertThat(Usuario.novo("A@B.com", "h").ativo()).isTrue();
    }

    @Test
    void exigeEmailEHash() {
        assertThatThrownBy(() -> new Usuario(null, " ", "h", true)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(null, "a@b.com", "", true)).isInstanceOf(IllegalArgumentException.class);
    }
}
