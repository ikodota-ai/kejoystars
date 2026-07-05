package com.mdd.front.validate.article;

import com.mdd.common.validator.annotation.StringContains;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Data
@ApiModel("文章搜索参数")
public class ArticleSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer cid;

    @ApiModelProperty(value = "CP编号")
    private Integer cpid;

    @Length(max = 100, message = "关键词过长了")
    @ApiModelProperty(value = "关键词")
    private String keyword;

    @ApiModelProperty(value = "只看关注")
    private Integer collect;

    @ApiModelProperty(value = "影视编号，可以多个影视，以\",\"号分隔")
    private String movieId;

    @StringContains(values = {"hot", "new", "default","desc","asc"})
    @ApiModelProperty(value = "排序号")
    private String sort;

    @ApiModelProperty(value = "创建时间戳")
    private String createTime;

    @ApiModelProperty(value = "创建日期")
    private String date;

}
