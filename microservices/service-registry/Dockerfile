FROM openjdk:11
ENV JAVA_OPTS="-Xms512m -Xmx512m"
ADD target/service-registry-0.0.1-SNAPSHOT.jar service-registry
ENTRYPOINT ["java", "-jar", "service-registry"]

