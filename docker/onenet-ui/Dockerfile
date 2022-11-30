FROM eurodynrnd/onenet-ui:latest

EXPOSE 5000

ARG CENTRAL_REGISTRY_URL
ENV CENTRAL_REGISTRY_URL_ENV=${CENTRAL_REGISTRY_URL}

RUN echo "window['backend_url'] = '"${CENTRAL_REGISTRY_URL_ENV}"'; window['backend_on_proxy_path'] = '/api';" >> /usr/share/nginx/html/assets/config.js
