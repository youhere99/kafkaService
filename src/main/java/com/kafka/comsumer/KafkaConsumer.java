package com.kafka.comsumer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.kafka.entity.CPbRecord;
import com.kafka.service.CPbRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.*;

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
     */
    @KafkaListener(id="sqlConsumer",topics = {"#{'${kafka.topics}'.split(',')}"}, containerFactory = "concurrentKafkaListenerContainerFactory", errorHandler = "consumerAwareErrorHandler")
    public void consumerMsg(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.debug(String.format("主题：%s，分区：%d，偏移量：%d，key：%s，value：%s", record.topic(), record.partition(), record.offset(), record.key(), record.value()));
        JSONObject jsonObject = JSONObject.parseObject(record.value());
        String sql = "";
        String table = StringUtils.replace(jsonObject.getString("table"), ".", "_");
        JSONArray jsonArray = jsonObject.getJSONArray("primary_keys");
        List<String> primaryKeys = null;
        if (jsonArray != null) {
            primaryKeys = jsonArray.toJavaList(String.class);
        }
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
        log.info("sql--{}", sql);
        jdbcTemplate.execute(sql);
        opTs = StringUtils.substringBefore(opTs, " ");
        String opTsPre = redisTemplate.opsForValue().get("opTs");
        if (opTsPre == null) {
            redisTemplate.opsForValue().set("opTs", opTs);
        } else {
            if (!StringUtils.equals(opTsPre, opTs)) {
                CPbRecord cPbRecord = CPbRecord.builder().id(UUID.randomUUID().toString()).pbTime(opTsPre).firstDataMsg(record.value()).crateTime(new Date()).status(CPbRecord.Status.RUN.getCode()).build();
                cPbRecordService.save(cPbRecord);
                redisTemplate.opsForValue().set("opTs", opTs);
            }
        }
        // 手动提交offset
        ack.acknowledge();

    }

    private StringBuffer dmlD(JSONObject jsonObject, String table, List<String> primaryKeys, String opTs, String opType) {
        Map<String, List<Object>> before = toMap(jsonObject.getJSONObject("before"),table);
        List<Object> columnWhere = before.get("column");
        List<Object> valuesWhere = before.get("values");
        StringBuffer sqlD = new StringBuffer("DELETE FROM  " + table);
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
                sqlDWhere.append(columnWhere.get(i) + " " + (valuesWhere.get(i) != null ? "='" + valuesWhere.get(i) + "'" : "is null"));
                if (i < columnWhere.size() - 1) {
                    sqlDWhere.append(" and ");
                }
            }
        }
        sqlD.append(sqlDWhere);
        return sqlD;
    }

    private StringBuffer dmlU(JSONObject jsonObject, String table, List<String> primaryKeys, String opTs, String opType) {
        Map<String, List<Object>> after = toMap(jsonObject.getJSONObject("after"),table);
        List<Object> column = after.get("column");
        List<Object> values = after.get("values");
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
            Map<String, List<Object>> before = toMap(jsonObject.getJSONObject("before"),table);
            List<Object> columnWhere = before.get("column");
            List<Object> valuesWhere = before.get("values");
            for (int i = 0; i < columnWhere.size(); i++) {
                sqlUWhere.append(columnWhere.get(i) + " " + (valuesWhere.get(i) != null ? "='" + valuesWhere.get(i) + "'" : "is null"));
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
            sqlU.append(column.get(i) + "=" + (values.get(i) != null ? "'" + values.get(i) + "'" : "null"));
            if (i < column.size() - 1) {
                sqlU.append(" , ");
            }
        }
        sqlU.append(sqlUWhere);
        return sqlU;
    }

    private StringBuffer dmlI(JSONObject jsonObject, String table, String opTs, String opType) {
        Map<String, List<Object>> after = toMap(jsonObject.getJSONObject("after"),table);
        List<Object> column = after.get("column");
        column.add("OP_TS");
        column.add("OP_TYPE");
        List<Object> values = after.get("values");
        values.add(opTs);
        values.add(opType);
        StringBuffer sqlI = new StringBuffer("insert into " + table);
        List<String> values2 = new ArrayList<>();
        for (Object s : values) {
            values2.add(s != null ? "'" + s + "'" : "null");
        }
        sqlI.append(" (" + StringUtils.join(column, ",") + ")");
        sqlI.append(" values (" + StringUtils.join(values2, ",") + ")");
        return sqlI;
    }


    protected Map<String, List<Object>> toMap(JSONObject jSONObject,String table) {
        Map<String, Object> map = JSONObject.parseObject(JSONObject.toJSONString(jSONObject, SerializerFeature.WRITE_MAP_NULL_FEATURES, SerializerFeature.WriteNullStringAsEmpty), Map.class, Feature.InitStringFieldAsEmpty, Feature.CustomMapDeserializer);
        map= filterColumn(map, table);
        List<Object> column = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> next = iterator.next();
            column.add(next.getKey());
            values.add(next.getValue());
        }
        Map<String, List<Object>> map2 = new HashMap<>();
        map2.put("column", column);
        map2.put("values", values);
        return map2;
    }

    /**
     * 匹配数据中的字段
     *
     * @return
     */
    protected Map<String, Object> filterColumn(Map<String, Object> map, String table) {
        Map<String, Object> oldMap= new HashMap<>(map);
        int before=oldMap.size();
        List<String> list = JSONObject.parseArray(redisTemplate.opsForValue().get(table), String.class);
        Iterator<String> iterator = map.keySet().iterator();
        List notExistCloumn=new ArrayList();
        while (iterator.hasNext()){
            String next = iterator.next();
            if (!list.contains(next)) {
                iterator.remove();
                notExistCloumn.add(next);
            }else{
                map.put(next,StringEscapeUtils.escapeSql(ConvertUtils.convert(oldMap.get(next))));
            }
        }
        int after=map.size();
        if(before!=after){
            log.warn("kafka消息表字段数据-{}",JSONObject.toJSONString(oldMap));
            log.warn("数据库表字段-{}",JSONObject.toJSONString(list));
            log.warn("改字段不在表中-{}",JSONObject.toJSONString(notExistCloumn));
        }
        return map;
    }



}
