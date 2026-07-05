package com.mdd.common.entity.article;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("文章实体")
public class Article implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @ApiModelProperty("ID")
    private Integer id;

    @ApiModelProperty("分类")
    private Integer cid;

    @ApiModelProperty(value = "CP编号")
    private Integer cpid;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("链接")
    private String url;

    @ApiModelProperty("关联影视")
    private String movies;

    @ApiModelProperty("简介")
    @TableField(value = "`desc`")
    private String desc;

    @ApiModelProperty("摘要")
    @TableField(value = "abstract")
    private String abstractField;

    @ApiModelProperty("封面")
    private String image;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("作者")
    private String author;

    @ApiModelProperty("虚拟浏览量")
    private Integer clickVirtual;

    @ApiModelProperty("实际浏览量")
    private Integer clickActual;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("是否显示: [0=否, 1=是]")
    private Integer isShow;

    @ApiModelProperty("创建时间")
    private Long createTime;

    @ApiModelProperty("更新时间")
    private Long updateTime;

    @ApiModelProperty("删除时间")
    private Long deleteTime;

}
