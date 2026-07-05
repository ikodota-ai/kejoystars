package com.mdd.admin.validate.finance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("手动充值")
public class FinanceAddValidate implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("充值账号")
    @NotNull(message = "充值账号不能为空")
    private String account;
    @ApiModelProperty("充值金额")
    @NotNull(message = "充值金额不能为空")
    @Min(value = 1,  message = "充值金额不能小于1元")
    private BigDecimal amount;
}
