package com.mdd.front.controller;

import com.alibaba.fastjson2.JSONArray;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.RechargeOrder;
import com.mdd.common.enums.PaymentEnum;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.RechargeOrderMapper;
import com.mdd.common.util.ConfigUtils;
import com.mdd.common.util.StringUtils;
import com.mdd.front.LikeFrontThreadLocal;
import com.mdd.front.service.IPayService;
import com.mdd.front.service.IRechargeService;
import com.mdd.front.validate.PaymentValidate;
import com.mdd.front.validate.RechargeValidate;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.vo.RechargeConfigVo;
import com.mdd.front.vo.RechargeRecordVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/recharge")
@Api(tags = "充值管理")
public class RechargeController {

    @Resource
    IRechargeService iRechargeService;

    @GetMapping("/config")
    @ApiOperation(value = "充值配置")
    public AjaxResult<Object> config() {
//        Integer userId = LikeFrontThreadLocal.getUserId();

//        RechargeConfigVo vo = iRechargeService.config(userId);
        String r = ConfigUtils.get("recharge", "list");
        JSONArray list = JSONArray.parseArray(r);
        JSONArray lists = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            if (list.getJSONObject(i).getInteger("status") == 1) {
                lists.add(list.getJSONObject(i));
            }
        }
        return AjaxResult.success(lists);
    }

    @GetMapping("/lists")
    @ApiOperation(value = "充值记录")
    public AjaxResult<Object> lists(@Validated PageValidate pageValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();

        PageResult<RechargeRecordVo> list = iRechargeService.record(userId, pageValidate);
        return AjaxResult.success(list);
    }

    @PostMapping("/recharge")
    @ApiOperation(value = "充值下单")
    public AjaxResult<Object> recharge(@Validated @RequestBody RechargeValidate rechargeValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();
        Integer terminal = LikeFrontThreadLocal.getTerminal();

        Map<String, Object> result = iRechargeService.placeOrder(userId, terminal, rechargeValidate);

        return AjaxResult.success(result.get("result"));
    }
}
