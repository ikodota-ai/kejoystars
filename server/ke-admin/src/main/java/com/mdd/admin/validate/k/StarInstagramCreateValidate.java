package com.mdd.admin.validate.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import javax.validation.constraints.*;

@Data
@ApiModel("【请填写功能名称】创建参数")
public class StarInstagramCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "请选择明星")
    @ApiModelProperty(value = "明星资料编号")
    private Integer starId;

    @NotNull(message = "请上传明星照片")
    @ApiModelProperty(value = "图片地址")
    private String image;

    @ApiModelProperty(value = "图片审核状态")
    private String status = "Y";

    @NotEmpty(message = "请选择图片来源！")
    @ApiModelProperty(value = "图片来源")
    private String source;

    @ApiModelProperty(value = "图片审核时间")
    private Integer verifyTime;

}
