-- Dados de exemplo para o perfil local (carregado só via spring.flyway.locations do application-local.yml).
-- 1234: F, criança e adolescente | 4567: M e F, adolescente e adulto | 6789: M e F, criança.
-- Catálogo estendido (V4): o protótipo não define nome/categoria para 1234, 4567 e 6789; por decisão da
-- SPEC-12 (etapa 9) os campos novos ficam nulos aqui, como nas linhas anteriores ao catálogo.

insert into procedimento (numero_procedimento) values (1234), (4567), (6789);

insert into procedimento_sexo (procedimento_id, sexo)
select p.id, s.sexo
from procedimento p
join (values (1234, 'F'), (4567, 'M'), (4567, 'F'), (6789, 'M'), (6789, 'F')) as s (numero, sexo)
    on s.numero = p.numero_procedimento;

insert into procedimento_faixa_etaria (procedimento_id, faixa)
select p.id, f.faixa
from procedimento p
join (values (1234, 'CRIANCA'), (1234, 'ADOLESCENTE'), (4567, 'ADOLESCENTE'), (4567, 'ADULTO'), (6789, 'CRIANCA'))
    as f (numero, faixa) on f.numero = p.numero_procedimento;
