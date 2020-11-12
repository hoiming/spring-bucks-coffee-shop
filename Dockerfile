FROM java:8
EXPOSE 8443
ARG JAR_FILE
ADD target/${JAR_FILE}.jar /springbucks.jar
ENTRYPOINT ["java", "-jar", "/springbucks.jar"]