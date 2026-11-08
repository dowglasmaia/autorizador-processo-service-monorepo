package org.maia.autorizador.autenticacao.infrastructure.security;

import org.maia.autorizador.autenticacao.application.port.HashSenha;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Hash de senha por BCrypt, via {@link PasswordEncoder} exposto por {@code shared.security}. */
@Component
class HashSenhaAdapter implements HashSenha {

    private final PasswordEncoder encoder;

    HashSenhaAdapter(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String gerar(String senha) {
        return encoder.encode(senha);
    }

    @Override
    public boolean confere(String senha, String hash) {
        return senha != null && encoder.matches(senha, hash);
    }
}
