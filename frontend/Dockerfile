# =============================================================================
# Imagen del frontend. Se compila la SPA y se sirve con nginx, que ademas hace
# de proxy hacia la API: asi el navegador habla con un solo origen y no hay
# que lidiar con CORS en produccion.
#
# Se usa node:24 porque trae npm 11. npm 10.9.x tiene un bug resolviendo peer
# dependencies ("Cannot read properties of null (reading 'edgesOut')") que
# rompe el install de este proyecto.
# =============================================================================

# ---------- Etapa 1: compilacion ----------
FROM node:24-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .
RUN npm run build

# ---------- Etapa 2: servir ----------
FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
