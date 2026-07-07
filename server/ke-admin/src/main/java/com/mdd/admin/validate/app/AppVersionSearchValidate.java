package com.mdd.admin.validate.app;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("APP版本搜索参数")
public class AppVersionSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "平台")
    private String platform;

    @ApiModelProperty(value = "通道")
    private String channel;

    @ApiModelProperty(value = "版本名称")
    private String versionName;

    @ApiModelProperty(value = "状态")
    private String status;

}
