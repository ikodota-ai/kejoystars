package com.mdd.front.vo.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("【请填写功能名称】详情Vo")
public class UserHistoryDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "")
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    private Integer userid;

    @ApiModelProperty(value = "收藏类型，S=明星，M=影视")
    private String type;

    @ApiModelProperty(value = "明星编号或影视编号")
    private Integer sMId;


}
