-- Catálogo estendido (NEW-01, SPEC-12): dados informativos do procedimento.
-- Todas as colunas aceitam nulo no banco: linhas anteriores não têm esses dados.
-- A obrigatoriedade de nome e categoria é regra da API para novos cadastros.

alter table procedimento
    add column nome                       varchar(200),
    add column codigo_tuss                varchar(30),
    add column categoria                  varchar(30),
    add column especialidade              varchar(30),
    add column tipo_cobertura             varchar(20),
    add column quantidade_maxima          integer,
    add column periodo                    varchar(10),
    add column exige_autorizacao_previa   boolean,
    add column exige_justificativa_medica boolean;

alter table procedimento
    add constraint ck_procedimento_categoria check (categoria in
        ('EXAMES_IMAGEM', 'CONSULTAS', 'CIRURGIAS', 'TERAPIAS', 'EXAMES_LABORATORIAIS')),
    add constraint ck_procedimento_especialidade check (especialidade in
        ('CARDIOLOGIA', 'ORTOPEDIA', 'PEDIATRIA', 'GINECOLOGIA', 'NEUROLOGIA')),
    add constraint ck_procedimento_tipo_cobertura check (tipo_cobertura in ('PLANO', 'REGRAS', 'NAO_COBERTO')),
    add constraint ck_procedimento_periodo check (periodo in ('MES', 'ANO', 'VIDA')),
    add constraint ck_procedimento_quantidade_maxima check (quantidade_maxima >= 1),
    add constraint ck_procedimento_quantidade_periodo check (quantidade_maxima is null or periodo is not null);

-- Espelho na auditoria (Envers): somente procedimento_audit, sem constraints.
alter table procedimento_audit
    add column nome                       varchar(200),
    add column codigo_tuss                varchar(30),
    add column categoria                  varchar(30),
    add column especialidade              varchar(30),
    add column tipo_cobertura             varchar(20),
    add column quantidade_maxima          integer,
    add column periodo                    varchar(10),
    add column exige_autorizacao_previa   boolean,
    add column exige_justificativa_medica boolean;
