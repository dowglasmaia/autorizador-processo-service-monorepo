package org.maia.autorizador.autenticacao.domain.model;

import java.util.Locale;

/**
 * Usuário autenticável. O e-mail é sempre normalizado (sem espaços nas pontas e em minúsculas) e a senha existe
 * somente como hash.
 */
public record Usuario(Long id, String email, String senhaHash, boolean ativo) {

    public Usuario {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail obrigatório.");
        }
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException("Hash da senha obrigatório.");
        }
        email = normalizarEmail(email);
    }

    /** Novo usuário ativo, ainda sem identificador. */
    public static Usuario novo(String email, String senhaHash) {
        return new Usuario(null, email, senhaHash, true);
    }

    /** Normaliza o e-mail: remove espaços das pontas e converte para minúsculas. */
    public static String normalizarEmail(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }
}
