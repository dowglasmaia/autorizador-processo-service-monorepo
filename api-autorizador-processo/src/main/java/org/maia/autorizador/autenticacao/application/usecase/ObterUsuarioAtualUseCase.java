package org.maia.autorizador.autenticacao.application.usecase;

import org.maia.autorizador.autenticacao.application.port.UsuarioRepository;
import org.maia.autorizador.autenticacao.domain.model.Usuario;
import org.maia.autorizador.shared.error.BusinessRuleException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta o usuário dono do token; usuário removido ou inativo deixa de estar autenticado. */
@Service
@Transactional(readOnly = true)
public class ObterUsuarioAtualUseCase {

    public record Resultado(long id, String email) {
    }

    private final UsuarioRepository usuarios;

    public ObterUsuarioAtualUseCase(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Resultado executar(long usuarioId) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .filter(Usuario::ativo)
                .orElseThrow(() -> new BusinessRuleException("NAO_AUTENTICADO",
                        BusinessRuleException.Categoria.NAO_AUTENTICADO, "Autenticação necessária."));
        return new Resultado(usuario.id(), usuario.email());
    }
}
