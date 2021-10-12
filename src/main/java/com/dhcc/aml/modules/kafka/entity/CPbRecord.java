package com.dhcc.aml.modules.kafka.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhaomingxing
 * 描述-
 * date 2021/9/23
 */
@ApiModel(value = "跑批记录表")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "C_PB_RECORD")
public class CPbRecord implements Serializable {

    @TableId(value = "ID")
    @ApiModelProperty(value = "主键")
    private String id;


    @TableField(value = "PB_TIME")
    @ApiModelProperty(value = "跑批时间")
    private String pbTime;


    @TableField(value = "STATUS")
    @ApiModelProperty(value = "状态-[ 0,可跑批、1,跑批中、2,已跑批]")
    private String status;


    @TableField(value = "FIRST_DATA_MSG")
    @ApiModelProperty(value = "最后一个消息体json")
    private String firstDataMsg;


    @TableField(value = "CRATE_TIME")
    @ApiModelProperty(value = "创建时间")
    private Date crateTime;

    /**
     *  {@link #status}
     */
    @Getter
    @AllArgsConstructor
    public enum Status {
        RUN("0"), RUNNING("1"), RUNNED("2");

        private String code;

    }

}