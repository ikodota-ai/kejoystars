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
    }

}
