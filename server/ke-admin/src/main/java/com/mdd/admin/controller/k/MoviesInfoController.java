package com.mdd.admin.controller.k;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.mdd.admin.aop.Log;
import com.mdd.admin.service.k.IMoviesInfoService;
import com.mdd.admin.service.k.IStarInfoService;
import com.mdd.admin.service.k.ITmdbService;
import com.mdd.admin.validate.commons.IdValidate;
import com.mdd.admin.validate.k.MoviesInfoCreateValidate;
import com.mdd.admin.validate.k.MoviesInfoUpdateValidate;
import com.mdd.admin.validate.k.MoviesInfoSearchValidate;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.validate.k.StarInfoCreateValidate;
import com.mdd.admin.vo.k.MoviesInfoListedVo;
import com.mdd.admin.vo.k.MoviesInfoDetailVo;
import com.mdd.admin.vo.k.StarInfoDetailVo;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.common.validator.annotation.IDMust;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("adminapi/k.movies")
@Api(tags = "【请填写功能名称】管理")
public class MoviesInfoController {

    @Resource
    IMoviesInfoService iMoviesInfoService;

    @Resource
    IStarInfoService iStarInfoService;

    @Resource
    ITmdbService iTmdbService;

    @GetMapping("/list")
    @ApiOperation(value="【请填写功能名称】列表")
    public AjaxResult<PageResult<MoviesInfoListedVo>> list(@Validated PageValidate pageValidate,
                                                     @Validated MoviesInfoSearchValidate searchValidate) {
        PageResult<MoviesInfoListedVo> list = iMoviesInfoService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @ApiOperation(value="【请填写功能名称】详情")
    public AjaxResult<MoviesInfoDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MoviesInfoDetailVo detail = iMoviesInfoService.detail(id);
        return AjaxResult.success(detail);
    }

    @Log(title = "【请填写功能名称】新增")
    @PostMapping("/add")
    @ApiOperation(value="【请填写功能名称】新增")
    public AjaxResult<Object> add(@Validated @RequestBody MoviesInfoCreateValidate createValidate) {
        iMoviesInfoService.add(createValidate);
        return AjaxResult.success();
    }

    @Log(title = "【请填写功能名称】编辑")
    @PostMapping("/edit")
    @ApiOperation(value="【请填写功能名称】编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody MoviesInfoUpdateValidate updateValidate) {
        iMoviesInfoService.edit(updateValidate);
        return AjaxResult.success();
    }

    @Log(title = "【请填写功能名称】删除")
    @PostMapping("/del")
    @ApiOperation(value="【请填写功能名称】删除")
    public AjaxResult<Object> del(@Validated @RequestBody List<Integer> idValidate) {
        if ( idValidate.isEmpty() ) {
            return AjaxResult.failed("请选择要删除的影视");
        }
        iMoviesInfoService.del(idValidate);
        return AjaxResult.success();
    }

    @Log(title = "影视影视信息抓取")
    @GetMapping("/grab")
    @ApiOperation(value="影视影视信息抓取")
    public AjaxResult<Object> grab(@RequestParam("url") String url){
        if (url.isEmpty() || !url.matches("^http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?$")) {
            return AjaxResult.failed("URL格式不正确");
        }
        try {
            JSONObject info = iTmdbService.grabMovie(url);

            return AjaxResult.success("影视信息抓取成功！", info);
        } catch (IOException | ParseException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return AjaxResult.failed("抓取失败！错误信息：" + e.getMessage());
        }
    }
}
