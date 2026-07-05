package com.mdd.front.validate.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("明星资料创建参数")
public class StarInfoCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "name参数缺失")
    @ApiModelProperty(value = "明星姓名")
    private String name;

    @ApiModelProperty(value = "明星姓名英文")
    private String enName;

    @ApiModelProperty(value = "性别")
    private String gender;

    @NotNull(message = "avatar参数缺失")
    @ApiModelProperty(value = "明星照片")
    private String avatar;

    @NotNull(message = "country参数缺失")
    @ApiModelProperty(value = "演员国籍编号")
    private Integer country;

    @ApiModelProperty(value = "生日")
    private Date birthday;

    @ApiModelProperty(value = "身高")
    private BigDecimal height;

    @ApiModelProperty(value = "体重")
    private BigDecimal weight;

    @ApiModelProperty(value = "星座")
    private String constellation;

    @ApiModelProperty(value = "毕业大学，没上过大学就是最高学历毕业学校")
    private String university;

    @ApiModelProperty(value = "院系")
    private String universityDepartments;

    @ApiModelProperty(value = "x 账号名")
    private String x;

    @ApiModelProperty(value = "twitter")
    private String twitter;

    @ApiModelProperty(value = "tiktok")
    private String tiktok;

    @ApiModelProperty(value = "收藏次数")
    private Integer favorites = 0;

    @ApiModelProperty(value = "收到糖果数")
    private Integer candy = 0;

    @ApiModelProperty(value = "关联影视")
    private String movies;

    @ApiModelProperty(value = "简介")
    private String biography;

    @ApiModelProperty(value = "抓取回来的原始json数据")
    private String original;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "修改人")
    private String updateUser;

}
