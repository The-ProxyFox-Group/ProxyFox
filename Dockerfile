FROM openjdk:17-jdk-slim

COPY modules/bot/build/libs/proxyfox-*.jar /usr/local/lib/ProxyFox.jar

RUN mkdir /bot

WORKDIR /bot

ENTRYPOINT ["java", "-Xms2G", "-Xmx2G", "-jar", "/usr/local/lib/ProxyFox.jar"]
