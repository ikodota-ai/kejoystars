package com.mdd.front.service;

import com.mdd.common.entity.UserCheckIn;
import com.mdd.front.validate.users.*;
import com.mdd.front.vo.user.UserCenterVo;
import com.mdd.front.vo.user.UserCheckInListVO;
import com.mdd.front.vo.user.UserInfoVo;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口类
 */
public interface IUserService {

    /**
     * 个人中心
     *
     * @author fzr
     * @param userId 用户ID
     * @param terminal 用户终端
     * @return UserCenterVo
     */
    UserCenterVo center(Integer userId, Integer terminal);

    /**
     * 个人信息
     *
     * @author fzr
     * @param userId 用户ID
     * @return UserInfoVo
     */
    UserInfoVo info(Integer userId);

    /**
     * 编辑信息
     *
     * @author fzr
     * @param updateValidate 参数
     * @param userId 用户ID
     */
    void setInfo(UserUpdateValidate updateValidate, Integer userId);

    /**
     * 修改密码
     *
     * @author fzr
     * @param password 新密码
     * @param oldPassword 旧密码
     * @param userId 用户ID
     */
    void changePwd(String password, String oldPassword, Integer userId);

    /**
     * 忘记密码
     *
     * @author fzr
     * @param password 新密码
     * @param mobile 手机号
     * @param code 验证码
     */
    void forgotPwd(String password, String mobile, String code);

    /**
     * 绑定手机
     *
     * @author fzr
     * @param mobileValidate 参数
     * @param userId 用户ID
     */
    void bindMobile(UserPhoneBindValidate mobileValidate, Integer userId);

    /**
     * 微信手机
     *
     * @author fzr
     * @param code 获取手机号的Code
     */
    void mnpMobile(String code);

    /**
     * 更新新用户信息
     *
     * @param newUserUpdateValidate 参数
     * @param userId 用户id
     */
    void updateNewUserInfo(NewUserUpdateValidate newUserUpdateValidate, Integer userId);

    /**
     * 绑定微信小程序
     *
     * @param bindMnpValidate 参数
     * @param userId 用户ID
     */
    void bindMnp(UserBindWechatValidate bindMnpValidate, Integer userId);

    /**
     * 绑定微信公众号
     *
     * @param bindOaValidate 参数
     * @param userId 用户ID
     */
    void bindOa(UserBindWechatValidate bindOaValidate, Integer userId);

    /**
     * @notes 重置登录密码
     * @return bool
     * @author damonyuan
     */
    void resetPassword(ResetPasswordValidate passwordValidate);

    /**
     * 用户签到
     *
     * @param userId
     */
    void checkIn(Integer userId);

    /**
     * 签到亡灵
     * @param userId
     */
    List<UserCheckInListVO> checkInList(Integer userId, String month);

    /**
     * 获取用户的邀请码
     * @param userId
     * @return
     */
    String getInvitationCode(Integer userId);

    /**
     * 获取用户的邀请码邀请的人
     * @param code
     * @return
     */
    List<Map<String, Object>> getInvitationUser(String code);

    /**
     * 使用邀请码
     * @param code
     * @param userId
     */
    void invitation(String code, Integer userId);
}
