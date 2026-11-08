package org.maia.autorizador.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Hash de senhas com BCrypt (força 12; o mínimo exigido é 10). */
@Configuration
public class PasswordEncoderConfig {

    static final int FORCA = 12;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(FORCA);
    }
}
