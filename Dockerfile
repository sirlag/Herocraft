FROM gradle:8.4-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM openjdk:21
EXPOSE 8080:8080
RUN mkdir /app
RUN mkdir /app/conf
COPY --from=build /home/gradle/src/build/libs/*.jar /app/herocrafter.jar
COPY --from=build /home/gradle/src/src/main/resources/*.conf /app/conf/
ENTRYPOINT ["java", "-jar", "/app/herocrafter.jar", "-config=/app/conf/application.conf", "-config=/app/conf/application-prod.conf"]
