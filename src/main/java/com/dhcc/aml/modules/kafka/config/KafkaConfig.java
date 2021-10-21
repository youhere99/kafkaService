package com.dhcc.aml.modules.kafka.config;

import com.alibaba.fastjson.JSONObject;
import com.dhcc.aml.common.core.util.AmlIdWorker;
import com.dhcc.aml.modules.kafka.entity.CExLog;
import com.dhcc.aml.modules.kafka.entity.CFilterTable;
import com.dhcc.aml.modules.kafka.service.CExLogService;
import com.dhcc.aml.modules.kafka.service.CFilterTableService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

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


    /**
     * 异常处理
     *
     * @return
     */

    @Bean
    public ConsumerAwareListenerErrorHandler consumerAwareErrorHandler() {
        return (message, exception, consumer) -> {
            try{
                log.info("消费异常：", exception);
                CExLog exLog = CExLog.builder().id(AmlIdWorker.get32UUID()).exMsg(message.getPayload().toString()).exError(exception.toString()).exTime(new Date()).build();
                cExLogService.save(exLog);
            }catch(Exception e){
                log.error("save(exLog)异常：",e);
            }finally {
                consumer.commitSync();
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
}
