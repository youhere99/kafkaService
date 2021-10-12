package com.dhcc.aml.modules.kafka.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
  * @author zhaomingxing
  * 描述-
  * date 2021/9/28
  */
@ApiModel(value="可消费的表")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "C_FILTER_TABLE")
public class CFilterTable implements Serializable {

    @TableField(value = "OWNER_TABLE_NAME")
    @ApiModelProperty(value="属主_表名")
    private String ownerTableName;


}