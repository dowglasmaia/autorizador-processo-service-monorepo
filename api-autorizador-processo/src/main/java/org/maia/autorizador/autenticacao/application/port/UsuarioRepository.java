package org.maia.autorizador.autenticacao.application.port;

import java.util.Optional;
import org.maia.autorizador.autenticacao.domain.model.Usuario;

/** Porta de saída: persistência de usuários. */
public interface UsuarioRepository {

    /** Busca pelo e-mail já normalizado. */
    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorId(long id);

    long contar();

    Usuario salvar(Usuario usuario);
}
