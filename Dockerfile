FROM openjdk:8-jdk-alpine
VOLUME /tmp
RUN mkdir /work
COPY . /work
WORKDIR /work
RUN /work/gradlew build
RUN mv /work/build/libs/*.jar /work/api.jar
ENTRYPOINT ["java","-DDROPBOX_ACCESS_TOKEN=D5wbxvYqXb0AAAAAAAALHR0uX3kDhF3i7bin1342ngi_UQfLM830EgcZ3y8FsJxv","-DZIP_SIZE=500","-DSOLR_HOST=http://localhost:8983/solr/","-Djava.security.egd=file:/dev/./urandom","-jar","/work/api.jar"]