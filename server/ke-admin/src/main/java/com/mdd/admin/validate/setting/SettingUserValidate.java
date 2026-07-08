package com.mdd.admin.validate.setting;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;

@Data
@ApiModel("用户设置参数")
public class SettingUserValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "默认头像")
    private String defaultAvatar = "";

    @Min(value = 0, message = "赠送会员天数不能小于0")
    @ApiModelProperty(value = "注册赠送会员天数(0=不赠送)")
    private Integer freeVipDays = 0;

}
