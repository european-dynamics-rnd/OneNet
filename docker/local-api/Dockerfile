FROM eurodynrnd/onenet-local-api:latest

EXPOSE 30000
EXPOSE 30001

ARG SOFIA_URI
ENV SOFIA_URI_ENV=${SOFIA_URI}

COPY data/scripts/start.sh /opt/scripts/
RUN chmod u+x /opt/scripts/start.sh

#Run
ENTRYPOINT ["/opt/scripts/start.sh"]
