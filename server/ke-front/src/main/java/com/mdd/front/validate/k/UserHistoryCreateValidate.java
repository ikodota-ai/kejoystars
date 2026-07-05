package com.mdd.front.validate.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import javax.validation.constraints.*;

@Data
@ApiModel("【请填写功能名称】创建参数")
public class UserHistoryCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "请传入收藏类型！")
    @ApiModelProperty(value = "收藏类型，S=明星，M=影视, C=CP")
    private String type;

    @NotNull(message = "请确认编号是否正确！")
    @ApiModelProperty(value = "明星编号或影视编号")
    private Integer sMId;

    @ApiModelProperty(value = "用户编号，前端不需要传入")
    private Integer userid;
}
