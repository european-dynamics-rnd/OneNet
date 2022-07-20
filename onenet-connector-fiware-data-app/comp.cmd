echo on

REM # BUILD
@call mvn clean install -U -e

REM # MAKE docker image
REM # sudo docker build -t data-app-eng:0.1 .
@call docker build -t onenet-connector-fiware-data-app:0.1 .
