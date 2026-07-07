package com.mdd.common.entity.app;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("APP版本实体")
public class AppVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @ApiModelProperty("ID")
    private Integer id;

    @ApiModelProperty(value = "平台: android=安卓, ios=苹果")
    private String platform;

    @ApiModelProperty(value = "发布通道: production/beta/internal")
    private String channel;

    @ApiModelProperty(value = "版本名称, 如1.4.2")
    private String versionName;

    @ApiModelProperty(value = "版本号(整数, 用于比较)")
    private Integer versionCode;

    @ApiModelProperty(value = "最低兼容版本号, 低于该值强制更新")
    private Integer minVersionCode;

    @ApiModelProperty(value = "是否强制更新: 0=否, 1=是")
    private Integer isForce;

    @ApiModelProperty(value = "包名/BundleId")
    private String bundleId;

    @ApiModelProperty(value = "安装地址: iOS为itms-services或分发平台链接, Android为apk直链")
    private String installUrl;

    @ApiModelProperty(value = "下载地址: apk/ipa原始文件直链")
    private String downloadUrl;

    @ApiModelProperty(value = "安装包大小(字节)")
    private Long packageSize;

    @ApiModelProperty(value = "安装包MD5")
    private String packageMd5;

    @ApiModelProperty(value = "更新说明")
    private String releaseNote;

    @ApiModelProperty(value = "状态: draft=草稿, online=上线, offline=下线")
    private String status;

    @ApiModelProperty(value = "灰度百分比(0-100)")
    private Integer grayPercent;

    @ApiModelProperty(value = "发布时间")
    private Long publishTime;

    @ApiModelProperty(value = "创建时间")
    private Long createTime;

    @ApiModelProperty(value = "更新时间")
    private Long updateTime;

    @ApiModelProperty(value = "删除时间")
    private Long deleteTime;

}
