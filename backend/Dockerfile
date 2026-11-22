FROM maven:3.9.11-eclipse-temurin-21 AS build
ARG SERVICE_DIR
WORKDIR /src
COPY ${SERVICE_DIR}/pom.xml ./pom.xml
COPY ${SERVICE_DIR}/src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-noble
WORKDIR /app
RUN sed -i 's|http://|https://|g' /etc/apt/sources.list.d/ubuntu.sources \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -g 10001 appgroup \
    && useradd -u 10001 -g appgroup -m -s /sbin/nologin appuser
COPY --from=build /src/target/*.jar /app/app.jar
USER 10001:10001
ENTRYPOINT ["java","-jar","/app/app.jar"]
