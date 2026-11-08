-- Usuários da autenticação (SPEC-22). Senha somente como hash BCrypt; e-mail sempre em minúsculas.
create table usuario (
    id         bigint generated always as identity primary key,
    email      varchar(254) not null,
    senha_hash varchar(100) not null,
    ativo      boolean      not null default true,
    criado_em  timestamptz  not null default now(),
    constraint ck_usuario_email_minusculo check (email = lower(email))
);

create unique index ux_usuario_email_lower on usuario (lower(email));
