package com.mdd.admin.vo.setting;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("用户设置Vo")
public class SettingUserVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "默认头像")
    private String defaultAvatar;

    @ApiModelProperty(value = "注册赠送会员天数(0=不赠送)")
    private Integer freeVipDays;

    @ApiModelProperty(value = "注册成功提示语(支持{days}占位符)")
    private String registerTip;

}
