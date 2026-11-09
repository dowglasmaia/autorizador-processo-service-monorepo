# Memória de produto (leitura obrigatória em toda spec)

Resumo curto. Fonte completa: `.specs/00-product-discovery/PRODUCT_CONTEXT.md`.

- **Produto:** Autorizador de Procedimentos Médicos. Catálogo de procedimentos (sexos e faixas etárias permitidos) + autorização por sexo **E** idade.
- **Regras invariantes:** RN1–RN11 em `PRODUCT_CONTEXT.md` §7. Em especial: nº do procedimento único e imutável; decisão = sexo ∈ sexos **E** faixa etária da idade ∈ faixas aceitas (CRIANCA 0–12, ADOLESCENTE 13–20, ADULTO 21–59, IDOSO 60+; ADR-19; sexo sem diferenciar caixa); só concedidas são gravadas; várias autorizações por procedimento; auditoria Envers.
- **Não alterar** regra de negócio sem autorização registrada (`shared/como-executar.md`). Divergências aprovadas estão em `.specs/00-product-discovery/BASELINE_COMPORTAMENTO_ORIGINAL.md` §3 (DIV-01..07).
- **Nova feature de backend:** campos do "Cadastrar procedimento" do protótipo (SPEC-12). São dados de catálogo; **não** alteram a decisão de autorização (PEN-02).
- **Idioma:** produto e UI em português do Brasil. Código e identificadores de API em português de domínio (`procedimento`, `autorizacao`, `numProcedimento`).
- **Nomenclatura proibida no backend:** não usar "legado"/"legacy" em código, pacotes, classes, pastas, testes ou scripts. Usar "baseline" ou "versão original".
- **Login por e-mail e senha com JWT** em todas as rotas (exceto o próprio login e o health). **Sem perfis/papéis** (PEN-11).
