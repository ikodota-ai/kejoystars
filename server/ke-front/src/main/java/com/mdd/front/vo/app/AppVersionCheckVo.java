package com.mdd.front.vo.app;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("APP版本检查结果Vo")
public class AppVersionCheckVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("是否有更新")
    private Boolean hasUpdate = false;

    @ApiModelProperty("是否强制更新")
    private Boolean isForce = false;

    @ApiModelProperty("最新版本名称")
    private String versionName = "";

    @ApiModelProperty("最新版本号")
    private Integer versionCode = 0;

    @ApiModelProperty("安装地址: iOS为itms-services或分发平台链接, Android为apk直链")
    private String installUrl = "";

    @ApiModelProperty("下载地址")
    private String downloadUrl = "";

    @ApiModelProperty("安装包大小(字节)")
    private Long packageSize = 0L;

    @ApiModelProperty("安装包MD5")
    private String packageMd5 = "";

    @ApiModelProperty("更新说明")
    private String releaseNote = "";

    @ApiModelProperty("发布时间戳")
    private Long publishTime = 0L;

}
