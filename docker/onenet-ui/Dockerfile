FROM eurodynrnd/onenet-ui:latest

ARG CENTRAL_REGISTRY_URL
RUN echo "window['backend_url'] = '"${CENTRAL_REGISTRY_URL}"';" >> /usr/share/nginx/html/assets/config.js
