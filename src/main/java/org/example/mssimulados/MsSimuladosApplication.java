package org.example.mssimulados;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MsSimuladosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsSimuladosApplication.class, args);
    }
}