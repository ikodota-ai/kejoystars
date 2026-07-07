package com.mdd.admin.vo.app;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("APP版本列表Vo")
public class AppVersionListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("ID")
    private Integer id;

    @ApiModelProperty("平台")
    private String platform;

    @ApiModelProperty("通道")
    private String channel;

    @ApiModelProperty("版本名称")
    private String versionName;

    @ApiModelProperty("版本号")
    private Integer versionCode;

    @ApiModelProperty("最低兼容版本号")
    private Integer minVersionCode;

    @ApiModelProperty("是否强制更新")
    private Integer isForce;

    @ApiModelProperty("安装地址")
    private String installUrl;

    @ApiModelProperty("下载地址")
    private String downloadUrl;

    @ApiModelProperty("安装包大小")
    private Long packageSize;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("灰度百分比")
    private Integer grayPercent;

    @ApiModelProperty("发布时间")
    private String publishTime;

    @ApiModelProperty("创建时间")
    private String createTime;

    @ApiModelProperty("更新时间")
    private String updateTime;

}
