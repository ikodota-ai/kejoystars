package com.mdd.front.validate.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("【请填写功能名称】搜素参数")
public class StarInstagramSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "明星资料编号")
    private Integer starId;

    @ApiModelProperty(value = "图片地址")
    private String image;

    @ApiModelProperty(value = "图片审核状态")
    private String status;

    @ApiModelProperty(value = "图片来源")
    private String source;

    @ApiModelProperty(value = "图片审核时间")
    private Integer verifyTime;

}
