FROM linuxserver/ffmpeg
ARG VERSION="0.0.1-SNAPSHOT"

WORKDIR /app

RUN apt-get update && apt-get install -y openjdk-17-jdk

ENV JAVA_HOME /usr/lib/jvm/java-17-openjdk-amd64

CMD ["java","-version"]

COPY target/pilipala-$VERSION.jar /app/pilipala.jar

ENTRYPOINT ["java","-jar","/app/pilipala.jar"]