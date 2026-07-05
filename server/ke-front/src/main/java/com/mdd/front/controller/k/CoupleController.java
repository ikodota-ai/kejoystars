package com.mdd.front.controller.k;

import com.mdd.common.aop.NotLogin;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.common.exception.OperateException;
import com.mdd.common.validator.annotation.IDMust;
import com.mdd.front.LikeFrontThreadLocal;
import com.mdd.front.service.k.ICoupleService;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.validate.k.CoupleSearchValidate;
import com.mdd.front.vo.k.CoupleDetailVo;
import com.mdd.front.vo.k.CoupleListedVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("api/k.couple")
@Api(tags = "CP")
public class CoupleController {

    @Resource
    ICoupleService iCoupleService;

    @NotLogin
    @GetMapping("/list")
    @ApiOperation(value="couple列表")
    public AjaxResult<PageResult<CoupleListedVo>> list(@Validated PageValidate pageValidate,
                                                       @Validated CoupleSearchValidate searchValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();
        if (searchValidate.getCollect()!=null && searchValidate.getCollect() == 1 && userId <= 0) {
            throw new OperateException("未登录，请先登录后使用只看关注！");
        }
        PageResult<CoupleListedVo> list = iCoupleService.list(userId, pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @NotLogin
    @GetMapping("/detail")
    @ApiOperation(value="couple详情")
    public AjaxResult<CoupleDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        CoupleDetailVo detail = iCoupleService.detail(id);
        return AjaxResult.success(detail);
    }
}
