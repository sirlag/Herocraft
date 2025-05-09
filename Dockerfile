FROM eclipse-temurin:21-jre
EXPOSE 8080:8080
RUN mkdir /app
RUN mkdir /app/conf
RUN bash curl -s https://api.github.com/repos/sirlag/Herocraft/releases/latest | grep "herocraft.*jar" | cut -d : -f 2,3 | tr -d \" | wget -qi -
RUN mv *.jar /app/herocrafter.jar

COPY src/main/resources/*.conf /app/conf/
ENTRYPOINT ["java", "-jar", "/app/herocrafter.jar", "-config=/app/conf/application.conf", "-config=/app/conf/application-prod.conf"]
