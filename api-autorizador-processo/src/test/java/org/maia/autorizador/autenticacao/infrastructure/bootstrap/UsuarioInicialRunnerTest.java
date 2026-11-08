package org.maia.autorizador.autenticacao.infrastructure.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.maia.autorizador.autenticacao.application.port.HashSenha;
import org.maia.autorizador.autenticacao.application.port.UsuarioRepository;
import org.maia.autorizador.autenticacao.domain.model.Usuario;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;

/** Bootstrap do primeiro usuário contra o PostgreSQL real: só com tabela vazia e variáveis presentes. */
class UsuarioInicialRunnerTest extends IntegrationTestBase {

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    HashSenha hashSenha;

    private UsuarioInicialRunner runner(String email, String senha) {
        return new UsuarioInicialRunner(usuarios, hashSenha, email, senha);
    }

    @Test
    void criaOPrimeiroUsuarioComSenhaEmHash() {
        runner("Admin@Ex.com", "senha-de-teste-1").run(null);

        Usuario criado = usuarios.buscarPorEmail("admin@ex.com").orElseThrow();
        assertThat(criado.ativo()).isTrue();
        assertThat(criado.senhaHash()).isNotEqualTo("senha-de-teste-1");
        assertThat(hashSenha.confere("senha-de-teste-1", criado.senhaHash())).isTrue();
    }

    @Test
    void ehIdempotenteQuandoJaExisteUsuario() {
        runner("admin@ex.com", "senha-de-teste-1").run(null);
        runner("admin@ex.com", "senha-de-teste-1").run(null);
        runner("outro@ex.com", "senha-de-teste-2").run(null);

        assertThat(usuarios.contar()).isEqualTo(1);
        assertThat(usuarios.buscarPorEmail("outro@ex.com")).isEmpty();
    }

    @Test
    void naoCriaNadaSemAsDuasVariaveis() {
        runner("", "senha-de-teste-1").run(null);
        runner("admin@ex.com", "").run(null);

        assertThat(usuarios.contar()).isZero();
    }
}
