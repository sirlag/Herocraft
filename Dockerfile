FROM eclipse-temurin:21-jre
EXPOSE 8080:8080
RUN mkdir /app
RUN mkdir /app/conf
RUN apt-get update
RUN apt-get install jq -y
WORKDIR /app
RUN wget $(wget -q -O - https://api.github.com/repos/sirlag/Herocraft/releases/latest  |  jq -r '.assets[] | select(.name | contains ("jar")) | .browser_download_url')
RUN mv *.jar /herocrafter.jar

COPY src/main/resources/*.conf /app/conf/
ENTRYPOINT ["java", "-jar", "/app/herocrafter.jar", "-config=/app/conf/application.conf", "-config=/app/conf/application-prod.conf"]
