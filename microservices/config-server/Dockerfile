FROM openjdk:11
ENV JAVA_OPTS="-Xms512m -Xmx512m"
ADD target/config-server-0.0.1-SNAPSHOT.jar config-server
ENTRYPOINT ["java", "-jar", "config-server"]

