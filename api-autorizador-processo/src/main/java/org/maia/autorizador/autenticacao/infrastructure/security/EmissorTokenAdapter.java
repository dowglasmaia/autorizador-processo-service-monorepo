package org.maia.autorizador.autenticacao.infrastructure.security;

import org.maia.autorizador.autenticacao.application.port.EmissorToken;
import org.maia.autorizador.autenticacao.domain.model.TokenEmitido;
import org.maia.autorizador.autenticacao.domain.model.Usuario;
import org.maia.autorizador.shared.security.EmissorJwt;
import org.springframework.stereotype.Component;

/** Emite o JWT do usuário usando o {@link EmissorJwt} de {@code shared.security} ({@code sub} = id do usuário). */
@Component
class EmissorTokenAdapter implements EmissorToken {

    private final EmissorJwt emissorJwt;

    EmissorTokenAdapter(EmissorJwt emissorJwt) {
        this.emissorJwt = emissorJwt;
    }

    @Override
    public TokenEmitido emitir(Usuario usuario) {
        EmissorJwt.TokenEmitido token = emissorJwt.emitir(String.valueOf(usuario.id()), usuario.email());
        return new TokenEmitido(token.valor(), token.expiraEmSegundos());
    }
}
