FROM openjdk:8-jre-alpine
WORKDIR /app
COPY  target/*.jar app.jar
RUN mkdir -p /app/upload && chmod -R 777 /app/upload
EXPOSE 8080
ENV DB_IP=127.0.0.1
ENV DB_PORT=3306
ENV DB_USER=root
VOLUME /app/upload
ENTRYPOINT ["java", "-jar", "app.jar"]