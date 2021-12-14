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
import java.util.Date;


@ApiModel(value = "消费状态")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "C_K_STATUS")
public class CKStatus implements Serializable {

    @TableField(value = "STATUS")
    @ApiModelProperty(value = "0:正常消费,1:暂停消费")
    private String status;


    @TableField(value = "UPDATE_TIME")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}