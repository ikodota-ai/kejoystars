package com.mdd.front.validate.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("couple创建参数")
public class CoupleCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "name参数缺失")
    @ApiModelProperty(value = "名称")
    private String name;

    @NotNull(message = "country参数缺失")
    @ApiModelProperty(value = "国家编号")
    private Long country;

    @NotNull(message = "image参数缺失")
    @ApiModelProperty(value = "标题图像")
    private String image;

    @ApiModelProperty(value = "关联明星")
    private String star;

    @ApiModelProperty(value = "关联影视")
    private String movies;

    @ApiModelProperty(value = "收藏统计次数")
    private Integer historyCount;

    @ApiModelProperty(value = "添加管理员")
    private String createUser;

    @ApiModelProperty(value = "修改管理员")
    private String updateUser;

}
