-- Auditoria (RN10): tabelas no formato do Hibernate Envers, com sufixo _audit (DATABASE_DESIGN.md, V2).

-- O Envers do Hibernate 7 gera o número da revisão por sequência (revinfo_seq, allocationSize 50), não por identity.
create sequence revinfo_seq start with 1 increment by 50;

create table revinfo (
    rev      integer primary key,
    revtstmp bigint
);

create table procedimento_audit (
    rev                 integer  not null,
    revtype             smallint,
    id                  bigint   not null,
    numero_procedimento integer,
    criado_em           timestamptz,
    constraint pk_procedimento_audit primary key (rev, id),
    constraint fk_procedimento_audit_rev foreign key (rev) references revinfo (rev)
);

create table autorizacao_audit (
    rev             integer  not null,
    revtype         smallint,
    id              bigint   not null,
    procedimento_id bigint,
    permitido       boolean,
    criada_em       timestamptz,
    constraint pk_autorizacao_audit primary key (rev, id),
    constraint fk_autorizacao_audit_rev foreign key (rev) references revinfo (rev)
);

create table procedimento_sexo_audit (
    rev             integer  not null,
    revtype         smallint,
    procedimento_id bigint   not null,
    sexo            char(1)  not null,
    constraint pk_procedimento_sexo_audit primary key (rev, procedimento_id, sexo),
    constraint fk_procedimento_sexo_audit_rev foreign key (rev) references revinfo (rev)
);

create table procedimento_idade_audit (
    rev             integer  not null,
    revtype         smallint,
    procedimento_id bigint   not null,
    idade           smallint not null,
    constraint pk_procedimento_idade_audit primary key (rev, procedimento_id, idade),
    constraint fk_procedimento_idade_audit_rev foreign key (rev) references revinfo (rev)
);
