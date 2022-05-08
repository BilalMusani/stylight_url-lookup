FROM openjdk:11 AS build
ADD . /usr/src/prettier
WORKDIR /usr/src/prettier
RUN ./gradlew bootJar

FROM openjdk:11 AS package
WORKDIR /usr/src/prettier
RUN ls
COPY --from=build /usr/src/prettier/build/libs/*.jar ./app.jar
RUN ls
ENTRYPOINT ["java","-jar","app.jar"]