package com.mdd.front.validate.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import javax.validation.constraints.*;
import com.mdd.common.validator.annotation.IDMust;

/**
 * 【请填写功能名称】参数
 * @author LikeAdmin
 */
@Data
@ApiModel("【请填写功能名称】更新参数")
public class UserHistoryUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "")
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    private Integer userid;

    @ApiModelProperty(value = "收藏类型，S=明星，M=影视, C=CP")
    private String type;

    @IDMust(message = "请传入明星编号或影视编号且需大于0")
    @ApiModelProperty(value = "明星编号或影视编号")
    private Integer sMId;
}
