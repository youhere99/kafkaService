package com.kafka.controller;

import com.kafka.entity.CExLog;
import com.kafka.service.CExLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value(value = "${kafka.topics}")
    private String topics;


    @Value(value = "${spring.datasource.username}")
    private String username;


    @GetMapping(value = "/send")
    public void send(String msg) {
        kafkaTemplate.send(topics, msg);

    }

    @GetMapping(value = "/metaData")
    public void metaData(String tableName) throws SQLException {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet("select * from " + tableName + " where 1<>1");
        SqlRowSetMetaData metaData = rowSet.getMetaData();
        List<String> column = new ArrayList<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            column.add(metaData.getColumnName(i));
        }

        Connection connection = jdbcTemplate.getDataSource().getConnection();
        DatabaseMetaData metaData2 = connection.getMetaData();
        String[] types = {"TABLE"};
        ResultSet rs = metaData2.getTables(null, username, "%", types);
        Map map = new HashMap<String, List<String>>();
        while (rs.next()) {
            ResultSet rs2 = metaData2.getColumns(null, username, rs.getString("TABLE_NAME"), null);
            List<String> column2 = new ArrayList<>();
            while (rs2.next()) {
                column2.add(rs2.getString("COLUMN_NAME"));
            }
            map.put(rs.getString("TABLE_NAME"), column2);
        }
        System.out.println(map);
    }
}
