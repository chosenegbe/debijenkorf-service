FROM openjdk:11
ENV JAVA_OPTS="-Xms512m -Xmx512m"
ADD target/cloud-gateway-0.0.1-SNAPSHOT.jar cloud-gateway
ENTRYPOINT ["java", "-jar", "cloud-gateway"]

