package com.mdd.common.entity.k;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("图片下载次数实体")
public class StarInstagramDownLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value="id", type= IdType.INPUT)
    @ApiModelProperty(value = "编号")
    private String id;

    @ApiModelProperty(value = "用户ID")
    private Integer userid;

    @ApiModelProperty(value = "图片编号")
    private Integer insid;

    @ApiModelProperty(value = "图片md5代码")
    private String checkCode;

    @ApiModelProperty(value = "下载时间")
    private Long createTime;

}