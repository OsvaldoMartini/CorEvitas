package com.generic.retailer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.generic.retailer")
public class Application implements CommandLineRunner {

    public static void main(String[] args) {

        new SpringApplicationBuilder()
                .main(Application.class)
                .sources(Application.class)
                .profiles("server")
                .run(args);
    }

    @Bean
    @Qualifier("books")
    public Book books() {
        return Book.builder().build();
    }
    ;

    @Bean
    @Qualifier("cds")
    public CD cds() {
        return CD.builder().build();
    }
    ;

    @Bean
    @Qualifier("dvds")
    public DVD dvds() {
        return DVD.builder().build();
    }
    ;

    @Override
    public void run(String... args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        try (Cli cli = Cli.create(reader, writer)) {
            cli.run();
        } catch (Exception e) {
            System.exit(1);
        }
    }
}
