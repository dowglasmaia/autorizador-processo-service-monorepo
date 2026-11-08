package org.maia.autorizador.autenticacao.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.autenticacao.application.port.EmissorToken;
import org.maia.autorizador.autenticacao.application.port.HashSenha;
import org.maia.autorizador.autenticacao.application.port.LimitadorTentativas;
import org.maia.autorizador.autenticacao.application.port.UsuarioRepository;
import org.maia.autorizador.autenticacao.domain.model.TokenEmitido;
import org.maia.autorizador.autenticacao.domain.model.Usuario;
import org.maia.autorizador.shared.error.BusinessRuleException;
import org.maia.autorizador.shared.error.LimiteExcedidoException;

/** Caso de uso de login sem Spring: portas substituídas por implementações em memória. */
class AutenticarUsuarioUseCaseTest {

    private static final String IP = "10.0.0.1";

    private final Map<String, Usuario> base = new HashMap<>();
    private final List<String> conferencias = new ArrayList<>();
    private final Map<String, Integer> falhas = new HashMap<>();
    private Duration bloqueio;
    private AutenticarUsuarioUseCase useCase;

    @BeforeEach
    void preparar() {
        UsuarioRepository repositorio = new UsuarioRepository() {
            @Override
            public Optional<Usuario> buscarPorEmail(String email) {
                return Optional.ofNullable(base.get(email));
            }

            @Override
            public Optional<Usuario> buscarPorId(long id) {
                return base.values().stream().filter(u -> u.id() == id).findFirst();
            }

            @Override
            public long contar() {
                return base.size();
            }

            @Override
            public Usuario salvar(Usuario usuario) {
                base.put(usuario.email(), usuario);
                return usuario;
            }
        };
        HashSenha hash = new HashSenha() {
            @Override
            public String gerar(String senha) {
                return "hash:" + senha;
            }

            @Override
            public boolean confere(String senha, String hashAlvo) {
                conferencias.add(hashAlvo);
                return hashAlvo.equals("hash:" + senha);
            }
        };
        LimitadorTentativas limitador = new LimitadorTentativas() {
            @Override
            public Optional<Duration> bloqueioRestante(String chave) {
                return Optional.ofNullable(bloqueio);
            }

            @Override
            public void registrarFalha(String chave) {
                falhas.merge(chave, 1, Integer::sum);
            }

            @Override
            public void limpar(String chave) {
                falhas.remove(chave);
            }
        };
        EmissorToken emissor = usuario -> new TokenEmitido("token-" + usuario.id(), 3600);
        useCase = new AutenticarUsuarioUseCase(repositorio, hash, limitador, emissor);
        base.put("ana@ex.com", new Usuario(1L, "ana@ex.com", "hash:segredo123", true));
        base.put("inativo@ex.com", new Usuario(2L, "inativo@ex.com", "hash:segredo123", false));
    }

    private AutenticarUsuarioUseCase.Comando comando(String email, String senha) {
        return new AutenticarUsuarioUseCase.Comando(email, senha, IP);
    }

    @Test
    void credencialValidaEmiteToken() {
        var resultado = useCase.executar(comando("ana@ex.com", "segredo123"));

        assertThat(resultado.accessToken()).isEqualTo("token-1");
        assertThat(resultado.expiraEmSegundos()).isEqualTo(3600);
        assertThat(resultado.email()).isEqualTo("ana@ex.com");
        assertThat(falhas).isEmpty();
    }

    @Test
    void emailEhNormalizadoIgnorandoCaixaEEspacos() {
        var resultado = useCase.executar(comando("  ANA@Ex.COM ", "segredo123"));

        assertThat(resultado.usuarioId()).isEqualTo(1L);
    }

    @Test
    void senhaErradaEmailInexistenteEUsuarioInativoDaoMesmaRespostaEEqualizamTrabalho() {
        var erros = List.of(
                capturar(comando("ana@ex.com", "errada")),
                capturar(comando("naoexiste@ex.com", "qualquer")),
                capturar(comando("inativo@ex.com", "segredo123")));

        assertThat(erros).allSatisfy(e -> {
            assertThat(e.getCode()).isEqualTo("CREDENCIAIS_INVALIDAS");
            assertThat(e.getCategoria()).isEqualTo(BusinessRuleException.Categoria.NAO_AUTENTICADO);
            assertThat(e.getMessage()).isEqualTo(AutenticarUsuarioUseCase.MENSAGEM_CREDENCIAIS);
        });
        // Sempre houve uma conferência de hash (fictício quando o usuário não existe ou está inativo).
        assertThat(conferencias).hasSize(3);
        assertThat(conferencias.get(1)).isEqualTo(conferencias.get(2)).isNotEqualTo("hash:segredo123");
        // Cada par (e-mail, IP) distinto registra a própria falha.
        assertThat(falhas.values()).hasSize(3).containsOnly(1);
    }

    @Test
    void limiteExcedidoBloqueiaSemConferirSenha() {
        bloqueio = Duration.ofSeconds(90);

        assertThatThrownBy(() -> useCase.executar(comando("ana@ex.com", "segredo123")))
                .isInstanceOfSatisfying(LimiteExcedidoException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("MUITAS_TENTATIVAS");
                    assertThat(e.getRetryAfterSegundos()).isEqualTo(90);
                });
        assertThat(conferencias).isEmpty();
    }

    private BusinessRuleException capturar(AutenticarUsuarioUseCase.Comando comando) {
        try {
            useCase.executar(comando);
        } catch (BusinessRuleException e) {
            return e;
        }
        throw new AssertionError("Era esperada falha de credencial");
    }
}
