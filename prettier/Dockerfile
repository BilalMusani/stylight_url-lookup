FROM openjdk:11
ADD . /usr/src/prettier
WORKDIR /usr/src/prettier
RUN ./gradlew bootJar
RUN ls
COPY build/libs/*.jar ./app.jar
ENTRYPOINT ["java","-jar","build/libs/prettier-0.0.1-SNAPSHOT.jar"]