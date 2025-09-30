package org.example;

;
import org.example.cli.VcsCliGraph;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VcsApplication {

    public static void main(String[] args){
        SpringApplication.run(VcsApplication.class, args);
    }

    @Bean
    CommandLineRunner runCLI(VcsCliGraph cli){
        return args -> cli.startCLI();
    }
}
