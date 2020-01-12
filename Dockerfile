FROM openjdk:11-jre-slim

ENV JAVA_ENV=PRODUCTION
ENV KUMULUZEE_ENV_NAME=prod
ENV KUMULUZEE_ENV_PROD=true
ENV KUMULUZEE_DATASOURCES0_CONNECTION-URL=jdbc:postgresql://localhost:5432/orders-service
ENV KUMULUZEE_DATASOURCES0_USERNAME=not_set
ENV KUMULUZEE_DATASOURCES0_PASSWORD=not_set

RUN mkdir /app
WORKDIR /app

ADD ./api/v1/target/orders-service.jar /app

EXPOSE 8080

CMD ["java", "-jar", "orders-service.jar"]
