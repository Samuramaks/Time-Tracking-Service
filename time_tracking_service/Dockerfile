FROM eclipse-temurin:21 AS builder
WORKDIR /opt/app
COPY .mvn/ .mvn
COPY  mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY ./src ./src
RUN ./mvnw clean install
CMD ["java", "-jar", "target/time_tracking_service-*.jar"]

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=builder /opt/app/target/*.jar app.jar
EXPOSE 8080:8080
ENTRYPOINT ["java", "-jar", "app.jar"]