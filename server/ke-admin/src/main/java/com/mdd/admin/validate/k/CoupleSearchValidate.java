package com.mdd.admin.validate.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

@Data
@ApiModel("couple搜素参数")
public class CoupleSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

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

    @ApiModelProperty(value = "添加管理员")
    private String createUser;

    @ApiModelProperty(value = "修改管理员")
    private String updateUser;

}
