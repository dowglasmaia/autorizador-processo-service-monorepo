import type { Auth, Autorizacao, Painel, Procedimento } from './api-types';

// Aliases reexportados do arquivo gerado (nenhum DTO é escrito à mão).
export type LoginRequest = Auth.components['schemas']['LoginRequest'];
export type TokenResponse = Auth.components['schemas']['TokenResponse'];
export type Usuario = Auth.components['schemas']['Usuario'];

export type ProcedimentoRequest = Procedimento.components['schemas']['ProcedimentoRequest'];
export type ProcedimentoDto = Procedimento.components['schemas']['Procedimento'];
export type PaginaProcedimentos = Procedimento.components['schemas']['PaginaProcedimentos'];

export type AutorizacaoRequest = Autorizacao.components['schemas']['AutorizacaoRequest'];
export type AutorizacaoDto = Autorizacao.components['schemas']['Autorizacao'];
export type PaginaAutorizacoes = Autorizacao.components['schemas']['PaginaAutorizacoes'];

export type ResumoPainel = Painel.components['schemas']['ResumoPainel'];
export type CoberturaFaixa = Painel.components['schemas']['CoberturaFaixa'];
export type DistribuicaoSexos = Painel.components['schemas']['DistribuicaoSexos'];

export type ProblemDetail = Procedimento.components['schemas']['ProblemDetail'];
export type FieldViolation = Procedimento.components['schemas']['FieldViolation'];
export type ProblemCode = NonNullable<ProblemDetail['code']>;

export type { Auth, Autorizacao, Painel, Procedimento };
