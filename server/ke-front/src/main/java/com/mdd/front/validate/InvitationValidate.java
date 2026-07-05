package com.mdd.front.validate;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class InvitationValidate implements Serializable {
    @NotNull(message = "邀请码不存在！")
    @Length(min = 10, max = 10, message = "邀请码格式不正确！")
    @ApiModelProperty("code")
    private String code;
}
