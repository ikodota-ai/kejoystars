package com.mdd.admin.controller.k;

import com.mdd.admin.aop.Log;
import com.mdd.admin.service.k.ICoupleService;
import com.mdd.admin.validate.commons.IdValidate;
import com.mdd.admin.validate.k.CoupleCreateValidate;
import com.mdd.admin.validate.k.CoupleUpdateValidate;
import com.mdd.admin.validate.k.CoupleSearchValidate;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.vo.k.CoupleListedVo;
import com.mdd.admin.vo.k.CoupleDetailVo;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.common.validator.annotation.IDMust;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("adminapi/k.couple")
@Api(tags = "couple管理")
public class CoupleController {

    @Resource
    ICoupleService iCoupleService;

    @GetMapping("/list")
    @ApiOperation(value="couple列表")
    public AjaxResult<PageResult<CoupleListedVo>> list(@Validated PageValidate pageValidate,
                                                     @Validated CoupleSearchValidate searchValidate) {
        PageResult<CoupleListedVo> list = iCoupleService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @ApiOperation(value="couple详情")
    public AjaxResult<CoupleDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        CoupleDetailVo detail = iCoupleService.detail(id);
        return AjaxResult.success(detail);
    }

    @Log(title = "couple新增")
    @PostMapping("/add")
    @ApiOperation(value="couple新增")
    public AjaxResult<Object> add(@Validated @RequestBody CoupleCreateValidate createValidate) {
        iCoupleService.add(createValidate);
        return AjaxResult.success();
    }

    @Log(title = "couple编辑")
    @PostMapping("/edit")
    @ApiOperation(value="couple编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody CoupleUpdateValidate updateValidate) {
        iCoupleService.edit(updateValidate);
        return AjaxResult.success();
    }

    @Log(title = "couple删除")
    @PostMapping("/del")
    @ApiOperation(value="couple删除")
    public AjaxResult<Object> del(@Validated @RequestBody List<Integer> idValidate) {
        if ( idValidate.isEmpty() ) {
            return AjaxResult.failed("请选择要删除的couple");
        }
        iCoupleService.del(idValidate);
        return AjaxResult.success();
    }

}
