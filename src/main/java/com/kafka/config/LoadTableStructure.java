package com.kafka.config;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 加载表结构
 */
@Component
public class LoadTableStructure implements ApplicationRunner {

    @Value(value = "${spring.datasource.username}")
    private String username;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void run(ApplicationArguments arg) throws SQLException {
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getTables(null, username.toUpperCase(), "%", new String[]{"TABLE"});
        while (rs.next()) {
            ResultSet rs2 = metaData.getColumns(null, username.toUpperCase(), rs.getString("TABLE_NAME"), null);
            List<String> column = new ArrayList<>();
            while (rs2.next()) {
                column.add(rs2.getString("COLUMN_NAME"));
            }
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(rs.getString("TABLE_NAME"), JSONObject.toJSONString(column));
        }
    }

}