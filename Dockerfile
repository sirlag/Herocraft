FROM gradle:8.4-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar

FROM openjdk:21
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/herocrafter.jar
ENTRYPOINT ["java", "-jar", "/app/herocrafter.jar"]
