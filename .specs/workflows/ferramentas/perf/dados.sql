-- Dados sintéticos do smoke de desempenho (SPEC-42): 1.000 procedimentos e 10.000 autorizações FICTÍCIOS.
-- Idempotente: procedimentos por numero_procedimento (on conflict); autorizações só completam até 10.000 no total.
insert into procedimento (numero_procedimento)
select g from generate_series(1, 1000) g
on conflict (numero_procedimento) do nothing;

insert into procedimento_sexo (procedimento_id, sexo)
select p.id, s.sexo
from procedimento p
cross join (values ('M'), ('F')) as s(sexo)
where p.numero_procedimento <= 1000 and (s.sexo = 'M' or p.numero_procedimento % 3 <> 0)
on conflict do nothing;

insert into procedimento_idade (procedimento_id, idade)
select p.id, i
from procedimento p
cross join generate_series(0, 150, 10) i
where p.numero_procedimento <= 1000
on conflict do nothing;

insert into autorizacao (procedimento_id, permitido, criada_em)
select p.id, (g % 4 <> 0), now() - (g || ' minutes')::interval
from generate_series(1, greatest(0, 10000 - (select count(*) from autorizacao))::int) g
join procedimento p on p.numero_procedimento = 1 + (g % 1000);

analyze;
