package com.mdd.front.controller;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.mdd.common.aop.NotLogin;
import com.mdd.common.aop.NotPower;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.common.util.StringUtils;
import com.mdd.common.validator.annotation.IDMust;
import com.mdd.front.service.ISettingDictDataService;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.vo.SettingDictDataVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dict.dictData")
@Api(tags = "区域国家数据")
public class SettingDictDataController {

    @Resource
    ISettingDictDataService iSettingDictDataService;

    @NotLogin
    @GetMapping("/lists")
    @ApiOperation(value="区域国家列表")
    public AjaxResult<List<SettingDictDataVo>> list() {
        Map<String, String> params = new HashMap<String, String>(){{
           put("type_id", "6");
        }};
        List<SettingDictDataVo> list = iSettingDictDataService.list(params);
        return AjaxResult.success(list);
    }
}
