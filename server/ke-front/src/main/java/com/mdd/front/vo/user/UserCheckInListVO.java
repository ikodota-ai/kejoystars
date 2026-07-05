package com.mdd.front.vo.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@Data
public class UserCheckInListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @ApiModelProperty(value = "")
    private Integer id;

    @ApiModelProperty(value = "用户编号")
    private Integer userid;

    @ApiModelProperty(value = "签到时间")
    private String createTime;

    @ApiModelProperty(value = "奖励")
    private BigDecimal reward;
}
