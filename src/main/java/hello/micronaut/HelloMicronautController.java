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