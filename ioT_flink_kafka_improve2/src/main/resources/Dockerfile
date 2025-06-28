FROM openjdk:8-jre
WORKDIR /app
COPY target/producer-agent-1.0.0.jar /app/agent.jar
ENTRYPOINT ["java","-Xms1g","-Xmx4g","-jar","/app/agent.jar"]
