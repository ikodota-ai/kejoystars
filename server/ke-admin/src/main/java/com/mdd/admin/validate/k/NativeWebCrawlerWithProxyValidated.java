package com.mdd.admin.validate.k;


import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

@Data
@ApiModel("代理台数数据")
public class NativeWebCrawlerWithProxyValidated implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "填写要访问的URL;")
    @ApiModelProperty(value = "url", required = true)
    private String url;

    @ApiModelProperty(value = "source")
    private String source;

    @ApiModelProperty(value = "method")
    private String method;

    @ApiModelProperty(value = "headers")
    private JSONObject headers;

    @ApiModelProperty(value = "url地址映射")
    private Map<String, String> mappingUrl;
}
