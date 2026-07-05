package com.mdd.front.vo.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("couple列表Vo")
public class CoupleListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "")
    private Integer id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "国家编号")
    private Long country;

    @ApiModelProperty(value = "标题图像")
    private String image;

    @ApiModelProperty(value = "关联明星")
    private String star;

    @ApiModelProperty(value = "关联影视")
    private String movies;

    @ApiModelProperty(value = "收藏统计次数")
    private Integer historyCount;

    @ApiModelProperty(value = "是否关注收藏 0否1是")
    public Integer isHistory;
}
