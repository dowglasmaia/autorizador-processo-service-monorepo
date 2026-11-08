package org.maia.autorizador.autenticacao.infrastructure.persistence;

import java.util.Optional;
import org.maia.autorizador.autenticacao.application.port.UsuarioRepository;
import org.maia.autorizador.autenticacao.domain.model.Usuario;
import org.springframework.stereotype.Component;

/** Adaptador JPA da porta {@link UsuarioRepository}; o e-mail é sempre consultado já normalizado. */
@Component
class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpa;

    UsuarioRepositoryAdapter(UsuarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpa.findByEmail(Usuario.normalizarEmail(email)).map(UsuarioRepositoryAdapter::paraDominio);
    }

    @Override
    public Optional<Usuario> buscarPorId(long id) {
        return jpa.findById(id).map(UsuarioRepositoryAdapter::paraDominio);
    }

    @Override
    public long contar() {
        return jpa.count();
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioEntity salvo = jpa.save(
                new UsuarioEntity(usuario.id(), usuario.email(), usuario.senhaHash(), usuario.ativo()));
        return paraDominio(salvo);
    }

    private static Usuario paraDominio(UsuarioEntity e) {
        return new Usuario(e.getId(), e.getEmail(), e.getSenhaHash(), e.isAtivo());
    }
}
