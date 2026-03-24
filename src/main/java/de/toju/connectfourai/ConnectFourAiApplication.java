package de.toju.connectfourai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ConnectFourAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConnectFourAiApplication.class, args);
    }

}
