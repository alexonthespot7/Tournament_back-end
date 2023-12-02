FROM maven:3.8.7-openjdk-18-slim AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim
ENV SPRING_CONFIG_NAME=application
COPY --from=build /target/cool-reads-0.0.1-SNAPSHOT.jar coolreads.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","coolreads.jar"]