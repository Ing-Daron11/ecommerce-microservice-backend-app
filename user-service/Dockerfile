
FROM openjdk:11
LABEL maintainer="selimhorri.github.io"
RUN mkdir -p /home/app
WORKDIR /home/app
ARG PROJECT_VERSION=0.1.0
ENV SPRING_PROFILES_ACTIVE=dev
ADD user-service/target/user-service-v${PROJECT_VERSION}.jar user-service.jar
EXPOSE 8700
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "user-service.jar"]