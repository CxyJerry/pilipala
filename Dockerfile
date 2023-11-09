FROM linuxserver/ffmpeg
ARG VERSION="0.0.1-SNAPSHOT"

WORKDIR /

RUN apt-get update && apt-get install -y openjdk-17-jdk

ENV JAVA_HOME /usr/lib/jvm/java-17-openjdk-amd64

CMD ["java","-version"]

COPY target/pilipala-$VERSION.jar /pilipala.jar

ENTRYPOINT ["java","-jar","pilipala.jar"]