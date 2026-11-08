package org.maia.autorizador.autenticacao.application.usecase;

import java.time.Duration;
import java.util.Optional;
import org.maia.autorizador.autenticacao.application.port.EmissorToken;
import org.maia.autorizador.autenticacao.application.port.HashSenha;
import org.maia.autorizador.autenticacao.application.port.LimitadorTentativas;
import org.maia.autorizador.autenticacao.application.port.UsuarioRepository;
import org.maia.autorizador.autenticacao.domain.model.TokenEmitido;
import org.maia.autorizador.autenticacao.domain.model.Usuario;
import org.maia.autorizador.shared.error.BusinessRuleException;
import org.maia.autorizador.shared.error.LimiteExcedidoException;
import org.maia.autorizador.shared.logging.MascaraEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Login por e-mail e senha. Resposta única para qualquer falha de credencial (mensagem idêntica), trabalho
 * equivalente quando o usuário não existe ou está inativo (hash fictício) e limite de falhas por (e-mail, IP).
 */
@Service
@Transactional(readOnly = true)
public class AutenticarUsuarioUseCase {

    public record Comando(String email, String senha, String ip) {
    }

    public record Resultado(String accessToken, long expiraEmSegundos, long usuarioId, String email) {
    }

    private static final Logger log = LoggerFactory.getLogger(AutenticarUsuarioUseCase.class);
    static final String CODE_CREDENCIAIS = "CREDENCIAIS_INVALIDAS";
    static final String MENSAGEM_CREDENCIAIS = "E-mail ou senha inválidos.";

    private final UsuarioRepository usuarios;
    private final HashSenha hashSenha;
    private final LimitadorTentativas limitador;
    private final EmissorToken emissor;
    private final String hashFicticio;

    public AutenticarUsuarioUseCase(UsuarioRepository usuarios, HashSenha hashSenha, LimitadorTentativas limitador,
            EmissorToken emissor) {
        this.usuarios = usuarios;
        this.hashSenha = hashSenha;
        this.limitador = limitador;
        this.emissor = emissor;
        this.hashFicticio = hashSenha.gerar("senha-ficticia-para-equalizar-o-tempo");
    }

    public Resultado executar(Comando comando) {
        String email = Usuario.normalizarEmail(comando.email());
        String chave = email + "|" + comando.ip();

        Optional<Duration> bloqueio = limitador.bloqueioRestante(chave);
        if (bloqueio.isPresent()) {
            long segundos = Math.max(1, bloqueio.get().toSeconds());
            log.warn("Login bloqueado por excesso de tentativas: email={}", MascaraEmail.mascarar(email));
            throw new LimiteExcedidoException("MUITAS_TENTATIVAS",
                    "Muitas tentativas de login. Tente novamente mais tarde.", segundos);
        }

        Optional<Usuario> encontrado = usuarios.buscarPorEmail(email).filter(Usuario::ativo);
        String hashAlvo = encontrado.map(Usuario::senhaHash).orElse(hashFicticio);
        boolean senhaConfere = hashSenha.confere(comando.senha(), hashAlvo);

        if (encontrado.isEmpty() || !senhaConfere) {
            limitador.registrarFalha(chave);
            log.info("Falha de login: email={}", MascaraEmail.mascarar(email));
            throw new BusinessRuleException(CODE_CREDENCIAIS, BusinessRuleException.Categoria.NAO_AUTENTICADO,
                    MENSAGEM_CREDENCIAIS);
        }

        limitador.limpar(chave);
        Usuario usuario = encontrado.get();
        TokenEmitido token = emissor.emitir(usuario);
        log.info("Login efetuado: email={}", MascaraEmail.mascarar(email));
        return new Resultado(token.valor(), token.expiraEmSegundos(), usuario.id(), usuario.email());
    }
}
