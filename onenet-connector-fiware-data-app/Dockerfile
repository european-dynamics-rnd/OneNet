# Start with a base image containing Java runtime

# FROM openjdk:11.0.6-stretch
# FROM openjdk:12-jdk-alpine
# FROM openjdk:12-jdk-oraclelinux7
FROM openjdk:11.0.12-jre

# Add Maintainer Info
LABEL maintainer="gabriele.deluca@eng.it"

# Install whois service
# RUN yum install -y whois

# Add a volume pointing to /tmp
VOLUME /tmp 

# Make port 8083 available to the world outside this container
EXPOSE 8083

# The application's jar file
# ARG JAR_FILE=target/*.jar
COPY target/dependency-jars /run/dependency-jars

# Add the application's jar to the container
# ADD ${JAR_FILE} market4.0-data-app.jar
ADD target/application.jar /run/application.jar

# Run the jar file 
# ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-cp", "market4.0-data-app.jar:/config/", "org.springframework.boot.loader.JarLauncher"]
# ENTRYPOINT ["java","-jar","/app.jar"]
# ENTRYPOINT java -jar run/application.jar -D exec.mainClass="apodrating.MainKt"
# ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-cp", "application.jar:/config/", "org.springframework.boot.loader.JarLauncher"]
ENTRYPOINT java -jar run/application.jar

# Healthy Status
HEALTHCHECK --interval=5s --retries=12 --timeout=10s \

# CMD wget -O /dev/null https://localhost:8083/about/version || exit 1
CMD curl --fail -k https://localhost:8083/actuator/health || CMD curl --fail -k https://localhost:9000/actuator/health || exit 1