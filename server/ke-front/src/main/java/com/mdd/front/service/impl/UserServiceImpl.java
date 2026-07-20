package com.mdd.front.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mdd.common.config.GlobalConfig;
import com.mdd.common.entity.UserCheckIn;
import com.mdd.common.entity.user.User;
import com.mdd.common.entity.user.UserAuth;
import com.mdd.common.enums.ClientEnum;
import com.mdd.common.enums.NoticeEnum;
import com.mdd.common.enums.UserEnum;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.UserCheckInMapper;
import com.mdd.common.mapper.user.UserAuthMapper;
import com.mdd.common.mapper.user.UserMapper;
import com.mdd.common.plugin.notice.NoticeCheck;
import com.mdd.common.plugin.wechat.WxMnpDriver;
import com.mdd.common.util.*;
import com.mdd.front.LikeFrontThreadLocal;
import com.mdd.front.service.IUserService;
import com.mdd.front.validate.users.*;
import com.mdd.front.vo.user.UserCenterVo;
import com.mdd.front.vo.user.UserCheckInListVO;
import com.mdd.front.vo.user.UserInfoVo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements IUserService {

    @Resource
    UserMapper userMapper;

    @Resource
    UserAuthMapper userAuthMapper;

    /**
     * 个人中心
     *
     * @author fzr
     * @param userId 用户ID
     * @return UserCenterVo
     */
    @Override
    public UserCenterVo center(Integer userId, Integer terminal) {
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .select("id,sn,sex,account,nickname,real_name,avatar,mobile,create_time,is_new_user,user_money,password,vip_expired,vip_down_count")
                .eq("id", userId).isNull("delete_time")
                .last("limit 1"));

        UserCenterVo vo = new UserCenterVo();
        BeanUtils.copyProperties(user, vo);
        if (user.getAvatar().isEmpty()) {
            String avatar = ConfigUtils.get("user", "defaultAvatar", "");
            vo.setAvatar(UrlUtils.toAbsoluteUrl(avatar));
        } else {
            vo.setAvatar(UrlUtils.toAbsoluteUrl(user.getAvatar()));
        }

//        vo.setIsAuth(false);
//        if (terminal.equals(ClientEnum.OA.getCode()) || terminal.equals(ClientEnum.MNP.getCode())  || terminal.equals(ClientEnum.H5.getCode())) {
        UserAuth userAuth = userAuthMapper.selectOne(new QueryWrapper<UserAuth>()
                    .select("id,openid,terminal")
                    .eq("user_id", userId)
                    //.eq("terminal", terminal)
                    .last("limit 1"));
        vo.setIsAuth(userAuth != null);
        //}

        // 是否有设置登录密码
        vo.setHasPassword(StringUtils.isNotBlank(user.getPassword()));
        vo.setCreateTime(TimeUtils.timestampToDate(user.getCreateTime()));
        vo.setSex(UserEnum.getSexDesc(user.getSex()));
        return vo;
    }

    /**
     * 个人信息
     *
     * @author fzr
     * @param userId 用户ID
     * @return UserInfoVo
     */
    @Override
    public UserInfoVo info(Integer userId) {
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .select("id,sn,sex,account,password,nickname,real_name,`vip_expired`, `vip_down_count`, avatar,mobile,create_time,user_money")
                .eq("id", userId)
                .last("limit 1"));

        UserAuth userAuth = userAuthMapper.selectOne(new QueryWrapper<UserAuth>()
                .select("id,openid")
                .eq("user_id", userId)
                .last("limit 1"));

        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(user, vo);
        vo.setHasPassword(!user.getPassword().isEmpty());
        vo.setHasAuth(userAuth != null);
        vo.setVersion(GlobalConfig.version);
        user.setVipExpired( (int)(user.getVipExpired()+1 - TimeUtils.timestamp())/ (3600*24));
        vo.setVipExpired(user.getVipExpired() <= 0 ? 0 : user.getVipExpired());
        vo.setSex(UserEnum.getSexDesc(user.getSex()));
        vo.setCreateTime(TimeUtils.timestampToDate(user.getCreateTime()));

        if (!user.getAvatar().isEmpty()) {
            vo.setAvatar(UrlUtils.toAbsoluteUrl(user.getAvatar()));
        } else {
            String avatar = ConfigUtils.get("user", "defaultAvatar", "");
            vo.setAvatar(UrlUtils.toAbsoluteUrl(avatar));
        }

        return vo;
    }

    /**
     * 编辑信息
     *
     * @author fzr
     * @param updateValidate 参数
     * @param userId 用户ID
     */
    @Override
    public void setInfo(UserUpdateValidate updateValidate, Integer userId) {
        String field = updateValidate.getField();
        String value = updateValidate.getValue();

        switch (field) {
            case "avatar":
                User avatarUser = new User();
                avatarUser.setId(userId);
                avatarUser.setAvatar(UrlUtils.toRelativeUrl(value));
                avatarUser.setUpdateTime(System.currentTimeMillis() / 1000);
                userMapper.updateById(avatarUser);
                break;
            case "account":
                User usernameUser = userMapper.selectOne(new QueryWrapper<User>()
                        .select("id,account")
                        .eq("account", value)
                        .isNull("delete_time")
                        .last("limit 1"));

                if (StringUtils.isNotNull(usernameUser) && !usernameUser.getId().equals(userId)) {
                    throw new OperateException("账号已被使用!");
                }

                if (StringUtils.isNotNull(usernameUser) && usernameUser.getAccount().equals(value)) {
                    throw new OperateException("新账号与旧账号一致,修改失败!");
                }

                User u = new User();
                u.setId(userId);
                u.setAccount(value);
                u.setUpdateTime(System.currentTimeMillis() / 1000);
                userMapper.updateById(u);
                break;
            case "nickname":
                User nicknameUser = new User();
                nicknameUser.setId(userId);
                nicknameUser.setNickname(value);
                nicknameUser.setUpdateTime(System.currentTimeMillis() / 1000);
                userMapper.updateById(nicknameUser);
                break;
            case "sex":
                User sexUser = new User();
                sexUser.setId(userId);
                sexUser.setSex(Integer.parseInt(value));
                sexUser.setUpdateTime(System.currentTimeMillis() / 1000);
                userMapper.updateById(sexUser);
                break;
            default:
                throw new OperateException("不被支持的类型");
        }
    }

    /**
     * 修改密码
     *
     * @author fzr
     * @param password 新密码
     * @param userId 用户ID
     */
    @Override
    public void changePwd(String password, String oldPassword, Integer userId) {
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .select("id,password")
                .eq("id", userId)
                .isNull("delete_time")
                .last("limit 1"));

        Assert.notNull(user, "用户不存在");

        if (!user.getPassword().isEmpty()) {
            Assert.notNull(oldPassword, "oldPassword参数缺失");
            String oldPwd = ToolUtils.makePassword(oldPassword.trim());
            if (!oldPwd.equals(user.getPassword())) {
                throw new OperateException("原密码不正确!");
            }
        }

        String pwd  = ToolUtils.makePassword(password.trim());

        User u = new User();
        u.setId(userId);
        u.setPassword(pwd);
        u.setUpdateTime(System.currentTimeMillis() / 1000);
        userMapper.updateById(u);
    }

    /**
     * 忘记密码
     *
     * @author fzr
     * @param password 新密码
     * @param mobile 手机号
     * @param code 验证码
     */
    @Override
    public void forgotPwd(String password, String mobile, String code) {
        // 校验验证码
        int sceneCode = NoticeEnum.FIND_LOGIN_PASSWORD_CAPTCHA.getCode();
        if (!NoticeCheck.verify(sceneCode, code, mobile)) {
            throw new OperateException("验证码错误!");
        }

        // 查询手机号
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .select("id,account,mobile,is_disable")
                .isNull("delete_time")
                .eq("mobile", mobile)
                .last("limit 1"));

        // 验证账号
        com.baomidou.mybatisplus.core.toolkit.Assert.notNull(user, "账号不存在!");

//        String salt = ToolUtils.randomString(5);
        String pwd  = ToolUtils.makePassword(password.trim());

        // 更新密码
        user.setPassword(pwd);
        user.setUpdateTime(System.currentTimeMillis() / 1000);
        userMapper.updateById(user);
    }

    /**
     * 绑定手机
     *
     * @author fzr
     * @param mobileValidate 参数
     * @param userId 用户ID
     */
    @Override
    public void bindMobile(UserPhoneBindValidate mobileValidate, Integer userId) {
//        String type   = mobileValidate.getType();
        String mobile = mobileValidate.getMobile();
//        String code   = mobileValidate.getCode().toLowerCase();

        // 校验验证码
//        int sceneCode = type.equals("bind") ? NoticeEnum.BIND_MOBILE_CAPTCHA.getCode() : NoticeEnum.CHANGE_MOBILE_CAPTCHA.getCode() ;
//        if (!NoticeCheck.verify(sceneCode, code, mobile)) {
//            throw new OperateException("验证码错误!");
//        }

        User user = userMapper.selectOne(new QueryWrapper<User>()
                .select("id,account,mobile")
                .eq("mobile", mobile)
                .isNull("delete_time")
                .last("limit 1"));

        if (StringUtils.isNotNull(user) && !user.getId().equals(userId)) {
            throw new OperateException("手机号已被其它账号绑定!");
        }

        User u = new User();
        u.setId(userId);
        u.setMobile(mobile);
        u.setUpdateTime(System.currentTimeMillis() / 1000);
        userMapper.updateById(u);
    }

    /**
     * 微信手机号
     *
     * @author fzr
     * @param code 获取手机号的Code
     */
    @Override
    public void mnpMobile(String code) {
        Map<String, String> config = ConfigUtils.get("mnp_setting");
        WxMaService wxMaService = new WxMaServiceImpl();
        WxMaDefaultConfigImpl wxConfig = new WxMaDefaultConfigImpl();
        wxConfig.setSecret(config.getOrDefault("app_secret", ""));
        wxConfig.setAppid(config.getOrDefault("app_id", ""));
        wxMaService.setWxMaConfig(wxConfig);

        try {
            WxMaPhoneNumberInfo wxMaPhoneNumberInfo = wxMaService.getUserService().getNewPhoneNoInfo(code);

            Integer userId = LikeFrontThreadLocal.getUserId();

            User userCheck = userMapper.selectOne(new QueryWrapper<User>()
                    .select("id,account,mobile")
                    .eq("mobile", wxMaPhoneNumberInfo.getPhoneNumber())
                    .isNull("delete_time")
                    .last("limit 1"));

            if (StringUtils.isNotNull(userCheck) && !userCheck.getId().equals(userId)) {
                throw new OperateException("手机号已被其它账号绑定!");
            }

            User user = new User();
            user.setId(userId);
            user.setMobile(wxMaPhoneNumberInfo.getPhoneNumber());
            user.setUpdateTime(System.currentTimeMillis() / 1000);
            userMapper.updateById(user);
        } catch (WxErrorException e) {
            throw new OperateException(e.getError().getErrorCode() + ", " + e.getError().getErrorMsg());
        }
    }


    /**
     * 更新新用户昵称头像等信息
     *
     * @param newUserUpdateValidate 参数
     * @param userId 用户id
     */
    @Override
    public void updateNewUserInfo(NewUserUpdateValidate newUserUpdateValidate, Integer userId) {
        User user = new User();
        user.setId(userId);
        user.setNickname(newUserUpdateValidate.getNickname());
        user.setAvatar(UrlUtils.toRelativeUrl(newUserUpdateValidate.getAvatar()));
        user.setIsNewUser(0);
        user.setUpdateTime(System.currentTimeMillis() / 1000);
        userMapper.updateById(user);
    }

    /**
     * 绑定小程序
     *
     * @param bindMnpValidate 参数
     * @param userId 用户ID
     */
    @Override
    public void bindMnp(UserBindWechatValidate bindMnpValidate, Integer userId) {
        try {
            // 通过code获取微信信息
            String code = bindMnpValidate.getCode();
            WxMaService wxMaService = WxMnpDriver.mnp();
            WxMaJscode2SessionResult sessionResult = wxMaService.getUserService().getSessionInfo(code);
            String openId = sessionResult.getOpenid();
            String uniId = sessionResult.getUnionid();
            String unionId = uniId == null ? "0" : uniId;

            // 授权校验,未授权创建授权，已授权返回
            bindWechatAuth(openId, unionId, ClientEnum.MNP.getCode(), userId);

        } catch (WxErrorException e) {
            throw new OperateException(e.getError().getErrorCode() + ", " + e.getError().getErrorMsg());
        }
    }

    /**
     * 绑定公众号
     *
     * @param bindOaValidate 参数
     * @param userId 用户ID
     */
    @Override
    public void bindOa(UserBindWechatValidate bindOaValidate, Integer userId) {
        try {
            // 通过code获取微信信息
            WxMpService wxMpService = WxMnpDriver.oa();
            WxOAuth2AccessToken wxOAuth2AccessToken = wxMpService.getOAuth2Service().getAccessToken(bindOaValidate.getCode());
            String uniId = wxOAuth2AccessToken.getUnionId();
            String openId  = wxOAuth2AccessToken.getOpenId();
            String unionId = uniId == null ? "0" : uniId;
            Integer terminal = LikeFrontThreadLocal.getTerminal();
//            String key = bindOaValidate.getKey();

            // 授权校验,未授权创建授权，已授权返回
            bindWechatAuth(openId, unionId, terminal, userId);

        } catch (WxErrorException e) {
            throw new OperateException(e.getError().getErrorCode() + ", " + e.getError().getErrorMsg());
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordValidate passwordValidate) {
        String code = passwordValidate.getCode();
        String mobile = passwordValidate.getMobile();
        String password = passwordValidate.getPassword();
        // 校验验证码
        int sceneCode = NoticeEnum.FIND_LOGIN_PASSWORD_CAPTCHA.getCode();
        if (!NoticeCheck.verify(sceneCode, code, mobile)) {
            throw new OperateException("验证码错误!");
        }

        // 查询手机号
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .select("id,account,mobile,is_disable")
                .isNull("delete_time")
                .eq("mobile", mobile)
                .last("limit 1"));

        // 验证账号
        com.baomidou.mybatisplus.core.toolkit.Assert.notNull(user, "账号不存在!");

        String pwd  = ToolUtils.makePassword(password.trim());

        // 更新密码
        user.setPassword(pwd);
        user.setUpdateTime(System.currentTimeMillis() / 1000);
        userMapper.updateById(user);
    }
    @Resource
    UserCheckInMapper userCheckInMapper;

    @Override
    @Transactional
    public void checkIn(Integer userId) {
        //先判断一次(常规路径给出友好提示); 真正防并发依赖 (userid,check_date) 唯一索引

        int count = Math.toIntExact(userCheckInMapper.selectCount(new QueryWrapper<UserCheckIn>()
                .eq("userid", userId)
                .apply("TO_DAYS(create_time) = TO_DAYS(NOW())")
        ));
        if (count > 0) {
            throw new OperateException("今天已经签到过了");
        }

        UserCheckIn entity = new UserCheckIn();
        entity.setUserid(userId);
        entity.setCreateTime(new Date());
        entity.setReward(new BigDecimal("10.0"));
        try {
            userCheckInMapper.insert(entity);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            //并发下第二个请求会命中 (userid, create_time) 唯一索引冲突
            throw new OperateException("今天已经签到过了");
        }
        userMapper.updateUserMoneyAdd(userId, 10, 10);
    }

    @Override
    public List<UserCheckInListVO> checkInList(Integer userId, String month) {
        QueryWrapper<UserCheckIn> querWrapper = new QueryWrapper<UserCheckIn>()
                .eq("userid", userId)
                .apply("create_time LIKE CONCAT('" + month + "','%')");
        List<UserCheckIn> list = userCheckInMapper.selectList(querWrapper);
        List<UserCheckInListVO> result = new ArrayList<>();
        for (UserCheckIn checkIn : list) {
            UserCheckInListVO vo = new UserCheckInListVO();
            BeanUtils.copyProperties(checkIn, vo);
            vo.setCreateTime(TimeUtils.formatChinaDate(checkIn.getCreateTime(), "yyyy-MM-dd"));
            result.add(vo);
        }

        return result;
    }

    @Override
    public String getInvitationCode(Integer userId){
        User user = userMapper.selectById(userId);
        if (user != null) {
            if (user.getInvitationCode() != null && !user.getInvitationCode().isEmpty()) {
                return user.getInvitationCode();
            }
            if (user.getTotalRechargeAmount().doubleValue() > 0) {
                //生成长度为10的随机字符串
                do {
                    String invitationCode = ToolUtils.randomString(10);
                    int count = Math.toIntExact(userMapper.selectCount(new QueryWrapper<User>()
                            .eq("invitation_code", invitationCode)
                            .isNotNull("invitation_code")
                            .last("limit 1")));
                    if (count == 0) {
                        user.setInvitationCode(invitationCode);
                        break;
                    }
                }while (true);

                userMapper.updateById(user);
                return user.getInvitationCode();
            }
        }
        return "";
    }

    @Override
    public List<Map<String, Object>> getInvitationUser(String code){
        List<User> list = userMapper.selectList(new QueryWrapper<User>().eq("parent_invitation_code", code).orderByAsc("invitation_time").last("limit 10"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (User item : list) {
            Map<String, Object> userMap = new java.util.HashMap<>();
            userMap.put("id", item.getId());
            userMap.put("user", item.getAccount());
            userMap.put("nickname", item.getNickname());
            userMap.put("avatar", UrlUtils.toAbsoluteUrl(item.getAvatar()));

            result.add(userMap);
        }

        return result;
    }

    @Override
    public void invitation(String code, Integer userId){
        User user = userMapper.selectById(userId);
        if (user != null) {
            if (user.getParentInvitationCode() != null && !user.getParentInvitationCode().isEmpty()) {
                throw new OperateException("您已经填写过邀请码了");
            }
            if (user.getInvitationCode() != null && user.getInvitationCode().equals(code)) {
                throw new OperateException("不能填写自己的邀请码");
            }
            User parentUser = userMapper.selectOne(new QueryWrapper<User>()
                    .eq("invitation_code", code)
                    .isNotNull("invitation_code")
                    .last("limit 1"));
            if (parentUser == null) {
                throw new OperateException("邀请码不存在");
            }
            user.setParentInvitationCode(code);
            user.setInvitationTime(System.currentTimeMillis()/1000);
            userMapper.updateById(user);
        } else {
            throw new OperateException("用户不存在");
        }
    }

    /**
     * 绑定微信授权
     *
     * @param openId openId
     * @param unionId unionId
     * @param terminal 客户端端
     * @param userId 用户ID
     */
    public void bindWechatAuth(String openId, String unionId, Integer terminal, Integer userId) {
        // 授权表中查找授权记录
        UserAuth userAuthOpenId = userAuthMapper.selectOne(new QueryWrapper<UserAuth>()
                .eq("openid", openId)
                .last("limit 1"));

        if (userAuthOpenId != null) {
            // 该微信已绑定
            throw new OperateException("该微信已绑定");
        }

        // 已有授权，返回已绑定微信。 没有授权，绑定微信
        if (!StringUtils.isBlank(unionId)) {
            UserAuth userAuthUnionId = userAuthMapper.selectOne(new QueryWrapper<UserAuth>()
                    .eq("unionid", unionId)
                    .last("limit 1"));

            if (userAuthUnionId != null && !userId.equals(userAuthUnionId.getUserId())) {
                // 该微信已绑定
                throw new OperateException("该微信已绑定");
            }
        }

        if (StringUtils.isNotNull(unionId)) {
            if (!unionId.equals("0")) {
                //在用unionid找记录，防止生成两个账号，同个unionid的问题
                UserAuth userAuth = userAuthMapper.selectOne(new QueryWrapper<UserAuth>()
                        .eq("unionid", unionId)
                        .last("limit 1"));
                if (StringUtils.isNotNull(userAuth)) {
                    throw new OperateException("该微信已被绑定");
                }
            }
        }

        // 记录微信授权
        UserAuth authModel = new UserAuth();
        authModel.setUserId(userId);
        authModel.setUnionid(unionId);
        authModel.setOpenid(openId);
        authModel.setTerminal(terminal);
        authModel.setCreateTime(System.currentTimeMillis() / 1000);
        authModel.setUpdateTime(System.currentTimeMillis() / 1000);
        userAuthMapper.insert(authModel);
    }

}
