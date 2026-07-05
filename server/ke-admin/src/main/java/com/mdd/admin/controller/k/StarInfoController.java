package com.mdd.admin.controller.k;

import com.mdd.admin.aop.Log;
import com.mdd.admin.service.k.IStarInfoService;
import com.mdd.admin.validate.commons.IdValidate;
import com.mdd.admin.validate.k.StarInfoCreateValidate;
import com.mdd.admin.validate.k.StarInfoUpdateValidate;
import com.mdd.admin.validate.k.StarInfoSearchValidate;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.vo.k.StarInfoListedVo;
import com.mdd.admin.vo.k.StarInfoDetailVo;
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
@RequestMapping("adminapi/k.star")
@Api(tags = "明星资料管理")
public class StarInfoController {

    @Resource
    IStarInfoService iStarInfoService;

    @GetMapping("/list")
    @ApiOperation(value="明星资料列表")
    public AjaxResult<PageResult<StarInfoListedVo>> list(@Validated PageValidate pageValidate,
                                                     @Validated StarInfoSearchValidate searchValidate) {
        PageResult<StarInfoListedVo> list = iStarInfoService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @ApiOperation(value="明星资料详情")
    public AjaxResult<StarInfoDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        StarInfoDetailVo detail = iStarInfoService.detail(id);
        return AjaxResult.success(detail);
    }

    @Log(title = "明星资料新增")
    @PostMapping("/add")
    @ApiOperation(value="明星资料新增")
    public AjaxResult<Object> add(@Validated @RequestBody StarInfoCreateValidate createValidate) {
        iStarInfoService.add(createValidate);
        return AjaxResult.success();
    }

    @Log(title = "明星资料编辑")
    @PostMapping("/edit")
    @ApiOperation(value="明星资料编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody StarInfoUpdateValidate updateValidate) {
        iStarInfoService.edit(updateValidate);
        return AjaxResult.success();
    }

    @Log(title = "明星资料删除")
    @PostMapping("/del")
    @ApiOperation(value="明星资料删除")
    public AjaxResult<Object> del(@Validated @RequestBody List<Integer> ids) {
        if ( ids.isEmpty() ) {
            return AjaxResult.failed("请选择要删除的明星");
        }

        iStarInfoService.del(ids);
        return AjaxResult.success();
    }

}
