package com.mdd.admin.validate.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

@Data
@ApiModel("明星资料搜素参数")
public class StarInfoSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "明星姓名")
    private String name;

    @ApiModelProperty(value = "明星姓名英文")
    private String enName;

    @ApiModelProperty(value = "明星照片")
    private String avatar;

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
    private Integer favorites;

    @ApiModelProperty(value = "收到糖果数")
    private Integer candy;

    @ApiModelProperty(value = "关联影视")
    private String movies;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "修改人")
    private String updateUser;

}
