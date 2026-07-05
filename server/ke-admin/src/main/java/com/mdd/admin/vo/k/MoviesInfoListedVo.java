package com.mdd.admin.vo.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel("【请填写功能名称】列表Vo")
public class MoviesInfoListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "")
    private Integer id;

    @ApiModelProperty(value = "影视名称")
    private String movieName;

    @ApiModelProperty(value = "")
    private String movieType;

    @ApiModelProperty(value = "封面图片")
    private String image;

    @ApiModelProperty(value = "制作公司")
    private String company;

    @ApiModelProperty(value = "播放平台")
    private String playPlatform;

    @ApiModelProperty(value = "集数")
    private Integer quantity;

    @ApiModelProperty(value = "开播时间")
    private Date startPlayTime;

    @ApiModelProperty(value = "更新每周几")
    private String updateWeek;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    @ApiModelProperty(value = "CP")
    private String cp;

    @ApiModelProperty(value = "出品国家")
    private Integer country;

    @ApiModelProperty(value = "演员列表")
    private String actorList;

    @ApiModelProperty(value = "状态，W=将要播出，P=热播，D=完结")
    private String status;

    @ApiModelProperty(value = "添加时间")
    private String createTime;

    @ApiModelProperty(value = "创建账号")
    private String createUser;

    @ApiModelProperty(value = "更新时间")
    private Date upTime;

    @ApiModelProperty(value = "更新账号")
    private String upUser;


}
