package com.kafka.config;

import com.alibaba.fastjson.JSONObject;
import com.kafka.entity.CExLog;
import com.kafka.entity.CFilterTable;
import com.kafka.entity.CKStatus;
import com.kafka.service.CExLogService;
import com.kafka.service.CFilterTableService;
import com.kafka.service.CKStatusService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author zhaomingxing
 * 描述-消费配置
 * date 2021/9/17
 */

@Slf4j
@Component
@EnableKafka
public class KafkaConfig {

    @Autowired
    private CExLogService cExLogService;

    @Autowired
    private CFilterTableService cFilterTableService;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private ConsumerFactory consumerFactory;

    @Autowired
    private CKStatusService cKStatusService;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;


    /**
     * 异常处理
     *
     * @return
     */

    @Bean
    public ConsumerAwareListenerErrorHandler consumerAwareErrorHandler() {
        return (message, exception, consumer) -> {
            try {
                kafkaListenerEndpointRegistry.getListenerContainer("sqlConsumer").stop();
                log.warn("------------kafka暂停消费--------------");
                CKStatus cKStatus = CKStatus.builder().status("1").updateTime(new Date()).build();
                cKStatusService.update(cKStatus, null);
                log.error("消费异常：", exception);
                JSONObject jsonObject = JSONObject.parseObject(message.getPayload().toString());
                String table = StringUtils.replace(jsonObject.getString("table"), ".", "_");
                CExLog exLog = CExLog.builder().id(UUID.randomUUID().toString()).tableName(table).exMsg(message.getPayload().toString()).exError(exception.toString()).exTime(new Date()).build();
                cExLogService.save(exLog);
            } catch (Exception e) {
                log.error("save(exLog)异常：", e);
            } finally {
//                consumer.commitSync();

            }
            return message;
        };
    }

    /**
     * 消息过滤
     *
     * @return
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory concurrentKafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> concurrentKafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(concurrentKafkaListenerContainerFactory, consumerFactory);
        concurrentKafkaListenerContainerFactory.setAckDiscarded(true);
        concurrentKafkaListenerContainerFactory.getContainerProperties().setAckMode(kafkaProperties.getListener().getAckMode());
        List<CFilterTable> list = cFilterTableService.list(null);
        if (list != null && list.size() > 0) {
            concurrentKafkaListenerContainerFactory.setRecordFilterStrategy(consumerRecord -> {
                JSONObject jsonObject = JSONObject.parseObject((String) consumerRecord.value());
                String table = StringUtils.replace(jsonObject.getString("table"), ".", "_");
                return (list.stream().noneMatch(x -> {
                    return x.getOwnerTableName().equals(table);
                }));
            });
        } else {
            concurrentKafkaListenerContainerFactory.setRecordFilterStrategy(consumerRecord -> {
                return true;
            });
        }
        return concurrentKafkaListenerContainerFactory;
    }

    @Scheduled(cron = "${check.kafka.cron}")
    public void startKafka() {
        if (!kafkaListenerEndpointRegistry.getListenerContainer("sqlConsumer").isRunning()) {
            CKStatus ckStatus = cKStatusService.list(null).get(0);
            if (ckStatus.getStatus().equals("0")) {
                kafkaListenerEndpointRegistry.getListenerContainer("sqlConsumer").start();
                log.warn("------------kafka启动消费--------------");
            }
        }
    }

}
