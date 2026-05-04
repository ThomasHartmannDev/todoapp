FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN chmod +x mvnw && ./mvnw package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN addgroup --system todoapp && adduser --system --ingroup todoapp todoapp

COPY --from=build /workspace/target/todoapp-0.0.1-SNAPSHOT.jar app.jar

USER todoapp
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
