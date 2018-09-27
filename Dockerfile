FROM openjdk:8-jdk-alpine
VOLUME /tmp
RUN mkdir /work
COPY . /work
WORKDIR /work
RUN /work/gradlew build
RUN mv /work/build/libs/*.jar /work/app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-DSOLR_HOST=http://solr:8983/solr","-DZIP_SIZE=500","-DDROPBOX_ACCESS_TOKEN=","-jar","/work/app.jar"]