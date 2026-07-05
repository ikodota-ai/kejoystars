package com.mdd.admin.vo.k;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("couple详情Vo")
public class CoupleDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "")
    private Integer id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "国家编号")
    private Long country;

    @ApiModelProperty(value = "标题图像")
    private String image;

    @ApiModelProperty(value = "关联明星")
    private String star;

    @ApiModelProperty(value = "关联明星编号及名字")
    private List<Map<String, String>> starInfo;

    @ApiModelProperty(value = "关联影视")
    private String movies;

    @ApiModelProperty(value = "关联影视编号及名称")
    private List<Map<String, String>> moviesInfo;

    @ApiModelProperty(value = "收藏统计次数")
    private Integer historyCount;

    @ApiModelProperty(value = "添加管理员")
    private String createUser;

    @ApiModelProperty(value = "修改管理员")
    private String updateUser;


}
