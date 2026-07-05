package com.mdd.admin.validate.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import javax.validation.constraints.*;

import com.mdd.common.validator.annotation.IDMust;

/**
 * 【请填写功能名称】参数
 * @author 十八子
 */
@Data
@ApiModel("couple更新参数")
public class CoupleUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @ApiModelProperty(value = "")
    private Integer id;

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
    private Integer historyCount=0;

    @ApiModelProperty(value = "添加管理员")
    private String createUser;

    @ApiModelProperty(value = "修改管理员")
    private String updateUser;

}
