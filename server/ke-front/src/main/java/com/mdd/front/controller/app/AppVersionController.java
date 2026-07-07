package com.mdd.front.controller.app;

import com.mdd.common.aop.NotLogin;
import com.mdd.common.core.AjaxResult;
import com.mdd.front.service.IAppVersionService;
import com.mdd.front.vo.app.AppVersionCheckVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/app/version")
@Api(tags = "APP版本")
public class AppVersionController {

    @Resource
    IAppVersionService iAppVersionService;

    @NotLogin
    @GetMapping("/check")
    @ApiOperation(value="检查更新")
    public AjaxResult<AppVersionCheckVo> check(@RequestParam("platform") String platform,
                                               @RequestParam(value = "channel", required = false, defaultValue = "production") String channel,
                                               @RequestParam(value = "versionCode", required = false, defaultValue = "0") Integer versionCode,
                                               @RequestParam(value = "deviceId", required = false, defaultValue = "") String deviceId) {
        AppVersionCheckVo vo = iAppVersionService.check(platform, channel, versionCode, deviceId);
        return AjaxResult.success(vo);
    }

}
