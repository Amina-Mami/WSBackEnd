package tn.gestondechetsws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"tn.gestondechetsws", "controller","services"})
public class GestonDechetsWsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestonDechetsWsApplication.class, args);
    }

}
