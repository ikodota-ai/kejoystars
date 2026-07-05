package com.mdd.front.controller.k;

import com.mdd.common.aop.NotLogin;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.common.enums.ErrorEnum;
import com.mdd.common.exception.OperateException;
import com.mdd.common.validator.annotation.IDMust;
import com.mdd.front.LikeFrontThreadLocal;
import com.mdd.front.service.k.IStarInfoService;
import com.mdd.front.service.k.IStarInstagramService;
import com.mdd.front.service.k.IUserHistoryService;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.validate.k.StarInfoSearchValidate;
import com.mdd.front.validate.k.StarInstagramSearchValidate;
import com.mdd.front.validate.k.UserHistoryCreateValidate;
import com.mdd.front.vo.k.StarGiftLogListedVo;
import com.mdd.front.vo.k.StarInfoDetailVo;
import com.mdd.front.vo.k.StarInfoListedVo;
import com.mdd.front.vo.k.StarInstagramListedVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/star")
@Api(tags = "明星资料")
public class StarInfoController {

    @Resource
    IStarInfoService iStarInfoService;

    @Resource
    IStarInstagramService iStarInstagramService;

    @NotLogin
    @GetMapping("/lists")
    @ApiOperation(value="明星列表")
    public AjaxResult<PageResult<StarInfoListedVo>> list(@Validated PageValidate pageValidate,
                                                         @Validated StarInfoSearchValidate searchValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();
        if (searchValidate.getCollect()!=null && searchValidate.getCollect() == 1 && userId <= 0) {
            throw new OperateException("未登录，请先登录后使用只看关注！");
        }

        PageResult<StarInfoListedVo> list = iStarInfoService.list(userId, pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @NotLogin
    @GetMapping("/detail")
    @ApiOperation(value="明星资料详情")
    public AjaxResult<StarInfoDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        StarInfoDetailVo vo = iStarInfoService.detail(id);
        return AjaxResult.success(vo);
    }

    @GetMapping("/gift")
    @ApiOperation(value="赠送糖果")
    public AjaxResult<Object> gift(@Validated @IDMust() @RequestParam("starId") Integer starId) {
        try {
            Map<String, Object> result = iStarInfoService.gift(starId, 1);
            return AjaxResult.success(result);
        } catch (RuntimeSqlException e) {
            return AjaxResult.failed(ErrorEnum.INSUFFICIENT_BALANCE.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new OperateException(e.getMessage());
        }
    }

    @GetMapping("/giftLogByMonth")
    @ApiOperation(value="赠送糖果月按日统计")
    public AjaxResult<Object> giftLogByMonth(@Validated @NotNull(message = "请传入年月如:2025-09!") @RequestParam("month") String month) {
        List<Map<String, Object>> result = iStarInfoService.giftLogByMonth(month);
        return AjaxResult.success(result);
    }

//    @GetMapping("/giftLogGroupByStarID")
//    @ApiOperation(value="赠送糖果数据按明星统计")
//    public AjaxResult<Object> giftLogGroupByStarID(@NotNull(message = "排序方式不传是最多倒序，1=最少排序，") @RequestParam("by") Integer by,
//                                                   @Validated @NotNull(message = "请传获取条数!") @RequestParam("limit") Integer limit) {
//
//        Integer userId = LikeFrontThreadLocal.getUserId();
//        List<Map<String, Object>> result = iStarInfoService.giftLogGroupByStarID(userId, limit, by);
//        return AjaxResult.success(result);
//    }
    @GetMapping("/giftLogGroupByStarID")
    @ApiOperation(value="赠送糖果数据按明星统计")
    public AjaxResult<Object> giftLogGroupByStarID(@Validated PageValidate pageValidate,
                                                   @NotNull(message = "排序方式不传是最多倒序，1=最少排序，") @RequestParam("by") Integer by) {

        Integer userId = LikeFrontThreadLocal.getUserId();
        PageResult<StarGiftLogListedVo> result = iStarInfoService.giftLogGroupByStarID(pageValidate, userId, by);
        return AjaxResult.success(result);
    }

    @NotLogin
    @GetMapping("/instagram")
    @ApiOperation(value="明星照片墙")
    public AjaxResult<PageResult<StarInstagramListedVo>> instagram(@Validated PageValidate pageValidate,
                                                         @Validated StarInstagramSearchValidate searchValidate) {
        searchValidate.setStatus("Y");
        PageResult<StarInstagramListedVo> list = iStarInstagramService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }
    ///api/star/instagram/downloadCount
    @GetMapping("/instagram/downloadCount")
    @ApiOperation(value="明星照片墙已下载次数")
    public AjaxResult<Object> instagramDownload() {
        int count = iStarInstagramService.getDownloadCount(LikeFrontThreadLocal.getUserId());
        return AjaxResult.success("今日已下载次数", count+"");
    }
}
