package com.mdd.front.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel(value = "充值记录Vo")
public class RechargeRecordVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    private Integer id;

    @ApiModelProperty("充值VIP编号")
    private Integer rid;

    @ApiModelProperty("充值类型！")
    private String payType;

    @ApiModelProperty(value = "金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "描述")
    private String tips;

    @ApiModelProperty("支付状态：0-待支付；1-已支付")
    private Integer payStatus;

    @ApiModelProperty(value = "操作")
    private Integer action;

    @ApiModelProperty(value = "时间")
    private String createTime;

}
