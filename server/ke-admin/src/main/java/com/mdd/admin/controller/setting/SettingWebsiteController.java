package com.mdd.admin.controller.setting;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import com.mdd.admin.aop.Log;
import com.mdd.admin.service.ISettingCopyrightService;
import com.mdd.admin.service.ISettingProtocolService;
import com.mdd.admin.service.ISettingWebsiteService;
import com.mdd.admin.validate.setting.SettingCopyrightValidate;
import com.mdd.admin.validate.setting.SettingAgreementValidate;
import com.mdd.admin.validate.setting.SettingSiteStatisticsValidate;
import com.mdd.admin.validate.setting.SettingWebsiteValidate;
import com.mdd.admin.vo.setting.SettingCopyrightVo;
import com.mdd.admin.vo.setting.SettingAgreementVo;
import com.mdd.admin.vo.setting.SettingSiteStatisticsVo;
import com.mdd.admin.vo.setting.SettingWebsiteVo;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.util.ConfigUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("adminapi/setting.web.web_setting")
@Api(tags = "配置网站信息")
public class SettingWebsiteController {

    @Resource
    ISettingWebsiteService iSettingWebsiteService;

    @Resource
    ISettingCopyrightService iSettingCopyrightService;

    @Resource
    ISettingProtocolService iSettingProtocolService;

    @GetMapping("/getWebsite")
    @ApiOperation(value="网站配置信息")
    public AjaxResult<SettingWebsiteVo> getWebsite() {
        SettingWebsiteVo detail = iSettingWebsiteService.getWebsite();
        return AjaxResult.success(detail);
    }

    @Log(title = "网站配置编辑")
    @PostMapping("/setWebsite")
    @ApiOperation(value="网站配置编辑")
    public AjaxResult<Object> setWebsite(@Validated @RequestBody SettingWebsiteValidate websiteValidate) {
        iSettingWebsiteService.setWebsite(websiteValidate);
        return AjaxResult.success();
    }

    @GetMapping("/getRechargeOption")
    @ApiOperation(value="充值选项设置列表")
    public AjaxResult<Object> getRechargeOption() {
        JSONArray list = iSettingWebsiteService.getRechargeOption();
        return AjaxResult.success(list);
    }

    @PostMapping("/setRechargeOption")
    @ApiOperation(value="充值选项设置列表")
    public AjaxResult<Object> getRechargeOption(@NotNull(message = "配置不能为空！") @RequestBody String c) {
        JSONArray option = JSONObject.parseObject(c).getJSONArray("c");
        //// 循环检查每个充值选项的必填字段是否存在且数据类型正确
        for (int i = 0; i < option.size(); i++) {
            JSONObject obj = option.getJSONObject(i);
            if (obj != null) {
                // 检查必填字段
                String[] requiredFields = {"id", "title", "amount", "candy", "expired", "status", "pic"};
                for (String field : requiredFields) {
                    if (!obj.containsKey(field)) {
                        return AjaxResult.failed("第" + (i + 1) + "项缺少必填字段: " + field);
                    }
                }
                // 检查字段类型
                try {
                    obj.getLong("id");
                    obj.getString("title");
                    obj.getBigDecimal("amount");
                    obj.getBigDecimal("candy");
                    obj.getIntValue("expired");
                    obj.getIntValue("status");
                    obj.getBigDecimal("pic");
                } catch (Exception e) {
                    return AjaxResult.failed("第" + (i + 1) + "项字段类型错误: " + e.getMessage());
                }
            } else {
                return AjaxResult.failed("充值选项格式错误");
            }
        }

        iSettingWebsiteService.setRechargeOption( option );
        return AjaxResult.success("充值选项设置成功");
    }

    @GetMapping("/getCopyright")
    @ApiOperation(value="网站版权信息")
    public AjaxResult<List<SettingCopyrightVo>> getCopyright() {
        List<SettingCopyrightVo> list = iSettingCopyrightService.getCopyright();
        return AjaxResult.success(list);
    }

    @Log(title = "网站版权编辑")
    @PostMapping("/setCopyright")
    @ApiOperation(value="网站版权编辑")
    public AjaxResult<Object> setCopyright(@Validated @RequestBody SettingCopyrightValidate copyrightValidate) {
        iSettingCopyrightService.setCopyright(copyrightValidate);
        return AjaxResult.success();
    }

    @GetMapping("/getAgreement")
    @ApiOperation(value="政策协议信息")
    public AjaxResult<SettingAgreementVo> getAgreement() {
        SettingAgreementVo detail = iSettingProtocolService.getAgreement();
        return AjaxResult.success(detail);
    }

    @Log(title = "政策协议编辑")
    @PostMapping("/setAgreement")
    @ApiOperation(value="政策协议编辑")
    public AjaxResult<Object> setAgreement(@Validated @RequestBody SettingAgreementValidate protocolValidate) {
        iSettingProtocolService.setAgreement(protocolValidate);
        return AjaxResult.success();
    }


    @GetMapping("/getSiteStatistics")
    @ApiOperation(value="获取站点统计配置")
    public AjaxResult<SettingSiteStatisticsVo> getSiteStatistics() {
        SettingSiteStatisticsVo vo = new SettingSiteStatisticsVo();
        vo.setClarityCode(ConfigUtils.get("siteStatistics", "clarity_code", ""));
        return AjaxResult.success(vo);
    }

    @Log(title = "站点统计配置")
    @PostMapping("/setSiteStatistics")
    @ApiOperation(value="站点统计配置")
    public AjaxResult<Object> setSiteStatistics(@Validated @RequestBody SettingSiteStatisticsValidate settingSiteStatisticsValidate) {
        ConfigUtils.set("siteStatistics", "clarity_code", settingSiteStatisticsValidate.getClarityCode());
        return AjaxResult.success();
    }

}
