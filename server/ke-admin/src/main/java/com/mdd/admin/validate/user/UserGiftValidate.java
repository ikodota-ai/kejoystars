package com.mdd.admin.validate.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class UserGiftValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "缺少用户id参数")
    @Min(value = 1, message = "用户id必须为数字")
    @JsonProperty("user_id")
    private Integer userId;

    @NotNull(message = "请输入赠送天数")
    @Min(value = 1, message = "赠送天数必须大于0")
    private Integer days;

    @NotBlank(message = "请选择赠送批次")
    @JsonProperty("batch_key")
    private String batchKey;

    private String remark;
}
