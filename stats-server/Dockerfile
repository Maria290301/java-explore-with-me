FROM eclipse-temurin:21-jdk-jammy
VOLUME /tmp
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8081"
COPY app.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
