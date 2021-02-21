
# 1. Build the micronaut application

## 1.1 Install micronaut 

Install micronaut as described in `https://micronaut.io/download.html`:

This example is running micronaut version 2.2.0.
(test with `mn --version`)

## 1.2 Create project
Create micronaut project with options:

```bash
developer@developer-VirtualBox:~/projects/hello-micronaut$ mn create-app hello-micronaut
| Application created at /home/developer/projects/hello-micronaut/hello-micronaut
developer@developer-VirtualBox:~/projects/hello-micronaut$ 
```
Complete the application by adding the `HelloMicronautController.java` class in directory `hello-micronaut/src/main/java/hello/micronaut` :

```java
package hello.micronaut;

import io.micronaut.context.annotation.Property;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

@Controller("/hellomicronaut")
public class HelloMicronautController {

    @Property(name = "hello.greeting")
    String greeting;

    @Property(name = "hello.location")
    String location;

    @Post(consumes = {MediaType.APPLICATION_JSON}, produces = {MediaType.TEXT_PLAIN})
    public String sayHelloMicronaut(String name) {
        return greeting + " " + name + " from " + location;
    }
}
```

The class uses 2 configuration items that are defined in the `application.yml`  file:

```yaml
micronaut:
  server:
    port: 8090
  application:
    name: helloMicronaut
hello:
  greeting: Hello
  location: development world
```

## 1.3 Run and test project locally

Run:

```bash
./gradlew run 
```

Test:
```bash
curl  -H 'Content-type: application/json' localhost:8090/hellomicronaut -d '{"name": "Luc"}'
```


# 2. Build Docker image

## 2.1 Build GraalVM Docker image

Build GraalVM Docker image:
(NOTE: this can take long time, in my case 2m41s, but may well go up to 10 minutes...)

```bash
developer@developer-VirtualBox:~/projects/hello-micronaut/hello-micronaut$ ./gradlew dockerBuildNative

> Task :compileJava
Note: Writing resource-config.json file to destination: META-INF/native-image/hello.micronaut/hello-micronaut/resource-config.json
Note: Creating bean classes for 1 type elements

> Task :dockerfileNative
Dockerfile written to: /home/developer/projects/hello-micronaut/hello-micronaut/build/docker/DockerfileNative

> Task :dockerBuildNative
Building image using context '/home/developer/projects/hello-micronaut/hello-micronaut'.
Using Dockerfile '/home/developer/projects/hello-micronaut/hello-micronaut/build/docker/DockerfileNative'
Using images 'hello-micronaut'.
Step 1/11 : FROM oracle/graalvm-ce:20.3.0-java11 AS graalvm
 ---> 0b7b0c4bfba5
Step 2/11 : RUN gu install native-image
 ---> Using cache
 ---> 3587e8ce6778
Step 3/11 : WORKDIR /home/app
 ---> Using cache
 ---> b261c52e901c
Step 4/11 : COPY build/layers/libs /home/app/libs
 ---> Using cache
 ---> 9bef7a42e3b4
Step 5/11 : COPY build/layers/resources /home/app/resources
 ---> 28b4af8808ae
Step 6/11 : COPY build/layers/application.jar /home/app/application.jar
 ---> 0b8b092a8f8f
Step 7/11 : RUN native-image -H:Class=hello.micronaut.Application -H:Name=application --no-fallback -cp /home/app/libs/*.jar:/home/app/resources:/home/app/application.jar
 ---> Running in b8f29a4420ce
[application:26]    classlist:   5,079.35 ms,  0.96 GB
[application:26]        (cap):     920.06 ms,  0.96 GB
[application:26]        setup:   2,948.00 ms,  0.96 GB
[application:26]     (clinit):   1,212.49 ms,  4.03 GB
[application:26]   (typeflow):  25,541.89 ms,  4.03 GB
[application:26]    (objects):  30,267.57 ms,  4.03 GB
[application:26]   (features):   3,022.89 ms,  4.03 GB
[application:26]     analysis:  62,317.35 ms,  4.03 GB
[application:26]     universe:   2,645.39 ms,  4.03 GB
[application:26]      (parse):   9,783.03 ms,  4.42 GB
[application:26]     (inline):   7,042.68 ms,  5.19 GB
[application:26]    (compile):  44,759.87 ms,  5.26 GB
[application:26]      compile:  65,952.92 ms,  5.26 GB
[application:26]        image:   9,048.58 ms,  5.18 GB
[application:26]        write:   1,131.09 ms,  5.18 GB
[application:26]      [total]: 149,422.61 ms,  5.18 GB
Removing intermediate container b8f29a4420ce
 ---> 7b0187dda5e4
Step 8/11 : FROM frolvlad/alpine-glibc
 ---> f6858800cf89
Step 9/11 : RUN apk update && apk add libstdc++
 ---> Using cache
 ---> 14f4843a53ac
Step 10/11 : COPY --from=graalvm /home/app/application /app/application
 ---> fc8770574b87
Step 11/11 : ENTRYPOINT ["/app/application"]
 ---> Running in 19dd53aad41f
Removing intermediate container 19dd53aad41f
 ---> 81c10a2a3d4c
Successfully built 81c10a2a3d4c
Successfully tagged hello-micronaut:latest
Created image with ID '81c10a2a3d4c'.

BUILD SUCCESSFUL in 2m 41s
6 actionable tasks: 5 executed, 1 up-to-date
developer@developer-VirtualBox:~/projects/hello-micronaut/hello-micronaut$ 
```

## 2.2 Check for Docker image

Check for the Docker image and note the size of the image:

```bash
docker images | grep -i hello-micronaut
```

## 2.3 Run Docker container

*Important note*
The current micronaut gradle DockerBuild does NOT include the default configuration files. So, when running the built container, the application properties have to be added. Below, the `application.yml` file is mounted and the environment variable ` MICRONAUT_CONFIG_FILES` points to it:

`docker run -d -v "$(pwd)"/src/main/resources/application.yml:/app/application.yml --env MICRONAUT_CONFIG_FILES=/app/application.yml -p8020:8090 --name my-hello-micronaut hello-micronaut` 

And test it:

```bash
curl  -H 'Content-type: application/json' localhost:8020/hellomicronaut -d '{"name": "Luc"}'
```

Stop the container:

```bash
docker stop my-hello-micronaut
```


## 2.4 Upload the Docker image to Docker Hub

Tag the image and upload to Docker Hub:

```bash
docker login --username=lgorissen

docker images | grep  hello-micronaut

docker tag hello-micronaut:latest lgorissen/hello-micronaut:1.0.0

docker push lgorissen/hello-micronaut:1.0.0 
```

Your Docker account and image name will be different...


## 2.5 Remove local Docker containers and images

Remove all local Docker artefacts:

```bash
docker rm my-hello-micronaut 

docker rmi hello-micronaut:latest 

docker rmi lgorissen/hello-micronaut:1.0.0 
```


# 3. Helm chart

Finally, a Helm chart has to be made that has configuration options.

# 3.1 Location

The Helm chart is added under the `hello-micronaut/hello-micronaut/k8s` directory.


# 3.2 Running the Helm chart

Run the Helm chart with the below command in the right directory, in my case `~/projects/hello-micronaut/hello-micronaut/k8s/charts/hello-micronaut` 

```bash
helm install my-hello-micronaut --values values.yml .
```

Verify the Pod is running with `kubectl get pod`.

# 3.3 Testing the Helm chart

In order to test the Helm chart, first port-forward the hello-micronaut service:
```bash
kubectl port-forward service/hello-micronaut 8020:8020
```

And then test with:
```bash
 curl  -H 'Content-type: application/json' localhost:8020/hellomicronaut -d '{"name": "Luc"}'
 ```