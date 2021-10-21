package com.kafka.controller;

import com.kafka.service.CExLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhaomingxing
 * 描述-
 * date 2021/9/18
 */
@RestController
@RequestMapping(value = "/test")
public class TestController {

    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private CExLogService cExLogService;

    @Value(value = "${kafka.topics}")
    private String topics;

    @GetMapping(value = "/send")
    public void send(String msg) {
        kafkaTemplate.send(topics, msg);

    }


}
