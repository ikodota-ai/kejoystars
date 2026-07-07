package com.mdd.admin.controller.app;

import com.mdd.admin.aop.Log;
import com.mdd.admin.service.IAppVersionService;
import com.mdd.admin.validate.app.AppVersionCreateValidate;
import com.mdd.admin.validate.app.AppVersionPublishValidate;
import com.mdd.admin.validate.app.AppVersionSearchValidate;
import com.mdd.admin.validate.app.AppVersionUpdateValidate;
import com.mdd.admin.validate.commons.IdValidate;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.vo.app.AppVersionDetailVo;
import com.mdd.admin.vo.app.AppVersionListedVo;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.common.validator.annotation.IDMust;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("adminapi/app.version")
@Api(tags = "APP版本管理")
public class AppVersionController {

    @Resource
    IAppVersionService iAppVersionService;

    @GetMapping("/list")
    @ApiOperation(value="APP版本列表")
    public AjaxResult<PageResult<AppVersionListedVo>> list(@Validated PageValidate pageValidate,
                                                           @Validated AppVersionSearchValidate searchValidate) {
        PageResult<AppVersionListedVo> vos = iAppVersionService.list(pageValidate, searchValidate);
        return AjaxResult.success(vos);
    }

    @GetMapping("/detail")
    @ApiOperation(value="APP版本详情")
    public AjaxResult<AppVersionDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        AppVersionDetailVo vo = iAppVersionService.detail(id);
        return AjaxResult.success(vo);
    }

    @Log(title = "APP版本新增")
    @PostMapping("/add")
    @ApiOperation(value="APP版本新增")
    public AjaxResult<Object> add(@Validated @RequestBody AppVersionCreateValidate createValidate) {
        iAppVersionService.add(createValidate);
        return AjaxResult.success();
    }

    @Log(title = "APP版本编辑")
    @PostMapping("/edit")
    @ApiOperation(value="APP版本编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody AppVersionUpdateValidate updateValidate) {
        iAppVersionService.edit(updateValidate);
        return AjaxResult.success();
    }

    @Log(title = "APP版本删除")
    @PostMapping("/del")
    @ApiOperation(value="APP版本删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iAppVersionService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @Log(title = "APP版本发布")
    @PostMapping("/publish")
    @ApiOperation(value="APP版本发布/上下线")
    public AjaxResult<Object> publish(@Validated @RequestBody AppVersionPublishValidate publishValidate) {
        iAppVersionService.publish(publishValidate.getId(), publishValidate.getStatus());
        return AjaxResult.success();
    }

}
