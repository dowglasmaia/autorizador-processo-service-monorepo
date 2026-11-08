package org.maia.autorizador.shared.logging;

/** Mascara e-mails para logs: {@code maria@ex.com} vira {@code m***@ex.com}. */
public final class MascaraEmail {

    private MascaraEmail() {
    }

    public static String mascarar(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }
        int arroba = email.indexOf('@');
        if (arroba <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(arroba);
    }
}
