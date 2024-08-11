package org.jolly.nautica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NauticaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NauticaApplication.class, args);
    }

}
