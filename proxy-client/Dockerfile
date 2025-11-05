
FROM eclipse-temurin:11-jre
LABEL maintainer="selimhorri.github.io"
RUN mkdir -p /home/app
WORKDIR /home/app
ARG PROJECT_VERSION=0.1.0
ENV SPRING_PROFILES_ACTIVE=dev
ADD proxy-client/target/proxy-client-v${PROJECT_VERSION}.jar proxy-client.jar
EXPOSE 8900
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "proxy-client.jar"]
