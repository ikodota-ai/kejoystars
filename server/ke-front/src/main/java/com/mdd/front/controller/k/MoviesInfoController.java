package com.mdd.front.controller.k;

import com.alibaba.fastjson2.JSONObject;
import com.mdd.common.aop.NotLogin;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.common.exception.OperateException;
import com.mdd.common.validator.annotation.IDMust;
import com.mdd.front.LikeFrontThreadLocal;
import com.mdd.front.service.k.IMoviesInfoService;
import com.mdd.front.service.k.IStarInfoService;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.validate.k.MoviesInfoSearchValidate;
import com.mdd.front.vo.k.MoviesInfoDetailVo;
import com.mdd.front.vo.k.MoviesInfoListedVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("api/k.movies")
@Api(tags = "影视管理")
public class MoviesInfoController {

    @Resource
    IMoviesInfoService iMoviesInfoService;

    @NotLogin
    @GetMapping("/list")
    @ApiOperation(value="影视列表")
    public AjaxResult<PageResult<MoviesInfoListedVo>> list(@Validated PageValidate pageValidate,
                                                           @Validated MoviesInfoSearchValidate searchValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();
        if (searchValidate.getCollect()!=null && searchValidate.getCollect() == 1 && userId <= 0) {
            throw new OperateException("未登录，请先登录后使用只看关注！");
        }
        PageResult<MoviesInfoListedVo> list = iMoviesInfoService.list(userId, pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @NotLogin
    @GetMapping("/detail")
    @ApiOperation(value="影视详情")
    public AjaxResult<MoviesInfoDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MoviesInfoDetailVo detail = iMoviesInfoService.detail(id);
        return AjaxResult.success(detail);
    }
}
