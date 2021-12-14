package com.kafka.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhaomingxing
 * 描述-
 * date 2021/9/23
 */
@ApiModel(value = "异常消息表")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "C_EX_LOG")
public class CExLog implements Serializable {

    @TableId(value = "ID")
    @ApiModelProperty(value = "主键")
    private String id;

    @TableField(value = "TABLE_NAME")
    @ApiModelProperty(value = "消费的表名")
    private String tableName;


    @TableField(value = "EX_MSG")
    @ApiModelProperty(value = "异常消息体")
    private String exMsg;

    @TableField(value = "EX_ERROR")
    @ApiModelProperty(value = "异常信息")
    private String exError;


    @TableField(value = "EX_TIME")
    @ApiModelProperty(value = "异常消费时间")
    private Date exTime;

}