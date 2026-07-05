package com.mdd.front.vo.k;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel("赠送明星糖果礼物实体")
public class StarGiftLogListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @ApiModelProperty(value = "")
    private Integer id;

    @ApiModelProperty(value = "明星编号")
    private Integer starId;

    @ApiModelProperty(value = "用户编号")
    private Integer userId;

    @ApiModelProperty(value = "赠送数量")
    private Integer giftCount;

    @ApiModelProperty(value = "明星名字")
    private String starName;

    @ApiModelProperty(value = "明星头像")
    private String avatar;

}