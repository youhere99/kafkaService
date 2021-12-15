package com.kafka;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;


//@EnableEurekaClient
@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
//@EnableCircuitBreaker
//@EnableFeignClients({"com.dhcc.aml"})
@EnableSwagger2Doc
@EnableScheduling
public class KafkaServiceBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(KafkaServiceBootstrap.class, args);
    }
}
