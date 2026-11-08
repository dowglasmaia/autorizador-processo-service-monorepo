package org.maia.autorizador.autenticacao.api;

import jakarta.servlet.http.HttpServletRequest;
import org.maia.autorizador.autenticacao.application.usecase.AutenticarUsuarioUseCase;
import org.maia.autorizador.autenticacao.application.usecase.ObterUsuarioAtualUseCase;
import org.maia.autorizador.generated.auth.api.AuthApi;
import org.maia.autorizador.generated.auth.model.LoginRequest;
import org.maia.autorizador.generated.auth.model.TokenResponse;
import org.maia.autorizador.generated.auth.model.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador HTTP da autenticação sobre a interface gerada. Não registra senha nem token; o e-mail só aparece
 * mascarado nos logs dos casos de uso.
 */
@RestController
@RequestMapping("/api/v1")
class AuthController implements AuthApi {

    private final AutenticarUsuarioUseCase autenticar;
    private final ObterUsuarioAtualUseCase obterUsuarioAtual;
    private final HttpServletRequest request;

    AuthController(AutenticarUsuarioUseCase autenticar, ObterUsuarioAtualUseCase obterUsuarioAtual,
            HttpServletRequest request) {
        this.autenticar = autenticar;
        this.obterUsuarioAtual = obterUsuarioAtual;
        this.request = request;
    }

    @Override
    public ResponseEntity<TokenResponse> autenticar(LoginRequest loginRequest) {
        var resultado = autenticar.executar(new AutenticarUsuarioUseCase.Comando(
                loginRequest.getEmail(), loginRequest.getSenha(), request.getRemoteAddr()));
        var usuario = new Usuario(resultado.usuarioId(), resultado.email());
        var corpo = new TokenResponse(resultado.accessToken(), TokenResponse.TokenTypeEnum.BEARER,
                resultado.expiraEmSegundos(), usuario);
        return ResponseEntity.ok(corpo);
    }

    @Override
    public ResponseEntity<Usuario> obterUsuarioAtual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        var resultado = obterUsuarioAtual.executar(Long.parseLong(autenticacao.getName()));
        return ResponseEntity.ok(new Usuario(resultado.id(), resultado.email()));
    }
}
