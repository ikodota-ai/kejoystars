package com.mdd.front.service;

import com.mdd.front.vo.app.AppVersionCheckVo;

/**
 * APP版本服务接口类
 */
public interface IAppVersionService {

    /**
     * 检查更新
     *
     * @param platform    平台: android/ios
     * @param channel     通道
     * @param versionCode 客户端当前版本号
     * @param deviceId    设备标识(用于灰度), 可空
     * @return AppVersionCheckVo
     */
    AppVersionCheckVo check(String platform, String channel, Integer versionCode, String deviceId);

}
