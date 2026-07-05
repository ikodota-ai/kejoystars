package com.mdd.common.entity.k;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("【请填写功能名称】实体")
public class StarInstagram implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @ApiModelProperty(value = "")
    private Integer id;

    @ApiModelProperty(value = "明星资料编号")
    private Integer starId;

    @ApiModelProperty("校验码")
    private String checkCode;

    @ApiModelProperty(value = "图片地址")
    private String image;

    @ApiModelProperty(value = "批次")
    private String batch;

    @ApiModelProperty(value = "图片审核状态")
    private String status;

    @ApiModelProperty(value = "图片来源")
    private String source;

    @ApiModelProperty(value = "图片审核时间")
    private Integer verifyTime;

    @ApiModelProperty(value = "图片添加时间")
    private Long createTime;

}