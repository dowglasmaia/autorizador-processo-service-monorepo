Aplica-se a qualquer spec do projeto. Objetivo: trabalho rastreável, com evidência curta e verificável.

1. Antes de iniciar
   Leia a spec: objetivo, escopo, restrições e critérios de aceitação.
   Consulte o contexto técnico global e as instruções aplicáveis.
   Confirme que a execução está autorizada. Aprovar o documento não autoriza implementar ou migrar. Reaproveite autorizações já dadas para o mesmo escopo.
   Verifique o estado atual do projeto e preserve alterações que não pertencem à spec.
   Não invente requisitos. Pergunte apenas o que bloqueia o próximo passo.
2. Durante a execução
   Execute as tarefas pela ordem e respeitando as dependências.
   Use a ferramenta indicada na tarefa (skill, script ou gerador), depois de ler as suas instruções.
   Escolhas técnicas locais, que não alteram comportamento, contrato nem escopo, podem ser decididas e registadas na evidência.
   Mudanças de comportamento, contrato, escopo ou arquitetura aprovada exigem autorização antes de executar a parte afetada.
   Pode detalhar ou dividir tarefas, mantendo a original visível.
   Se uma tarefa deixar de fazer sentido, proponha cancelamento ou substituição. Mantenha-a desmarcada, marcada como cancelada, com motivo e aprovação.
   Em caso de conflito entre documentos, registe a divergência e resolva só o necessário para avançar.
   Ao interromper, registe o ponto de paragem e a próxima ação. Ao retomar, confira o estado atual antes de repetir operações.
3. Evidência

Registe uma linha curta abaixo da tarefa, com data, hora e fuso.

Concluída

md
- [x] Descrição da tarefa.
  > ✅ 2026-11-08 12:39 +00:00 — Resultado. Verificação: comando/revisão e resultado. Desvio: se houver.

Parcial ou bloqueada

md
- [ ] Descrição da tarefa.
  > ⏳ 2026-11-08 12:39 +00:00 — Parcial: o que foi feito. Falta: o que resta. Próxima ação: passo.
  > ⛔ 2026-11-08 12:39 +00:00 — Bloqueio: motivo. Para desbloquear: o que é necessário.

Regras:

Indique o resultado observado, não apenas “feito” ou “testado”.
Distinga: teste executado, teste só escrito e verificação não executada.
Cite ficheiros e comandos só o suficiente para reproduzir a verificação.
Não cole logs completos, credenciais ou dados reais.
Não invente horários, resultados nem aprovações.
Um bloqueio afeta a tarefa e as dependentes. Continue as independentes.
4. Conclusão

A spec está concluída quando:

todas as tarefas ativas estão marcadas, com evidência;
os critérios de aceitação foram atendidos e verificados;
as verificações obrigatórias foram executadas com sucesso;
não há bloqueios nem falhas relevantes em aberto;
desvios e cancelamentos estão aprovados e registados;
a documentação afetada está coerente com o resultado.

Tarefas canceladas com aprovação não contam como pendentes. Uma verificação obrigatória não executada mantém a tarefa pendente.

Ao concluir:

Acrescente um resumo curto: resultado, validações e limitações aceites.
Mova a spec para .specs/archive/ (crie a pasta se não existir) com git mv, para preservar o histórico.
