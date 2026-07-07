package com.mdd.admin.validate.app;

import com.mdd.common.validator.annotation.IntegerContains;
import com.mdd.common.validator.annotation.StringContains;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("APP版本创建参数")
public class AppVersionCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "平台不能为空")
    @StringContains(values = {"android", "ios"}, message = "平台不合法")
    @ApiModelProperty(value = "平台: android/ios", required = true)
    private String platform;

    @Length(max = 32, message = "通道名称过长")
    @ApiModelProperty(value = "通道, 默认production")
    private String channel = "production";

    @NotEmpty(message = "版本名称不能为空")
    @Length(max = 32, message = "版本名称过长")
    @ApiModelProperty(value = "版本名称, 如1.4.2", required = true)
    private String versionName;

    @NotNull(message = "版本号不能为空")
    @DecimalMin(value = "1", message = "版本号必须大于0")
    @ApiModelProperty(value = "版本号(整数)", required = true)
    private Integer versionCode;

    @DecimalMin(value = "0", message = "最低兼容版本号不能小于0")
    @ApiModelProperty(value = "最低兼容版本号")
    private Integer minVersionCode = 0;

    @NotNull(message = "缺少isForce参数")
    @IntegerContains(values = {0, 1}, message = "isForce不合法")
    @ApiModelProperty(value = "是否强制更新: 0/1", required = true)
    private Integer isForce = 0;

    @Length(max = 128, message = "包名过长")
    @ApiModelProperty(value = "包名/BundleId")
    private String bundleId = "";

    @NotEmpty(message = "安装地址不能为空")
    @Length(max = 1024, message = "安装地址过长")
    @ApiModelProperty(value = "安装地址", required = true)
    private String installUrl;

    @Length(max = 1024, message = "下载地址过长")
    @ApiModelProperty(value = "下载地址")
    private String downloadUrl = "";

    @DecimalMin(value = "0", message = "包大小不能小于0")
    @ApiModelProperty(value = "安装包大小(字节)")
    private Long packageSize = 0L;

    @Length(max = 64, message = "MD5过长")
    @ApiModelProperty(value = "安装包MD5")
    private String packageMd5 = "";

    @ApiModelProperty(value = "更新说明")
    private String releaseNote = "";

    @StringContains(values = {"draft", "online", "offline"}, message = "状态不合法")
    @ApiModelProperty(value = "状态: draft/online/offline")
    private String status = "draft";

    @DecimalMin(value = "0", message = "灰度百分比不能小于0")
    @ApiModelProperty(value = "灰度百分比(0-100)")
    private Integer grayPercent = 100;

}
