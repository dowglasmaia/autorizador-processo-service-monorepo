package org.maia.autorizador.autenticacao.infrastructure.bootstrap;

import org.maia.autorizador.autenticacao.application.port.HashSenha;
import org.maia.autorizador.autenticacao.application.port.UsuarioRepository;
import org.maia.autorizador.autenticacao.domain.model.Usuario;
import org.maia.autorizador.shared.logging.MascaraEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro usuário a partir de {@code AUTH_BOOTSTRAP_EMAIL}/{@code AUTH_BOOTSTRAP_SENHA}, somente com a
 * tabela vazia e as duas variáveis presentes. Idempotente e nunca registra a senha.
 */
@Component
class UsuarioInicialRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuarioInicialRunner.class);

    private final UsuarioRepository usuarios;
    private final HashSenha hashSenha;
    private final String email;
    private final String senha;

    UsuarioInicialRunner(UsuarioRepository usuarios, HashSenha hashSenha,
            @Value("${app.auth.bootstrap.email:}") String email,
            @Value("${app.auth.bootstrap.senha:}") String senha) {
        this.usuarios = usuarios;
        this.hashSenha = hashSenha;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || senha.isBlank() || usuarios.contar() > 0) {
            return;
        }
        Usuario criado = usuarios.salvar(Usuario.novo(email, hashSenha.gerar(senha)));
        log.info("Usuário inicial criado: email={}", MascaraEmail.mascarar(criado.email()));
    }
}
