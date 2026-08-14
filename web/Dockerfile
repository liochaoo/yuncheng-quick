ARG NODE_IMAGE=node:22.23.1-bookworm-slim
ARG NGINX_IMAGE=nginxinc/nginx-unprivileged:1.30.2-alpine

FROM --platform=$BUILDPLATFORM ${NODE_IMAGE} AS build

ENV CI=true \
    PNPM_HOME=/pnpm \
    PATH=/pnpm:$PATH

WORKDIR /workspace

RUN corepack enable \
    && corepack prepare pnpm@10.33.4 --activate

COPY . .

RUN --mount=type=cache,id=yuncheng-quick-pnpm,target=/pnpm/store,sharing=locked \
    pnpm config set store-dir /pnpm/store \
    && pnpm install --frozen-lockfile \
    && pnpm run build

FROM ${NGINX_IMAGE}

ARG APP_VERSION=dev
ARG VCS_REF=unknown

LABEL org.opencontainers.image.title="Yuncheng Quick Web" \
      org.opencontainers.image.description="Yuncheng Quick web application" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${VCS_REF}" \
      org.opencontainers.image.source="https://github.com/liochaoo/yuncheng-quick"

ENV BACKEND_URL=http://yuncheng-quick-server:8087 \
    NGINX_ENVSUBST_FILTER=BACKEND_URL

COPY --from=build /workspace/apps/web-ele/dist/ /usr/share/nginx/html/
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template

EXPOSE 8080
