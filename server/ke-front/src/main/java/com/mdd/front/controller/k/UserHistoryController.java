package com.mdd.front.controller.k;

import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.front.LikeFrontThreadLocal;
import com.mdd.front.service.k.IUserHistoryService;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.validate.k.UserHistoryCreateValidate;
import com.mdd.front.validate.k.UserHistorySearchValidate;
import com.mdd.front.validate.k.UserHistoryUpdateValidate;
import com.mdd.front.vo.k.UserHistoryListedVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("api/history")
@Api(tags = "明星影视收藏管理")
public class UserHistoryController {

    @Resource
    IUserHistoryService iUserHistoryService;

    @PostMapping("/collect")
    @ApiOperation(value="明星影视收藏新增")
    public AjaxResult<Object> add(@Validated @RequestBody UserHistoryCreateValidate createValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();
        createValidate.setUserid(userId);
        iUserHistoryService.add(createValidate);
        return AjaxResult.success("关注成功！");
    }

    @PostMapping("/remove")
    @ApiOperation(value="明星影视收藏删除")
    public AjaxResult<Object> remove(@Validated @RequestBody UserHistoryCreateValidate createValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();
        createValidate.setUserid(userId);
        iUserHistoryService.remove(createValidate);
        return AjaxResult.success("取消关注成功！");
    }
}
