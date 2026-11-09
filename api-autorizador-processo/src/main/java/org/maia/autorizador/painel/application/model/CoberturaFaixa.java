package org.maia.autorizador.painel.application.model;

/** Quantos procedimentos aceitam uma faixa etária (nome da faixa: CRIANCA, ADOLESCENTE, ADULTO ou IDOSO). */
public record CoberturaFaixa(String faixa, int quantidade) {
}
