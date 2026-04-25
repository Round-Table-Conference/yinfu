FROM eclipse-temurin:11-jdk-alpine AS build
WORKDIR /app
COPY yinfu-backend/pom.xml .
COPY yinfu-backend/src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY --from=build /app/target/yinfu-backend-1.0.0.jar.original app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]