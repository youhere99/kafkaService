package com.dhcc.aml.modules.kafka;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EnableEurekaClient
@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@EnableCircuitBreaker
@EnableFeignClients({"com.dhcc.aml"})
@EnableSwagger2Doc
public class KafkaServiceBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(KafkaServiceBootstrap.class, args);
    }
}