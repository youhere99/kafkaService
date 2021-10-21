package com.kafka.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zhaomingxing
 * 描述-
 * date 2021/9/28
 */
@ApiModel(value = "可消费的表")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "C_FILTER_TABLE")
public class CFilterTable implements Serializable {

    @TableField(value = "OWNER_TABLE_NAME")
    @ApiModelProperty(value = "属主_表名")
    private String ownerTableName;


}