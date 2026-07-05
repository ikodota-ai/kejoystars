package com.mdd.admin.controller.k;

import com.mdd.admin.aop.Log;
import com.mdd.admin.service.k.IStarInstagramService;
import com.mdd.admin.validate.commons.IdValidate;
import com.mdd.admin.validate.k.StarInstagramCreateValidate;
import com.mdd.admin.validate.k.StarInstagramUpdateValidate;
import com.mdd.admin.validate.k.StarInstagramSearchValidate;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.vo.k.StarInstagramListedVo;
import com.mdd.admin.vo.k.StarInstagramDetailVo;
import com.mdd.common.aop.NotLogin;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.common.validator.annotation.IDMust;
import com.mdd.admin.crontab.CrawlQuartzJob;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("adminapi/k.instagram")
@Api(tags = "【请填写功能名称】管理")
public class StarInstagramController {

    @Resource
    IStarInstagramService iStarInstagramService;

    @GetMapping("/list")
    @ApiOperation(value="【请填写功能名称】列表")
    public AjaxResult<PageResult<StarInstagramListedVo>> list(@Validated PageValidate pageValidate,
                                                     @Validated StarInstagramSearchValidate searchValidate) {
        PageResult<StarInstagramListedVo> list = iStarInstagramService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @ApiOperation(value="【请填写功能名称】详情")
    public AjaxResult<StarInstagramDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        StarInstagramDetailVo detail = iStarInstagramService.detail(id);
        return AjaxResult.success(detail);
    }

    @Log(title = "【请填写功能名称】新增")
    @PostMapping("/add")
    @ApiOperation(value="【请填写功能名称】新增")
    public AjaxResult<Object> add(@Validated @RequestBody StarInstagramCreateValidate createValidate) {
        iStarInstagramService.add(createValidate);
        return AjaxResult.success();
    }

    @Log(title = "【请填写功能名称】编辑")
    @PostMapping("/edit")
    @ApiOperation(value="【请填写功能名称】编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody StarInstagramUpdateValidate updateValidate) {
        iStarInstagramService.edit(updateValidate);
        return AjaxResult.success();
    }

    @Log(title = "【请填写功能名称】删除")
    @PostMapping("/del")
    @ApiOperation(value="【请填写功能名称】删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iStarInstagramService.del(idValidate.getIds());
        return AjaxResult.success();
    }

    @Resource
    CrawlQuartzJob crawlQuartzJob;
    @NotLogin
    @GetMapping("/grabs")
    public AjaxResult<Object> grabs() {
        crawlQuartzJob.grabStarInfo();
        return AjaxResult.success();
    }

    @NotLogin
    @GetMapping("/grabsImage")
    public AjaxResult<Object> grabsImage() {
        crawlQuartzJob.handle();
        return AjaxResult.success();
    }
}
