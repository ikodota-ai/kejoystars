package com.mdd.admin.validate.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.*;

@Data
@ApiModel("【请填写功能名称】创建参数")
public class MoviesInfoCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "请填写影视名称")
    @ApiModelProperty(value = "影视名称")
    private String movieName;

    @ApiModelProperty(value = "影视又名")
    private String movieEnName;

    @NotNull(message = "请选择影视类型")
    @ApiModelProperty(value = "")
    private String movieType;

    @NotNull(message = "请上传影视封面图片")
    @ApiModelProperty(value = "封面图片")
    private String image;

    @ApiModelProperty(value = "制作公司")
    private String company;

    @ApiModelProperty(value = "播放平台")
    private String playPlatform;

    @ApiModelProperty(value = "集数")
    private Integer quantity = 1;

    @ApiModelProperty(value = "开播时间")
    private Date startPlayTime;

    @ApiModelProperty(value = "更新每周几")
    private String updateWeek;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    @ApiModelProperty(value = "CP")
    private String cp;

    @ApiModelProperty(value = "影视内容简介")
    private String content;

    @NotNull(message = "请选择出品国家")
    @ApiModelProperty(value = "出品国家")
    private Integer country;

    @ApiModelProperty(value = "演员列表")
    private String actorList;

    @NotNull(message = "请选择影视状态")
    @ApiModelProperty(value = "状态，W=将要播出，P=热播，D=完结")
    private String status;

}
