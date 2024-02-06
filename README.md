# Boilerplate for Quarkus REST API with Panache

This is an example using Quarkus.

## Tech Stacks

- Java 17
- Maven
- Quarkus
- Smallrye Mutiny
- Panache

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell
./mvnw compile quarkus:dev
```
> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging

The application can be packaged using:
```shell
./mvnw package
```
- It produces the `target/quarkus-app/quarkus-run.jar`.
- Dependencies are copied into the `target/quarkus-app/lib` directory.

## Packaging a Uber-jar

To create a jar file containing dependency files:
```shell
./mvnw package -Dquarkus.package.type=uber-jar
```
It produces the `target/${name}-${version}-runner.jar`.

## Packaging a Native executable

If you installed GraalVM already, to create a native executable jar file:
```shell
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:
```shell
./mvnw package -Pnative -Dquarkus.native.container-build=true
```
It produces the `./target/${name}-${version}-runner` file.

## Packaging a Native docker image and Execute

```shell
docker build -f src/main/docker/Dockerfile.native -t my-quarkus-app .
docker run -i --rm -p 8080:8080 my-quarkus-app
```
