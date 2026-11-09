-- Regra de idade por faixa etária (CRIANCA 0-12, ADOLESCENTE 13-20, ADULTO 21-59, IDOSO 60-150) no lugar de
-- idades exatas. As idades já gravadas viram as faixas que as contêm. A tabela de auditoria antiga
-- (procedimento_idade_audit) fica como histórico, sem uso pela aplicação.

create table procedimento_faixa_etaria (
    procedimento_id bigint      not null,
    faixa           varchar(12) not null,
    constraint pk_procedimento_faixa_etaria primary key (procedimento_id, faixa),
    constraint fk_procedimento_faixa_procedimento foreign key (procedimento_id)
        references procedimento (id) on delete cascade,
    constraint ck_procedimento_faixa check (faixa in ('CRIANCA', 'ADOLESCENTE', 'ADULTO', 'IDOSO'))
);

create table procedimento_faixa_etaria_audit (
    rev             integer     not null,
    revtype         smallint,
    procedimento_id bigint      not null,
    faixa           varchar(12) not null,
    constraint pk_procedimento_faixa_etaria_audit primary key (rev, procedimento_id, faixa),
    constraint fk_procedimento_faixa_audit_rev foreign key (rev) references revinfo (rev)
);

insert into procedimento_faixa_etaria (procedimento_id, faixa)
select distinct procedimento_id,
       case when idade <= 12 then 'CRIANCA'
            when idade <= 20 then 'ADOLESCENTE'
            when idade <= 59 then 'ADULTO'
            else 'IDOSO' end
from procedimento_idade;

drop table procedimento_idade;
