package com.mdd.common.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "搜索列表Vo")
public class Search implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("title")
    private String title;

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("类型，S=明星，M=电影，C=CP, A=资讯")
    private String type;

}
