# Dockerfile pour le frontend Angular
FROM node:20-alpine AS builder

WORKDIR /app

# Copier package.json et package-lock.json
COPY package*.json ./
RUN npm ci --only=production

# Copier le code source
COPY . .

# Build de production
RUN npm run build

# Stage de production avec Nginx
FROM nginx:alpine

# Copier les fichiers buildés
COPY --from=builder /app/dist/app-spa-frontend /usr/share/nginx/html

# Configuration Nginx pour Angular (SPA)
COPY nginx.conf /etc/nginx/nginx.conf

# Variables d'environnement pour l'URL de l'API
ENV API_URL=https://spa-api.loicpincon.fr
ENV MQTT_WS_URL=wss://mqtt.loicpincon.fr

EXPOSE 80

# Script pour remplacer les variables d'environnement au runtime
RUN echo '#!/bin/sh' > /docker-entrypoint.sh && \
    echo 'envsubst < /usr/share/nginx/html/assets/env.template.js > /usr/share/nginx/html/assets/env.js' >> /docker-entrypoint.sh && \
    echo 'nginx -g "daemon off;"' >> /docker-entrypoint.sh && \
    chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]