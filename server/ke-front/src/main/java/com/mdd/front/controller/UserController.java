package com.mdd.front.controller;

import com.mdd.common.aop.NotLogin;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.exception.OperateException;
import com.mdd.front.LikeFrontThreadLocal;
import com.mdd.front.service.IUserService;
import com.mdd.front.validate.InvitationValidate;
import com.mdd.front.validate.users.*;
import com.mdd.front.vo.user.UserCenterVo;
import com.mdd.front.vo.user.UserCheckInListVO;
import com.mdd.front.vo.user.UserInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/user")
@Api(tags = "用户管理")
public class UserController {

    @Resource
    IUserService iUserService;

    @GetMapping("/center")
    @ApiOperation(value="个人中心")
    public AjaxResult<UserCenterVo> center() {
        Integer userId = LikeFrontThreadLocal.getUserId();
        Integer terminal = LikeFrontThreadLocal.getTerminal();

        UserCenterVo vo = iUserService.center(userId, terminal);
        return AjaxResult.success(vo);
    }

    @GetMapping("/info")
    @ApiOperation(value="个人信息")
    public AjaxResult<UserInfoVo> info() {
        Integer userId = LikeFrontThreadLocal.getUserId();

        UserInfoVo vo = iUserService.info(userId);
        return AjaxResult.success(vo);
    }

    @PostMapping("/setInfo")
    @ApiOperation(value="编辑信息")
    public AjaxResult<Object> setInfo(@Validated @RequestBody UserUpdateValidate updateValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();

        iUserService.setInfo(updateValidate, userId);
        return AjaxResult.success();
    }

    @PostMapping("/changePassword")
    @ApiOperation(value="修改密码")
    public AjaxResult<Object> changePassword(@Validated @RequestBody UserChangePwdValidate passwordValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();

        if (!passwordValidate.getPassword().equals(passwordValidate.getPasswordConfirm())) {
            throw new OperateException("两次输入的密码不一致");
        }
        iUserService.changePwd(passwordValidate.getPassword(), passwordValidate.getOldPassword(), userId);
        return AjaxResult.success();
    }

    @NotLogin
    @PostMapping("/resetPassword")
    @ApiOperation(value="重置密码")
    public AjaxResult<Object> resetPassword(@Validated @RequestBody ResetPasswordValidate passwordValidate) {
        iUserService.resetPassword(passwordValidate);
        return AjaxResult.success();
    }



    @NotLogin
    @PostMapping("/forgotPwd")
    @ApiOperation(value="忘记密码")
    public AjaxResult<Object> forgotPwd(@Validated @RequestBody UserForgetPwdValidate userForgetPwdValidate) {
        String password = userForgetPwdValidate.getPassword();
        String mobile = userForgetPwdValidate.getMobile();
        String code = userForgetPwdValidate.getCode();

        iUserService.forgotPwd(password, mobile, code);
        return AjaxResult.success();
    }

    @PostMapping("/bindMobile")
    @ApiOperation(value="绑定手机")
    public AjaxResult<Object> bindMobile(@Validated @RequestBody UserPhoneBindValidate mobileValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();

        iUserService.bindMobile(mobileValidate, userId);
        return AjaxResult.success();
    }

    @PostMapping("/getMobileByMnp")
    @ApiOperation(value="微信手机号")
    public AjaxResult<Object> mnpMobile(@Validated @RequestBody UserPhoneMnpValidate mobileValidate) {
        iUserService.mnpMobile(mobileValidate.getCode().trim());
        return AjaxResult.success();
    }

    @PostMapping("/updateUser")
    @ApiOperation(value="更新新用户信息")
    public AjaxResult<Object> updateData(@Validated @RequestBody NewUserUpdateValidate newUserUpdateValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();
        iUserService.updateNewUserInfo(newUserUpdateValidate, userId);
        return AjaxResult.success();
    }

    @PostMapping("/bindMnp")
    @ApiOperation(value="绑定小程序")
    public AjaxResult<Object> bindMnp(@Validated @RequestBody UserBindWechatValidate BindMnpValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();

        iUserService.bindMnp(BindMnpValidate, userId);
        return AjaxResult.success();
    }

    @PostMapping("/bindOa")
    @ApiOperation(value="绑定微信公众号")
    public AjaxResult<Object> bindOa(@Validated @RequestBody UserBindWechatValidate BindOaValidate) {
        Integer userId = LikeFrontThreadLocal.getUserId();

        iUserService.bindOa(BindOaValidate, userId);
        return AjaxResult.success();
    }

    @GetMapping("/checkIn")
    @ApiOperation(value="签到")
    public AjaxResult<Object> checkIn() {
        Integer userId = LikeFrontThreadLocal.getUserId();
        iUserService.checkIn(userId);
        return AjaxResult.success("签到成功！");
    }

    @GetMapping("/checkInList")
    @ApiOperation(value="签到记录")
    public AjaxResult<Object> checkInList(@Validated @NotNull(message = "月份不能为空") @Param("month") String month) {
        Integer userId = LikeFrontThreadLocal.getUserId();
        List<UserCheckInListVO> result = iUserService.checkInList(userId, month);
        return AjaxResult.success(result);
    }

    @GetMapping("/getInvitationCode")
    @ApiOperation(value="取得邀请码，及我邀请的用户")
    public AjaxResult<Object> getInvitationCode () {
        Integer userId = LikeFrontThreadLocal.getUserId();
        String result = iUserService.getInvitationCode( userId );
        List<Map<String, Object>> inviteUser = Collections.emptyList();
        if (result != null && !result.isEmpty()) {
            inviteUser = iUserService.getInvitationUser(result);
        }
        List<Map<String, Object>> finalInviteUser = inviteUser;
        return AjaxResult.success("", new HashMap<String, Object>(){{
            put("code", result);
            put("inviteUsers", finalInviteUser);
        }});
    }

    @PostMapping("/invitation")
    @ApiOperation(value="填写邀请码")
    public AjaxResult<Object> invitation (@Validated @RequestBody InvitationValidate invitationVO) {
        Integer userId = LikeFrontThreadLocal.getUserId();
        iUserService.invitation(invitationVO.getCode(), userId );
        return AjaxResult.success("邀请码使用成功！");
    }
}
