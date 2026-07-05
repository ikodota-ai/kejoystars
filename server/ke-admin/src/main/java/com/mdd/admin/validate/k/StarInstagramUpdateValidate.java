package com.mdd.admin.validate.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import javax.validation.constraints.*;
import com.mdd.common.validator.annotation.IDMust;
import net.bytebuddy.implementation.bind.annotation.Empty;

/**
 * 【请填写功能名称】参数
 * @author LikeAdmin
 */
@Data
@ApiModel("【请填写功能名称】更新参数")
public class StarInstagramUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "请传入要处理的照片编号")
    @ApiModelProperty(value = "")
    private String id;

    @NotNull(message = "请传入图片状态")
    @ApiModelProperty(value = "图片审核状态")
    private String status;
}
