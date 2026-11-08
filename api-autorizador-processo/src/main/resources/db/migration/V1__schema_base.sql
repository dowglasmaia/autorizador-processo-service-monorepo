-- Esquema base do Autorizador (DATABASE_DESIGN.md, V1).

create table procedimento (
    id                   bigint generated always as identity primary key,
    numero_procedimento  integer     not null,
    criado_em            timestamptz not null default now(),
    constraint uq_procedimento_numero unique (numero_procedimento),
    constraint ck_procedimento_numero check (numero_procedimento > 0)
);

create table procedimento_sexo (
    procedimento_id bigint  not null,
    sexo            char(1) not null,
    constraint pk_procedimento_sexo primary key (procedimento_id, sexo),
    constraint fk_procedimento_sexo_procedimento foreign key (procedimento_id)
        references procedimento (id) on delete cascade,
    constraint ck_procedimento_sexo check (sexo in ('M', 'F'))
);

create table procedimento_idade (
    procedimento_id bigint   not null,
    idade           smallint not null,
    constraint pk_procedimento_idade primary key (procedimento_id, idade),
    constraint fk_procedimento_idade_procedimento foreign key (procedimento_id)
        references procedimento (id) on delete cascade,
    constraint ck_procedimento_idade check (idade between 0 and 150)
);

-- criada_em é nullable para aceitar linhas importadas sem data. Sem UNIQUE em procedimento_id (RN11).
create table autorizacao (
    id              bigint generated always as identity primary key,
    procedimento_id bigint      not null,
    permitido       boolean     not null,
    criada_em       timestamptz default now(),
    constraint fk_autorizacao_procedimento foreign key (procedimento_id)
        references procedimento (id)
);

create index ix_autorizacao_procedimento on autorizacao (procedimento_id);
