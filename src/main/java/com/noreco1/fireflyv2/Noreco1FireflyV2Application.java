package com.noreco1.fireflyv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class Noreco1FireflyV2Application {

    public static void main(String[] args) {
        SpringApplication.run(Noreco1FireflyV2Application.class, args);
    }

}
