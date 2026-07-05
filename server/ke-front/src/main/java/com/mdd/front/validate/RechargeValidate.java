package com.mdd.front.validate;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("新用户更新信息参数")
public class RechargeValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "请选择充值VIP类型！")
    @ApiModelProperty("充值VIP编号")
    private Integer rid;

    @ApiModelProperty("充值金额,前端不需要传入！")
    private BigDecimal money;

    @ApiModelProperty("充值类型！")
    private String payType;
}
