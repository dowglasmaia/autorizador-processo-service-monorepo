import type { Procedimento } from '../../shared/api';

type Esquemas = Procedimento.components['schemas'];

export type Categoria = Esquemas['Categoria'];
export type Especialidade = Esquemas['Especialidade'];
export type TipoCobertura = Esquemas['TipoCobertura'];
export type Periodo = Esquemas['Periodo'];

// `Record` sobre os tipos gerados obriga a cobrir todo valor novo do contrato.
export const ROTULO_CATEGORIA: Readonly<Record<Categoria, string>> = {
  EXAMES_IMAGEM: 'Exames de imagem',
  CONSULTAS: 'Consultas',
  CIRURGIAS: 'Cirurgias',
  TERAPIAS: 'Terapias',
  EXAMES_LABORATORIAIS: 'Exames laboratoriais',
};

export const ROTULO_ESPECIALIDADE: Readonly<Record<Especialidade, string>> = {
  CARDIOLOGIA: 'Cardiologia',
  ORTOPEDIA: 'Ortopedia',
  PEDIATRIA: 'Pediatria',
  GINECOLOGIA: 'Ginecologia',
  NEUROLOGIA: 'Neurologia',
};

export const ROTULO_COBERTURA: Readonly<Record<TipoCobertura, string>> = {
  PLANO: 'Coberto pelo plano',
  REGRAS: 'Coberto conforme regras',
  NAO_COBERTO: 'Não coberto',
};

export const ROTULO_PERIODO: Readonly<Record<Periodo, string>> = {
  MES: 'por mês',
  ANO: 'por ano',
  VIDA: 'na vida',
};

export const ROTULO_SEXO: Readonly<Record<'M' | 'F', string>> = {
  M: 'Masculino',
  F: 'Feminino',
};

export const CATEGORIAS = Object.keys(ROTULO_CATEGORIA) as Categoria[];
export const ESPECIALIDADES = Object.keys(ROTULO_ESPECIALIDADE) as Especialidade[];
export const COBERTURAS = Object.keys(ROTULO_COBERTURA) as TipoCobertura[];
export const PERIODOS = Object.keys(ROTULO_PERIODO) as Periodo[];
