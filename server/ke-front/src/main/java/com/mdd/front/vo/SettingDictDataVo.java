package com.mdd.front.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("字典数据Vo")
public class SettingDictDataVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "键")
    private String name;

    @ApiModelProperty(value = "值")
    private String value;
}
