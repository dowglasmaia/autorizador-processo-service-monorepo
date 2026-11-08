/* eslint-disable */
// ARQUIVO GERADO por `npm run api:types` (scripts/gerar-api-types.mjs). NÃO EDITE À MÃO.
// Fonte: api-autorizador-processo/src/main/resources/openapi/modules/*.yaml

export namespace Auth {
  export interface paths {
      "/auth/login": {
          parameters: {
              query?: never;
              header?: never;
              path?: never;
              cookie?: never;
          };
          get?: never;
          put?: never;
          /** Autentica com e-mail e senha */
          post: operations["autenticar"];
          delete?: never;
          options?: never;
          head?: never;
          patch?: never;
          trace?: never;
      };
      "/auth/me": {
          parameters: {
              query?: never;
              header?: never;
              path?: never;
              cookie?: never;
          };
          /** Obtém o usuário autenticado */
          get: operations["obterUsuarioAtual"];
          put?: never;
          post?: never;
          delete?: never;
          options?: never;
          head?: never;
          patch?: never;
          trace?: never;
      };
  }
  export type webhooks = Record<string, never>;
  export interface components {
      schemas: {
          LoginRequest: {
              /**
               * Format: email
               * @description E-mail do usuário (normalizado no servidor).
               */
              email: string;
              /** @description Senha em texto; nunca é registrada em log. */
              senha: string;
          };
          Usuario: {
              /** Format: int64 */
              id: number;
              /** Format: email */
              email: string;
          };
          TokenResponse: {
              /** @description JWT HS256. */
              accessToken: string;
              /** @enum {string} */
              tokenType: "Bearer";
              /**
               * Format: int64
               * @description Validade do token em segundos.
               */
              expiraEm: number;
              usuario: components["schemas"]["Usuario"];
          };
          /** @description Violação de validação de um campo. */
          FieldViolation: {
              /**
               * @description Nome do campo ou parâmetro inválido.
               * @example numProcedimento
               */
              campo: string;
              /**
               * @description Mensagem em português.
               * @example deve ser maior ou igual a 1
               */
              mensagem: string;
          };
          /** @description Corpo de erro RFC 9457 (application/problem+json) com extensões. */
          ProblemDetail: {
              /**
               * Format: uri
               * @description URI que identifica o tipo do problema.
               * @example about:blank
               */
              type?: string;
              /** @description Resumo curto do problema. */
              title: string;
              /**
               * Format: int32
               * @description Status HTTP.
               */
              status: number;
              /** @description Explicação em português do problema. */
              detail?: string;
              /** @description Caminho da requisição que originou o problema. */
              instance?: string;
              /**
               * @description Código estável usado pelo cliente para decidir o tratamento.
               * @enum {string}
               */
              code: "VALIDACAO_INVALIDA" | "CORPO_INVALIDO" | "PARAMETRO_INVALIDO" | "PROCEDIMENTO_DUPLICADO" | "AUTORIZACAO_NEGADA" | "PROCEDIMENTO_NAO_ENCONTRADO" | "ERRO_INTERNO" | "CREDENCIAIS_INVALIDAS" | "NAO_AUTENTICADO" | "SESSAO_EXPIRADA" | "ACESSO_NEGADO" | "MUITAS_TENTATIVAS";
              /**
               * Format: date-time
               * @description Instante do erro em UTC.
               */
              timestamp: string;
              /** @description Violações por campo; presente apenas em 400 de validação. */
              errors?: components["schemas"]["FieldViolation"][];
          };
      };
      responses: {
          /** @description Requisição inválida (VALIDACAO_INVALIDA, CORPO_INVALIDO ou PARAMETRO_INVALIDO). */
          BadRequestResponse: {
              headers: {
                  [name: string]: unknown;
              };
              content: {
                  "application/problem+json": components["schemas"]["ProblemDetail"];
              };
          };
          /** @description Não autenticado (CREDENCIAIS_INVALIDAS, NAO_AUTENTICADO ou SESSAO_EXPIRADA), com cabeçalho WWW-Authenticate Bearer. */
          UnauthorizedResponse: {
              headers: {
                  /** @description Esquema de autenticação exigido. */
                  "WWW-Authenticate"?: string;
                  [name: string]: unknown;
              };
              content: {
                  "application/problem+json": components["schemas"]["ProblemDetail"];
              };
          };
          /** @description Muitas tentativas de login (MUITAS_TENTATIVAS). */
          TooManyRequestsResponse: {
              headers: {
                  /** @description Segundos até nova tentativa. */
                  "Retry-After"?: number;
                  [name: string]: unknown;
              };
              content: {
                  "application/problem+json": components["schemas"]["ProblemDetail"];
              };
          };
          /** @description Falha inesperada (ERRO_INTERNO), sem detalhes técnicos. */
          InternalServerErrorResponse: {
              headers: {
                  [name: string]: unknown;
              };
              content: {
                  "application/problem+json": components["schemas"]["ProblemDetail"];
              };
          };
      };
      parameters: never;
      requestBodies: never;
      headers: never;
      pathItems: never;
  }
  export type $defs = Record<string, never>;
  export interface operations {
      autenticar: {
          parameters: {
              query?: never;
              header?: never;
              path?: never;
              cookie?: never;
          };
          requestBody: {
              content: {
                  "application/json": components["schemas"]["LoginRequest"];
              };
          };
          responses: {
              /** @description Autenticado. */
              200: {
                  headers: {
                      [name: string]: unknown;
                  };
                  content: {
                      "application/json": components["schemas"]["TokenResponse"];
                  };
              };
              400: components["responses"]["BadRequestResponse"];
              401: components["responses"]["UnauthorizedResponse"];
              429: components["responses"]["TooManyRequestsResponse"];
              500: components["responses"]["InternalServerErrorResponse"];
          };
      };
      obterUsuarioAtual: {
          parameters: {
              query?: never;
              header?: never;
              path?: never;
              cookie?: never;
          };
          requestBody?: never;
          responses: {
              /** @description Usuário autenticado. */
              200: {
                  headers: {
                      [name: string]: unknown;
                  };
                  content: {
                      "application/json": components["schemas"]["Usuario"];
                  };
              };
              401: components["responses"]["UnauthorizedResponse"];
              500: components["responses"]["InternalServerErrorResponse"];
          };
      };
  }
}

export namespace Autorizacao {
  export interface paths {
      "/autorizacoes": {
          parameters: {
              query?: never;
              header?: never;
              path?: never;
              cookie?: never;
          };
          /** Lista autorizações paginadas, das mais recentes para as mais antigas */
          get: operations["listarAutorizacoes"];
          put?: never;
          /**
           * Solicita a autorização de um procedimento
           * @description Registra a solicitação quando sexo e idade são aderentes ao procedimento.
           */
          post: operations["solicitarAutorizacao"];
          delete?: never;
          options?: never;
          head?: never;
          patch?: never;
          trace?: never;
      };
  }
  export type webhooks = Record<string, never>;
  export interface components {
      schemas: {
          AutorizacaoRequest: {
              /**
               * Format: int32
               * @description Número do procedimento.
               */
              numProcedimento: number;
              /**
               * Format: int32
               * @description Idade do beneficiário.
               */
              idade: number;
              /** @description Sexo do beneficiário (M ou F, aceita minúsculas). */
              sexo: string;
          };
          Autorizacao: {
              /** Format: int64 */
              id: number;
              procedimento: components["schemas"]["Procedimento"];
              /** @description Resultado da autorização. */
              permitido: boolean;
              /**
               * Format: date-time
               * @description Data de criação em UTC; pode ser nula.
               */
              criadaEm: string | null;
          };
          PaginaAutorizacoes: {
              itens: components["schemas"]["Autorizacao"][];
              /** Format: int32 */
              pagina: number;
              /** Format: int32 */
              tamanho: number;
              /** Format: int64 */
              totalItens: number;
              /** Format: int32 */
              totalPaginas: number;
          };
          /**
           * @description Categoria do procedimento (lista do protótipo).
           * @enum {string}
           */
          Categoria: "EXAMES_IMAGEM" | "CONSULTAS" | "CIRURGIAS" | "TERAPIAS" | "EXAMES_LABORATORIAIS";
          /**
           * @description Especialidade do procedimento (lista do protótipo).
           * @enum {string}
           */
          Especialidade: "CARDIOLOGIA" | "ORTOPEDIA" | "PEDIATRIA" | "GINECOLOGIA" | "NEUROLOGIA";
          /**
           * @description Tipo de cobertura; informativo, não altera a decisão de autorização.
           * @enum {string}
           */
          TipoCobertura: "PLANO" | "REGRAS" | "NAO_COBERTO";
          /**
           * @description Período a que se refere a quantidade máxima.
           * @enum {string}
           */
          Periodo: "MES" | "ANO" | "VIDA";
          /**
           * @description Sexo aceito pelo procedimento. A entrada também aceita minúsculas; a saída é sempre M ou F.
           * @enum {string}
           */
          Sexo: "M" | "F";
          Procedimento: {
              /** Format: int64 */
              id: number;
              /** Format: int32 */
              numProcedimento: number;
              /** @description Ausente (nulo) em procedimentos anteriores ao catálogo estendido; o mesmo vale para os demais campos do catálogo. */
              nome?: string;
              codigoTuss?: string;
              categoria?: components["schemas"]["Categoria"];
              especialidade?: components["schemas"]["Especialidade"];
              tipoCobertura?: components["schemas"]["TipoCobertura"];
              /** Format: int32 */
              quantidadeMaxima?: number;
              periodo?: components["schemas"]["Periodo"];
              exigeAutorizacaoPrevia?: boolean;
              exigeJustificativaMedica?: boolean;
              /** @description Sexos aceitos, na ordem M, F. */
              sexos: components["schemas"]["Sexo"][];
              /** @description Idades aceitas, em ordem crescente. */
              idades: number[];
              /**
               * Format: date-time
               * @description Data de criação em UTC.
               */
              criadoEm: string;
          };
          /** @description Violação de validação de um campo. */
          FieldViolation: {
              /**
               * @description Nome do campo ou parâmetro inválido.
               * @example numProcedimento
               */
              campo: string;
              /**
               * @description Mensagem em português.
               * @example deve ser maior ou igual a 1
               */
              mensagem: string;
          };
          /** @description Corpo de erro RFC 9457 (application/problem+json) com extensões. */
          ProblemDetail: {
              /**
               * Format: uri
               * @description URI que identifica o tipo do problema.
               * @example about:blank
               */
              type?: string;
              /** @description Resumo curto do problema. */
              title: string;
              /**
               * Format: int32
               * @description Status HTTP.
               */
              status: number;
              /** @description Explicação em português do problema. */
              detail?: string;
              /** @description Caminho da requisição que originou o problema. */
              instance?: string;
              /**
               * @description Código estável usado pelo cliente para decidir o tratamento.
               * @enum {string}
               */
              code: "VALIDACAO_INVALIDA" | "CORPO_INVALIDO" | "PARAMETRO_INVALIDO" | "PROCEDIMENTO_DUPLICADO" | "AUTORIZACAO_NEGADA" | "PROCEDIMENTO_NAO_ENCONTRADO" | "ERRO_INTERNO" | "CREDENCIAIS_INVALIDAS" | "NAO_AUTENTICADO" | "SESSAO_EXPIRADA" | "ACESSO_NEGADO" | "MUITAS_TENTATIVAS";
              /**
               * Format: date-time
               * @description Instante do erro em UTC.
               */
              timestamp: string;
              /** @description Violações por campo; presente apenas em 400 de validação. */
              errors?: components["schemas"]["FieldViolation"][];
          };
      };
      responses: {
          /** @description Requisição inválida (VALIDACAO_INVALIDA, CORPO_INVALIDO ou PARAMETRO_INVALIDO). */
          BadRequestResponse: {
              headers: {
                  [name: string]: unknown;
              };
              content: {
                  "application/problem+json": components["schemas"]["ProblemDetail"];
              };
          };
          /** @description Falha inesperada (ERRO_INTERNO), sem detalhes técnicos. */
          InternalServerErrorResponse: {
              headers: {
                  [name: string]: unknown;
              };
              content: {
                  "application/problem+json": components["schemas"]["ProblemDetail"];
              };
          };
          /** @description Operação não permitida (PROCEDIMENTO_DUPLICADO ou AUTORIZACAO_NEGADA). */
          ForbiddenResponse: {
              headers: {
                  [name: string]: unknown;
              };
              content: {
                  "application/problem+json": components["schemas"]["ProblemDetail"];
              };
          };
          /** @description Recurso não encontrado (PROCEDIMENTO_NAO_ENCONTRADO). */
          NotFoundResponse: {
              headers: {
                  [name: string]: unknown;
              };
              content: {
                  "application/problem+json": components["schemas"]["ProblemDetail"];
              };
          };
      };
      parameters: never;
      requestBodies: never;
      headers: never;
      pathItems: never;
  }
  export type $defs = Record<string, never>;
  export interface operations {
      listarAutorizacoes: {
          parameters: {
              query?: {
                  /** @description Número da página, começando em 0. */
                  page?: number;
                  /** @description Itens por página. */
                  size?: number;
              };
              header?: never;
              path?: never;
              cookie?: never;
          };
          requestBody?: never;
          responses: {
              /** @description Página de autorizações. */
              200: {
                  headers: {
                      [name: string]: unknown;
                  };
                  content: {
                      "application/json": components["schemas"]["PaginaAutorizacoes"];
                  };
              };
              400: components["responses"]["BadRequestResponse"];
              500: components["responses"]["InternalServerErrorResponse"];
          };
      };
      solicitarAutorizacao: {
          parameters: {
              query?: never;
              header?: never;
              path?: never;
              cookie?: never;
          };
          requestBody: {
              content: {
                  "application/json": components["schemas"]["AutorizacaoRequest"];
              };
          };
          responses: {
              /** @description Autorização concedida e registrada. */
              201: {
                  headers: {
                      [name: string]: unknown;
                  };
                  content: {
                      "application/json": components["schemas"]["Autorizacao"];
                  };
              };
              400: components["responses"]["BadRequestResponse"];
              403: components["responses"]["ForbiddenResponse"];
              404: components["responses"]["NotFoundResponse"];
              500: components["responses"]["InternalServerErrorResponse"];
          };
      };
  }
}

export namespace Painel {
  export interface paths {
      "/painel/resumo": {
          parameters: {
              query?: never;
              header?: never;
              path?: never;
              cookie?: never;
          };
          /** Obtém o resumo do painel */
          get: operations["obterResumoPainel"];
          put?: never;
          post?: never;
          delete?: never;
          options?: never;
          head?: never;
          patch?: never;
          trace?: never;
      };
  }
  export type webhooks = Record<string, never>;
  export interface components {
      schemas: {
          CoberturaIdade: {
              /** Format: int32 */
              idade: number;
              /**
               * Format: int32
               * @description Número de procedimentos que aceitam a idade.
               */
              quantidade: number;
          };
          DistribuicaoSexos: {
              /**
               * Format: int32
               * @description Procedimentos que aceitam o sexo masculino.
               */
              masculino: number;
              /**
               * Format: int32
               * @description Procedimentos que aceitam o sexo feminino.
               */
              feminino: number;
          };
          ResumoPainel: {
              /** Format: int32 */
              totalProcedimentos: number;
              /** Format: int32 */
              totalAutorizacoes: number;
              /** Format: int32 */
              idadesCobertas: number;
              /** @description Ordenada por idade ascendente. */
              coberturaPorIdade: components["schemas"]["CoberturaIdade"][];
              distribuicaoSexos: components["schemas"]["DistribuicaoSexos"];
              /** @description Até 4 autorizações mais recentes. */
              ultimasAutorizacoes: components["schemas"]["Autorizacao"][];
          };
          /**
           * @description Categoria do procedimento (lista do protótipo).
           * @enum {string}
           */
          Categoria: "EXAMES_IMAGEM" | "CONSULTAS" | "CIRURGIAS" | "TERAPIAS" | "EXAMES_LABORATORIAIS";
          /**
           * @description Especialidade do procedimento (lista do protótipo).
           * @enum {string}
           */
          Especialidade: "CARDIOLOGIA" | "ORTOPEDIA" | "PEDIATRIA" | "GINECOLOGIA" | "NEUROLOGIA";
          /**
           * @description Tipo de cobertura; informativo, não altera a decisão de autorização.
           * @enum {string}
           */
          TipoCobertura: "PLANO" | "REGRAS" | "NAO_COBERTO";
          /**
           * @description Período a que se refere a quantidade máxima.
           * @enum {string}
           */
          Periodo: "MES" | "ANO" | "VIDA";
          /**
           * @description Sexo aceito pelo procedimento. A entrada também aceita minúsculas; a saída é sempre M ou F.
           * @enum {string}
           */
          Sexo: "M" | "F";
          Procedimento: {
              /** Format: int64 */
              id: number;
              /** Format: int32 */
              numProcedimento: number;
              /** @description Ausente (nulo) em procedimentos anteriores ao catálogo estendido; o mesmo vale para os demais campos do catálogo. */
              nome?: string;
              codigoTuss?: string;
              categoria?: components["schemas"]["Categoria"];
              especialidade?: components["schemas"]["Especialidade"];
              tipoCobertura?: components["schemas"]["TipoCobertura"];
              /** Format: int32 */
              quantidadeMaxima?: number;
              periodo?: components["schemas"]["Periodo"];
              exigeAutorizacaoPrevia?: boolean;
              exigeJustificativaMedica?: boolean;
              /** @description Sexos aceitos, na ordem M, F. */
              sexos: components["schemas"]["Sexo"][];
              /** @description Idades aceitas, em ordem crescente. */
              idades: number[];
              /**
               * Format: date-time
               * @description Data de criação em UTC.
               */
              criadoEm: string;
          };
          Autorizacao: {
              /** Format: int64 */
              id: number;
              procedimento: components["schemas"]["Procedimento"];
              /** @description Resultado da autorização. */
              permitido: boolean;
              /**
               * Format: date-time
               * @description Data de criação em UTC; pode ser nula.
               */
              criadaEm: string | null;
          };
          /** @description Violação de validação de um campo. */
          FieldViolation: {
              /**
               * @description Nome do campo ou parâmetro inválido.
               * @example numProcedimento
               */
              campo: string;
              /**
               * @description Mensagem em português.
               * @example deve ser maior ou igual a 1
               */
              mensagem: string;
          };
          /** @description Corpo de erro RFC 9457 (application/problem+json) com extensões. */
          ProblemDetail: {
              /**
               * Format: uri
               * @description URI que identifica o tipo do problema.
               * @example about:blank
               */
              type?: string;
              /** @description Resumo curto do problema. */
              title: string;
              /**
               * Format: int32
               * @description Status HTTP.
               */
              status: number;
              /** @description Explicação em português do problema. */
              detail?: string;
              /** @description Caminho da requisição que originou o problema. */
              instance?: string;
              /**
               * @description Código estável usado pelo cliente para decidir o tratamento.
               * @enum {string}
               */
              code: "VALIDACAO_INVALIDA" | "CORPO_INVALIDO" | "PARAMETRO_INVALIDO" | "PROCEDIMENTO_DUPLICADO" | "AUTORIZACAO_NEGADA" | "PROCEDIMENTO_NAO_ENCONTRADO" | "ERRO_INTERNO" | "CREDENCIAIS_INVALIDAS" | "NAO_AUTENTICADO" | "SESSAO_EXPIRADA" | "ACESSO_NEGADO" | "MUITAS_TENTATIVAS";
              /**
               * Format: date-time
               * @description Instante do erro em UTC.
               */
              timestamp: string;
              /** @description Violações por campo; presente apenas em 400 de validação. */
              errors?: components["schemas"]["FieldViolation"][];
          };
      };
      responses: {
          /** @description Falha inesperada (ERRO_INTERNO), sem detalhes técnicos. */
          InternalServerErrorResponse: {
              headers: {
                  [name: string]: unknown;
              };
              content: {
                  "application/problem+json": components["schemas"]["ProblemDetail"];
              };
          };
      };
      parameters: never;
      requestBodies: never;
      headers: never;
      pathItems: never;
  }
  export type $defs = Record<string, never>;
  export interface operations {
      obterResumoPainel: {
          parameters: {
              query?: never;
              header?: never;
              path?: never;
              cookie?: never;
          };
          requestBody?: never;
          responses: {
              /** @description Resumo do painel. */
              200: {
                  headers: {
                      [name: string]: unknown;
                  };
                  content: {
                      "application/json": components["schemas"]["ResumoPainel"];
                  };
              };
              500: components["responses"]["InternalServerErrorResponse"];
          };
      };
  }
}

export namespace Procedimento {
  export interface paths {
      "/procedimentos": {
          parameters: {
              query?: never;
              header?: never;
              path?: never;
              cookie?: never;
          };
          /**
           * Lista procedimentos paginados
           * @description Filtra `numProcedimento` por prefixo quando `q` é informado.
           */
          get: operations["listarProcedimentos"];
          put?: never;
          /** Cadastra um procedimento */
          post: operations["cadastrarProcedimento"];
          delete?: never;
          options?: never;
          head?: never;
          patch?: never;
          trace?: never;
      };
      "/procedimentos/{id}": {
          parameters: {
              query?: never;
              header?: never;
              path?: never;
              cookie?: never;
          };
          /** Obtém um procedimento pelo identificador */
          get: operations["obterProcedimento"];
          put?: never;
          post?: never;
          delete?: never;
          options?: never;
          head?: never;
          patch?: never;
          trace?: never;
      };
  }
  export type webhooks = Record<string, never>;
  export interface components {
      schemas: {
          /**
           * @description Sexo aceito pelo procedimento. A entrada também aceita minúsculas; a saída é sempre M ou F.
           * @enum {string}
           */
          Sexo: "M" | "F";
          /**
           * @description Categoria do procedimento (lista do protótipo).
           * @enum {string}
           */
          Categoria: "EXAMES_IMAGEM" | "CONSULTAS" | "CIRURGIAS" | "TERAPIAS" | "EXAMES_LABORATORIAIS";
          /**
           * @description Especialidade do procedimento (lista do protótipo).
           * @enum {string}
           */
          Especialidade: "CARDIOLOGIA" | "ORTOPEDIA" | "PEDIATRIA" | "GINECOLOGIA" | "NEUROLOGIA";
          /**
           * @description Tipo de cobertura; informativo, não altera a decisão de autorização.
           * @enum {string}
           */
          TipoCobertura: "PLANO" | "REGRAS" | "NAO_COBERTO";
          /**
           * @description Período a que se refere a quantidade máxima.
           * @enum {string}
           */
          Periodo: "MES" | "ANO" | "VIDA";
          ProcedimentoRequest: {
              /**
               * Format: int32
               * @description Número do procedimento.
               */
              numProcedimento: number;
              /** @description Nome do procedimento (texto livre; espaços nas pontas são removidos). */
              nome: string;
              /** @description Código TUSS. */
              codigoTuss?: string;
              categoria: components["schemas"]["Categoria"];
              especialidade?: components["schemas"]["Especialidade"];
              tipoCobertura?: components["schemas"]["TipoCobertura"];
              /**
               * Format: int32
               * @description Quantidade máxima por período; exige `periodo`.
               */
              quantidadeMaxima?: number;
              periodo?: components["schemas"]["Periodo"];
              /** @description Informativo; padrão `false`. */
              exigeAutorizacaoPrevia?: boolean;
              /** @description Informativo; padrão `false`. */
              exigeJustificativaMedica?: boolean;
              /** @description Sexos aceitos; pode ser vazio. */
              sexos?: components["schemas"]["Sexo"][];
              /** @description Idades aceitas; pode ser vazio. */
              idades?: number[];
          };
          Procedimento: {
              /** Format: int64 */
              id: number;
              /** Format: int32 */
              numProcedimento: number;
              /** @description Ausente (nulo) em procedimentos anteriores ao catálogo estendido; o mesmo vale para os demais campos do catálogo. */
              nome?: string;
              codigoTuss?: string;
              categoria?: components["schemas"]["Categoria"];
              especialidade?: components["schemas"]["Especialidade"];
              tipoCobertura?: components["schemas"]["TipoCobertura"];
              /** Format: int32 */
              quantidadeMaxima?: number;
              periodo?: components["schemas"]["Periodo"];
              exigeAutorizacaoPrevia?: boolean;
              exigeJustificativaMedica?: boolean;
              /** @description Sexos aceitos, na ordem M, F. */
              sexos: components["schemas"]["Sexo"][];
              /** @description Idades aceitas, em ordem crescente. */
              idades: number[];
              /**
               * Format: date-time
               * @description Data de criação em UTC.
               */
              criadoEm: string;
          };
          PaginaProcedimentos: {
              itens: components["schemas"]["Procedimento"][];
              /** Format: int32 */
              pagina: number;
              /** Format: int32 */
              tamanho: number;
              /** Format: int64 */
              totalItens: number;
              /** Format: int32 */
              totalPaginas: number;
          };
          /** @description Violação de validação de um campo. */
          FieldViolation: {
              /**
               * @description Nome do campo ou parâmetro inválido.
               * @example numProcedimento
               */
              campo: string;
              /**
               * @description Mensagem em português.
               * @example deve ser maior ou igual a 1
               */
              mensagem: string;
          };
          /** @description Corpo de erro RFC 9457 (application/problem+json) com extensões. */
          ProblemDetail: {
              /**
               * Format: uri
               * @description URI que identifica o tipo do problema.
               * @example about:blank
               */
              type?: string;
              /** @description Resumo curto do problema. */
              title: string;
              /**
               * Format: int32
               * @description Status HTTP.
               */
              status: number;
              /** @description Explicação em português do problema. */
              detail?: string;
              /** @description Caminho da requisição que originou o problema. */
              instance?: string;
              /**
               * @description Código estável usado pelo cliente para decidir o tratamento.
               * @enum {string}
               */
              code: "VALIDACAO_INVALIDA" | "CORPO_INVALIDO" | "PARAMETRO_INVALIDO" | "PROCEDIMENTO_DUPLICADO" | "AUTORIZACAO_NEGADA" | "PROCEDIMENTO_NAO_ENCONTRADO" | "ERRO_INTERNO" | "CREDENCIAIS_INVALIDAS" | "NAO_AUTENTICADO" | "SESSAO_EXPIRADA" | "ACESSO_NEGADO" | "MUITAS_TENTATIVAS";
              /**
               * Format: date-time
               * @description Instante do erro em UTC.
               */
              timestamp: string;
              /** @description Violações por campo; presente apenas em 400 de validação. */
              errors?: components["schemas"]["FieldViolation"][];
          };
      };
      responses: {
          /** @description Requisição inválida (VALIDACAO_INVALIDA, CORPO_INVALIDO ou PARAMETRO_INVALIDO). */
          BadRequestResponse: {
              headers: {
                  [name: string]: unknown;
              };
              content: {
                  "application/problem+json": components["schemas"]["ProblemDetail"];
              };
          };
          /** @description Falha inesperada (ERRO_INTERNO), sem detalhes técnicos. */
          InternalServerErrorResponse: {
              headers: {
                  [name: string]: unknown;
              };
              content: {
                  "application/problem+json": components["schemas"]["ProblemDetail"];
              };
          };
          /** @description Operação não permitida (PROCEDIMENTO_DUPLICADO ou AUTORIZACAO_NEGADA). */
          ForbiddenResponse: {
              headers: {
                  [name: string]: unknown;
              };
              content: {
                  "application/problem+json": components["schemas"]["ProblemDetail"];
              };
          };
          /** @description Recurso não encontrado (PROCEDIMENTO_NAO_ENCONTRADO). */
          NotFoundResponse: {
              headers: {
                  [name: string]: unknown;
              };
              content: {
                  "application/problem+json": components["schemas"]["ProblemDetail"];
              };
          };
      };
      parameters: {
          /** @description Número da página, começando em 0. */
          Page: number;
          /** @description Itens por página. */
          Size: number;
      };
      requestBodies: never;
      headers: never;
      pathItems: never;
  }
  export type $defs = Record<string, never>;
  export interface operations {
      listarProcedimentos: {
          parameters: {
              query?: {
                  /** @description Prefixo do número do procedimento (somente dígitos). */
                  q?: string;
                  /** @description Número da página, começando em 0. */
                  page?: components["parameters"]["Page"];
                  /** @description Itens por página. */
                  size?: components["parameters"]["Size"];
                  /** @description Ordenação; aceita apenas `numProcedimento,asc` ou `numProcedimento,desc`. */
                  sort?: "numProcedimento,asc" | "numProcedimento,desc";
              };
              header?: never;
              path?: never;
              cookie?: never;
          };
          requestBody?: never;
          responses: {
              /** @description Página de procedimentos. */
              200: {
                  headers: {
                      [name: string]: unknown;
                  };
                  content: {
                      "application/json": components["schemas"]["PaginaProcedimentos"];
                  };
              };
              400: components["responses"]["BadRequestResponse"];
              500: components["responses"]["InternalServerErrorResponse"];
          };
      };
      cadastrarProcedimento: {
          parameters: {
              query?: never;
              header?: never;
              path?: never;
              cookie?: never;
          };
          requestBody: {
              content: {
                  "application/json": components["schemas"]["ProcedimentoRequest"];
              };
          };
          responses: {
              /** @description Procedimento criado. */
              201: {
                  headers: {
                      /** @description URL do recurso criado, `/api/v1/procedimentos/{id}`. */
                      Location?: string;
                      [name: string]: unknown;
                  };
                  content: {
                      "application/json": components["schemas"]["Procedimento"];
                  };
              };
              400: components["responses"]["BadRequestResponse"];
              403: components["responses"]["ForbiddenResponse"];
              500: components["responses"]["InternalServerErrorResponse"];
          };
      };
      obterProcedimento: {
          parameters: {
              query?: never;
              header?: never;
              path: {
                  /** @description Identificador do procedimento. */
                  id: number;
              };
              cookie?: never;
          };
          requestBody?: never;
          responses: {
              /** @description Procedimento encontrado. */
              200: {
                  headers: {
                      [name: string]: unknown;
                  };
                  content: {
                      "application/json": components["schemas"]["Procedimento"];
                  };
              };
              400: components["responses"]["BadRequestResponse"];
              404: components["responses"]["NotFoundResponse"];
              500: components["responses"]["InternalServerErrorResponse"];
          };
      };
  }
}
