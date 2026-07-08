package com.mdd.admin.service.impl;

import com.mdd.admin.service.ISettingUserService;
import com.mdd.admin.validate.setting.SettingUserValidate;
import com.mdd.admin.vo.setting.SettingUserVo;
import com.mdd.common.util.ConfigUtils;
import com.mdd.common.util.UrlUtils;
import org.springframework.stereotype.Service;

/**
 * 用户设置服务实现类
 */
@Service
public class SettingUserServiceImpl implements ISettingUserService {

    /**
     * 用户设置详情
     *
     * @author fzr
     * @return SettingUserVo
     */
    @Override
    public SettingUserVo getConfig() {
        String defaultAvatar = ConfigUtils.get("default_image", "user_avatar", "");
        SettingUserVo vo = new SettingUserVo();
        vo.setDefaultAvatar(UrlUtils.toAdminAbsoluteUrl(defaultAvatar));
        String freeVipDays = ConfigUtils.get("user", "free_vip_days", "0");
        vo.setFreeVipDays(Integer.parseInt(freeVipDays));
        vo.setRegisterTip(ConfigUtils.get("user", "register_tip", "恭喜你注册成功，作为首次注册用户你已享有{days}天免费会员，可下载IG,X图片"));
        return vo;
    }

    /**
     * 用户设置保存
     *
     * @author fzr
     * @param userValidate 参数
     */
    @Override
    public void setConfig(SettingUserValidate userValidate) {
        ConfigUtils.set("default_image", "user_avatar", UrlUtils.toRelativeUrl(userValidate.getDefaultAvatar()));
        Integer freeVipDays = userValidate.getFreeVipDays() == null ? 0 : userValidate.getFreeVipDays();
        ConfigUtils.set("user", "free_vip_days", String.valueOf(freeVipDays));
        ConfigUtils.set("user", "register_tip", userValidate.getRegisterTip() == null ? "" : userValidate.getRegisterTip());
    }

}
