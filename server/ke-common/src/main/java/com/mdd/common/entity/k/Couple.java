package com.mdd.common.entity.k;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel("couple实体")
public class Couple implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @ApiModelProperty(value = "")
    private Integer id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "拼音名称")
    private String enName;

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

    @ApiModelProperty(value = "添加时间")
    private Long createTime;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

}