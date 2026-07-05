package com.mdd.front.vo.article;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("文章列表Vo")
public class ArticleListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Integer id;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "链接")
    private String url;

    @ApiModelProperty(value = "关联影视含ID")
    private String movies;

    @ApiModelProperty(value = "关联影视")
    private String movieName;

    @ApiModelProperty(value = "文章内容")
    private String content;

    @ApiModelProperty("简介")
    private String desc;

    @ApiModelProperty(value = "图片")
    private String image;

    @ApiModelProperty(value = "作者")
    private String author;

    @ApiModelProperty(value = "是否显示")
    private Integer isShow;

    @ApiModelProperty(value = "创建时间")
    private String createTime;
}
