package com.dhcc.aml.modules.kafka.comsumer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dhcc.aml.common.core.util.AmlIdWorker;
import com.dhcc.aml.modules.kafka.entity.CPbRecord;
import com.dhcc.aml.modules.kafka.service.CPbRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhaomingxing
 * 描述-消费
 * date 2021/9/17
 */
@Component
@Slf4j
public class KafkaConsumer {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CPbRecordService cPbRecordService;


    /**
     * 消费监听
     *
     */
    @KafkaListener(topics = {"#{'${kafka.topics}'.split(',')}"}, containerFactory = "concurrentKafkaListenerContainerFactory", errorHandler = "consumerAwareErrorHandler")
    public void consumerMsg(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.debug(String.format("主题：%s，分区：%d，偏移量：%d，key：%s，value：%s", record.topic(), record.partition(), record.offset(), record.key(), record.value()));
        JSONArray jsonArray = JSONArray.parseArray(record.value());
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        String sql = "";
        String table = StringUtils.replace(jsonObject.getString("table"), ".", "_");
        List<String> primaryKeys = jsonObject.getJSONArray("primary_keys").toJavaList(String.class);
        String opTs = jsonObject.getString("op_ts");
        String opType = jsonObject.getString("op_type");
        switch (opType) {
            case "I":
                //  insert into 表名(列1，列2，......)values(值1，值2，......);
                StringBuffer sqlI = dmlI(jsonObject, table, opTs, opType);
                sql = sqlI.toString();
                break;

            case "U":
                //UPDATE table SET column1 = expression1, column2 = expression2, ... column_n = expression_n WHERE conditions;
                StringBuffer sqlU = dmlU(jsonObject, table, primaryKeys, opTs, opType);
                sql = sqlU.toString();
                break;

            case "D":
                //delete from 表名 where 条件
                StringBuffer sqlD = dmlD(jsonObject, table, primaryKeys, opTs, opType);
                sql = sqlD.toString();
                break;
        }
        jdbcTemplate.execute(sql);
        opTs = StringUtils.substringBefore(opTs, " ");
        String opTsPre = redisTemplate.opsForValue().get("opTs");
        if (opTsPre == null) {
            redisTemplate.opsForValue().set("opTs", opTs);
        } else {
            if (!StringUtils.equals(opTsPre, opTs)) {
                CPbRecord cPbRecord = CPbRecord.builder().id(AmlIdWorker.get32UUID()).pbTime(opTsPre).firstDataMsg(record.value()).crateTime(new Date()).status(CPbRecord.Status.RUN.getCode()).build();
                cPbRecordService.save(cPbRecord);
                redisTemplate.opsForValue().set("opTs", opTs);
            }
        }
        // 手动提交offset
        ack.acknowledge();

    }

    private StringBuffer dmlD(JSONObject jsonObject, String table, List<String> primaryKeys, String opTs, String opType) {
        Map<String, List<String>> before = toMap(jsonObject.getJSONObject("before"));
        List<String> columnWhere = before.get("column");
        List<String> valuesWhere = before.get("values");
        StringBuffer sqlD = new StringBuffer("DELETE FROM  " + table );
        StringBuffer sqlDWhere = new StringBuffer(" WHERE ");
        if (!primaryKeys.isEmpty()) {
            for (int i = 0; i < primaryKeys.size(); i++) {
                int j = columnWhere.indexOf(primaryKeys.get(i));
                sqlDWhere.append(columnWhere.get(j) + "='" + valuesWhere.get(j) + "'");
                if (i < primaryKeys.size() - 1) {
                    sqlDWhere.append(" and ");
                }
            }
        } else {
            for (int i = 0; i < columnWhere.size(); i++) {
                sqlDWhere.append(columnWhere.get(i) + "='" + valuesWhere.get(i) + "'");
                if (i < columnWhere.size() - 1) {
                    sqlDWhere.append(" and ");
                }
            }
        }
        sqlD.append(sqlDWhere);
        return sqlD;
    }

    private StringBuffer dmlU(JSONObject jsonObject, String table, List<String> primaryKeys, String opTs, String opType) {
        Map<String, List<String>> after= toMap(jsonObject.getJSONObject("after"));
        List<String>   column = after.get("column");
        List<String>  values = after.get("values");
        StringBuffer sqlU = new StringBuffer("UPDATE  " + table + " SET ");
        StringBuffer sqlUWhere = new StringBuffer(" WHERE ");
        if (!primaryKeys.isEmpty()) {
            for (int i = 0; i < primaryKeys.size(); i++) {
                int j = column.indexOf(primaryKeys.get(i));
                sqlUWhere.append(column.get(j) + "='" + values.get(j) + "'");
                if (i < primaryKeys.size() - 1) {
                    sqlUWhere.append(" and ");
                }
                column.remove(j);
                values.remove(j);
            }
        } else {
            Map<String, List<String>> before = toMap(jsonObject.getJSONObject("before"));
            List<String> columnWhere = before.get("column");
            List<String> valuesWhere = before.get("values");
            for (int i = 0; i < columnWhere.size(); i++) {
                sqlUWhere.append(columnWhere.get(i) + "='" + valuesWhere.get(i) + "'");
                if (i < columnWhere.size() - 1) {
                    sqlUWhere.append(" and ");
                }
            }
        }
        column.add("OP_TS");
        column.add("OP_TYPE");
        values.add(opTs);
        values.add(opType);
        for (int i = 0; i < column.size(); i++) {
            sqlU.append(column.get(i) + "='" + values.get(i) + "'");
            if (i < column.size() - 1) {
                sqlU.append(" , ");
            }
        }
        sqlU.append(sqlUWhere);
        return sqlU;
    }

    private StringBuffer dmlI(JSONObject jsonObject, String table, String opTs, String opType) {
        Map<String, List<String>> after = toMap(jsonObject.getJSONObject("after"));
        List<String> column = after.get("column");
        column.add("OP_TS");
        column.add("OP_TYPE");
        List<String> values = after.get("values");
        values.add(opTs);
        values.add(opType);
        StringBuffer sqlI = new StringBuffer("insert into " + table);
        values = values.stream().map(x -> {
            return "'" + x + "'";
        }).collect(Collectors.toList());
        sqlI.append(" (" + StringUtils.join(column, ",") + ")");
        sqlI.append(" values (" + StringUtils.join(values, ",") + ")");
        return sqlI;
    }


    protected Map<String, List<String>> toMap(JSONObject jSONObject) {

        Map<String, String> map = JSONObject.parseObject(jSONObject.toJSONString(), Map.class);
        List<String> column = new ArrayList<>();
        List<String> values = new ArrayList<>();
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            column.add(next.getKey());
            values.add(next.getValue());
        }
        Map<String, List<String>> map2 = new HashMap<>();
        map2.put("column", column);
        map2.put("values", values);
        return map2;
    }

}