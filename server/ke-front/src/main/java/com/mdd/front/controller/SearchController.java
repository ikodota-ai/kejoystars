package com.mdd.front.controller;

import com.alibaba.fastjson.JSONObject;
import com.mdd.common.aop.NotLogin;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.front.service.ISearchService;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.vo.SearchListedVo;
import com.mdd.front.vo.k.CoupleListedVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/search")
@Api(tags = "搜索")
public class SearchController {


    @Resource
    ISearchService iSearchService;

    @NotLogin
    @GetMapping("/hotLists")
    @ApiOperation(value="搜索列表")
    public AjaxResult<JSONObject> hotLists() {
        JSONObject result = iSearchService.hotLists();
        return AjaxResult.success(result);
    }

    @NotLogin
    @GetMapping("/keywords")
    @ApiOperation(value="搜索")
    public AjaxResult<PageResult<SearchListedVo>> keywords(@Validated PageValidate pageValidate,
                                                           @Validated @NotNull(message = "请输入关键字！") @Param("keywords") String keywords) {
        PageResult<SearchListedVo> list = iSearchService.list(pageValidate, keywords);
        return AjaxResult.success(list);
    }
}
