package kg.example.levantee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LevanteeApplication {

    public static void main(String[] args) {
        SpringApplication.run(LevanteeApplication.class, args);
    }

}
