package org.maia.autorizador.autenticacao.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.autenticacao.application.port.UsuarioRepository;
import org.maia.autorizador.autenticacao.domain.model.Usuario;
import org.maia.autorizador.shared.error.BusinessRuleException;

class ObterUsuarioAtualUseCaseTest {

    private static UsuarioRepository repositorioCom(Usuario usuario) {
        return new UsuarioRepository() {
            @Override
            public Optional<Usuario> buscarPorEmail(String email) {
                return Optional.empty();
            }

            @Override
            public Optional<Usuario> buscarPorId(long id) {
                return Optional.ofNullable(usuario).filter(u -> u.id() == id);
            }

            @Override
            public long contar() {
                return usuario == null ? 0 : 1;
            }

            @Override
            public Usuario salvar(Usuario novo) {
                return novo;
            }
        };
    }

    @Test
    void devolveUsuarioAtivo() {
        var useCase = new ObterUsuarioAtualUseCase(repositorioCom(new Usuario(7L, "a@b.com", "h", true)));

        assertThat(useCase.executar(7L)).isEqualTo(new ObterUsuarioAtualUseCase.Resultado(7L, "a@b.com"));
    }

    @Test
    void usuarioInativoOuInexistenteNaoEstaAutenticado() {
        var inativo = new ObterUsuarioAtualUseCase(repositorioCom(new Usuario(7L, "a@b.com", "h", false)));
        var inexistente = new ObterUsuarioAtualUseCase(repositorioCom(null));

        assertThatThrownBy(() -> inativo.executar(7L)).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> inexistente.executar(7L)).isInstanceOf(BusinessRuleException.class);
    }
}
