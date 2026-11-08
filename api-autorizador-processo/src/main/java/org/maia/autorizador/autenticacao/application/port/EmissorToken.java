package org.maia.autorizador.autenticacao.application.port;

import org.maia.autorizador.autenticacao.domain.model.TokenEmitido;
import org.maia.autorizador.autenticacao.domain.model.Usuario;

/** Porta de saída: emissão do token de acesso do usuário. */
public interface EmissorToken {

    TokenEmitido emitir(Usuario usuario);
}
