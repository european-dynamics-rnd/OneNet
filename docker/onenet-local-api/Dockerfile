FROM eurodynrnd/onenet-local-api:latest

EXPOSE 30001

ARG CENTRAL_REGISTRY
ENV CENTRAL_REGISTRY_ENV=${CENTRAL_REGISTRY}

RUN touch ./entrypoint.sh
RUN echo "java -jar ./app.jar --sofia.uri=${CENTRAL_REGISTRY_ENV}" > ./entrypoint.sh
RUN chmod +x ./entrypoint.sh
CMD ./entrypoint.sh
