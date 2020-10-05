FROM openjdk:11-oraclelinux7

ARG JAR_FILE

COPY target/${JAR_FILE} /app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
