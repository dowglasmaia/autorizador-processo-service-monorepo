# syntax=docker/dockerfile:1
# Imagem do frontend (SPEC-13). Build com Node 24; runtime só com Nginx sem privilégios (sem Node/npm).

# --- Estágio de build ---------------------------------------------------------
FROM node:24-alpine AS build
WORKDIR /workspace

# Camada de dependências separada do código para aproveitar o cache.
# Os tipos da API já são versionados em src/app/shared/api: o gerador (api:types) não roda aqui.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# --- Estágio de runtime -------------------------------------------------------
FROM nginxinc/nginx-unprivileged:stable-alpine
# Copia só o resultado estático (browser/) e a configuração do Nginx.
# Os .template passam por envsubst no início do contêiner (mecanismo da imagem) e viram arquivos de conf.d.
COPY nginx/security-headers.inc /etc/nginx/conf.d/
COPY nginx/default.conf.template nginx/limites.conf.template /etc/nginx/templates/
# Limites configuráveis em tempo de execução (-e NGINX_...): corpo, buffer de cabeçalho e login por IP.
ENV NGINX_MAX_BODY=64k     NGINX_MAX_HEADER_BUFFER=8k     NGINX_LOGIN_RATE=30r/m     NGINX_LOGIN_BURST=10
COPY --from=build /workspace/dist/app-autorizador-processo/browser /usr/share/nginx/html

# Escuta em 8080 (usuário não-root não abre a porta 80).
EXPOSE 8080
