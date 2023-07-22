package com.generic.retailer.config;

import com.generic.retailer.model.Book;
import com.generic.retailer.model.CD;
import com.generic.retailer.model.DVD;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.generic.retailer")
public class Config {

    @Bean("books")
    //    @DependsOn({"fileReader","fileWriter"})
    public Book books() {
        return Book.builder().build();
    }

    @Bean("cds")
    public CD cds() {
        return CD.builder().build();
    }

    @Bean("dvds")
    public DVD dvds() {
        return DVD.builder().build();
    }
}
