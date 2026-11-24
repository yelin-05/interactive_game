FROM maven:3.9.9-eclipse-temurin-11 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package

FROM tomcat:9.0-jdk11-temurin
RUN rm -rf ${CATALINA_HOME}/webapps/*
COPY --from=build /build/target/moshi.war ${CATALINA_HOME}/webapps/ROOT.war
COPY --chmod=0755 deploy/run-tomcat.sh /usr/local/bin/run-tomcat.sh
EXPOSE 8080
CMD ["run-tomcat.sh"]
