package com.kafka.controller;

import com.dhcc.aml.common.core.util.AmlIdWorker;
import com.dhcc.aml.modules.kafka.entity.CExLog;
import com.dhcc.aml.modules.kafka.service.CExLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

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


    @PostMapping(value = "/save")
    public void save(@RequestBody CExLog cxLog) {
        CExLog exLog = CExLog.builder().id(AmlIdWorker.get32UUID()).exMsg(cxLog.getExMsg()).exError(cxLog.getExError()).exTime(new Date()).build();
        cExLogService.save(exLog);

    }


}
