package com.dhcc.aml.modules.kafka.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
  * @author zhaomingxing
  * 描述-
  * date 2021/9/23
  */
@ApiModel(value="异常消息表")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "C_EX_LOG")
public class CExLog implements Serializable {

    @TableId(value = "ID")
    @ApiModelProperty(value="主键")
    private String id;


    @TableField(value = "EX_MSG")
    @ApiModelProperty(value="异常消息体")
    private String exMsg;

    @TableField(value = "EX_ERROR")
    @ApiModelProperty(value="异常信息")
    private String exError;


    @TableField(value = "EX_TIME")
    @ApiModelProperty(value="异常消费时间")
    private Date exTime;

}