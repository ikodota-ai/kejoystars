package com.mdd.front.controller;

import com.mdd.common.aop.NotLogin;
import com.mdd.common.aop.NotPower;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.enums.ClientEnum;
import com.mdd.common.exception.OperateException;
import com.mdd.common.plugin.notice.NoticeCheck;
import com.mdd.common.util.ConfigUtils;
import com.mdd.common.util.StringUtils;
import com.mdd.front.LikeFrontThreadLocal;
import com.mdd.front.service.ILoginService;
import com.mdd.front.service.IUserService;
import com.mdd.front.validate.login.*;
import com.mdd.front.validate.users.UserBindWechatValidate;
import com.mdd.front.vo.login.LoginUrlsVo;
import com.mdd.front.vo.login.LoginTokenVo;
import com.mdd.front.vo.user.UserInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/api/login")
@Api(tags = "登录管理")
public class LoginController {

    @Resource
    ILoginService iLoginService;
    @Resource
    IUserService iUserService;

    @NotLogin
    @PostMapping("/register")
    @ApiOperation(value="注册账号")
    public AjaxResult<Object> register(@Validated @RequestBody RegisterValidate registerValidate) {
        Integer terminal = LikeFrontThreadLocal.getTerminal();
        if (StringUtils.isNull(registerValidate.getTerminal())) {
            terminal = LikeFrontThreadLocal.getTerminal();
        }
        //验证短信验证码
        int sceneCode = 102;//NoticeEnum.LOGIN_CAPTCHA.getCode();
        if (!NoticeCheck.verify(sceneCode, registerValidate.getCode(), registerValidate.getAccount())) {
            throw new OperateException("验证码错误!");
        }
        String account = registerValidate.getAccount();
        String password = registerValidate.getPassword();

        iLoginService.register(account, password, terminal);
        int freeVipDays = 0;
        try {
            freeVipDays = Integer.parseInt(ConfigUtils.get("user", "free_vip_days", "0"));
        } catch (NumberFormatException ignored) {}
        if (freeVipDays > 0) {
            String tip = ConfigUtils.get("user", "register_tip", "恭喜你注册成功，作为首次注册用户你已享有{days}天免费会员，可下载IG,X图片");
            if (StringUtils.isNull(tip) || StringUtils.isEmpty(tip)) {
                tip = "恭喜你注册成功，作为首次注册用户你已享有{days}天免费会员，可下载IG,X图片";
            }
            return AjaxResult.success(tip.replace("{days}", String.valueOf(freeVipDays)));
        }
        return AjaxResult.success("注册成功");
    }

    @NotLogin
    @PostMapping("/account")
    @ApiOperation(value="账号登录")
    public AjaxResult<LoginTokenVo> account(@Validated @RequestBody LoginPwdValidate loginPwdValidate) {
        if (StringUtils.isNull(loginPwdValidate.getTerminal())) {
            loginPwdValidate.setTerminal(LikeFrontThreadLocal.getTerminal());
        }
        LoginTokenVo vo = iLoginService.accountLogin(loginPwdValidate);
        return AjaxResult.success(vo);
    }

//    @NotLogin
//    @PostMapping("/mobileLogin")
//    @ApiOperation(value="手机登录")
//    public AjaxResult<LoginTokenVo> mobileLogin(@Validated @RequestBody LoginPhoneValidate loginPhoneValidate) {
//        Integer terminal = LikeFrontThreadLocal.getTerminal();
//        String mobile = loginPhoneValidate.getMobile();
//        String code = loginPhoneValidate.getCode();
//
//        LoginTokenVo vo = iLoginService.mobileLogin(mobile, code, terminal);
//        return AjaxResult.success(vo);
//    }

    @NotLogin
    @PostMapping("/mnpLogin")
    @ApiOperation(value="微信登录")
    public AjaxResult<LoginTokenVo> mnpLogin(@Validated @RequestBody LoginCodeValidate loginCodeValidate) {
        Integer terminal = ClientEnum.MNP.getCode();

        String code = loginCodeValidate.getCode();

        LoginTokenVo vo = iLoginService.mnpLogin(code, terminal);
        return AjaxResult.success(vo);
    }

    @NotLogin
    @PostMapping("/oaLogin")
    @ApiOperation(value="公众号登录")
    public AjaxResult<LoginTokenVo> oaLogin(@Validated @RequestBody LoginCodeValidate loginCodeValidate) {
        Integer terminal = ClientEnum.OA.getCode();
        String code = loginCodeValidate.getCode();
//        if (StringUtils.isNull(loginCodeValidate.getTerminal())) {
//            terminal = LikeFrontThreadLocal.getTerminal();
//        }
        LoginTokenVo vo = iLoginService.officeLogin(code, terminal);
        return AjaxResult.success(vo);
    }
    @NotLogin
    @GetMapping("/codeUrl")
    @ApiOperation(value="公众号链接")
    public AjaxResult<LoginUrlsVo> codeUrl(@Validated @NotNull() @RequestParam("url") String url) {
        LoginUrlsVo vo = new LoginUrlsVo();
        vo.setUrl(iLoginService.oaCodeUrl(url));
        return AjaxResult.success(vo);
    }


    @NotLogin
    @GetMapping("/getScanCode")
    @ApiOperation(value="PC扫码链接")
    public AjaxResult<LoginUrlsVo> scanCodeUrl(@Validated @NotNull() @RequestParam("url") String url, HttpSession session) {
        String qrcodeUrl = iLoginService.scanCodeUrl(url, session);
        LoginUrlsVo vo = new LoginUrlsVo();
        vo.setUrl(qrcodeUrl);
        return AjaxResult.success(vo);
    }

    @NotLogin
    @PostMapping("/scanLogin")
    @ApiOperation(value="PC扫码登录")
    public AjaxResult<Object> scanLogin(@Validated @RequestBody LoginScanValidate loginScanValidate, HttpSession session) {
        Integer terminal = LikeFrontThreadLocal.getTerminal();
        String code = loginScanValidate.getCode();
        String state = loginScanValidate.getState();

        LoginTokenVo vo = iLoginService.scanLogin(code, state, terminal, session);
        return AjaxResult.success(vo);
    }

    @NotLogin
    @PostMapping("/logout")
    @ApiOperation(value="退出登录")
    public AjaxResult<Object> logout() {
        iLoginService.logout();
        return AjaxResult.success();
    }

    @NotLogin
    @PostMapping("/oaAuthBind")
    @ApiOperation(value="绑定微信公众号")
    public AjaxResult<Object> oaAuthBind(@Validated @RequestBody UserBindWechatValidate BindOaValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();
        iUserService.bindOa(BindOaValidate, userId);
        return AjaxResult.success();
    }


    @PostMapping("/mnpAuthBind")
    @ApiOperation(value="绑定小程序")
    public AjaxResult<Object> mnpAuthBind(@Validated @RequestBody UserBindWechatValidate BindOaValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();
        iUserService.bindMnp(BindOaValidate, userId);
        return AjaxResult.success();
    }

}
