package org.maia.autorizador.autenticacao.application.port;

/** Porta de saída: geração e conferência de hash de senha. */
public interface HashSenha {

    String gerar(String senha);

    boolean confere(String senha, String hash);
}
