package com.mdd.admin.validate.app;

import com.mdd.common.validator.annotation.IDMust;
import com.mdd.common.validator.annotation.StringContains;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("APP版本发布参数")
public class AppVersionPublishValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @ApiModelProperty(value = "ID", required = true)
    private Integer id;

    @StringContains(values = {"draft", "online", "offline"}, message = "状态不合法")
    @ApiModelProperty(value = "状态: draft/online/offline", required = true)
    private String status;

}
