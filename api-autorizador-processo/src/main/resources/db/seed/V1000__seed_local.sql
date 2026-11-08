-- Dados de exemplo para o perfil local (carregado só via spring.flyway.locations do application-local.yml).
-- 1234: F, idades 10 e 20 | 4567: M e F, idades 20 e 30 | 6789: M e F, idade 10.
-- Catálogo estendido (V4): o protótipo não define nome/categoria para 1234, 4567 e 6789; por decisão da
-- SPEC-12 (etapa 9) os campos novos ficam nulos aqui, como nas linhas anteriores ao catálogo.

insert into procedimento (numero_procedimento) values (1234), (4567), (6789);

insert into procedimento_sexo (procedimento_id, sexo)
select p.id, s.sexo
from procedimento p
join (values (1234, 'F'), (4567, 'M'), (4567, 'F'), (6789, 'M'), (6789, 'F')) as s (numero, sexo)
    on s.numero = p.numero_procedimento;

insert into procedimento_idade (procedimento_id, idade)
select p.id, i.idade
from procedimento p
join (values (1234, 10), (1234, 20), (4567, 20), (4567, 30), (6789, 10)) as i (numero, idade)
    on i.numero = p.numero_procedimento;
