package com.mdd.admin.service;

import com.alibaba.fastjson2.JSONArray;
import com.mdd.admin.validate.setting.SettingWebsiteValidate;
import com.mdd.admin.vo.setting.SettingWebsiteVo;

import java.util.Map;

/**
 * 网站信息服务接口类
 */
public interface ISettingWebsiteService {

    /**
     * 获取网站信息
     *
     * @author fzr
     * @return SettingWebsiteVo
     */
    SettingWebsiteVo getWebsite();

    /**
     * 保存网站信息
     *
     * @author fzr
     * @param websiteValidate 参数
     */
    void setWebsite(SettingWebsiteValidate websiteValidate);

    /**
     * 取得充值VIP类型
     *
     * @return
     */
    public JSONArray getRechargeOption();

    /**
     * 设置充值VIP类型
     *
     * @param option
     */
    public void setRechargeOption(JSONArray option);

}
