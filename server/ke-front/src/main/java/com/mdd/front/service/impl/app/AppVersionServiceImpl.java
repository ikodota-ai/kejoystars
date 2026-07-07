package com.mdd.front.service.impl.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mdd.common.entity.app.AppVersion;
import com.mdd.common.mapper.app.AppVersionMapper;
import com.mdd.common.util.StringUtils;
import com.mdd.front.service.IAppVersionService;
import com.mdd.front.vo.app.AppVersionCheckVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * APP版本服务实现类
 */
@Service
public class AppVersionServiceImpl implements IAppVersionService {

    @Resource
    AppVersionMapper appVersionMapper;

    @Override
    public AppVersionCheckVo check(String platform, String channel, Integer versionCode, String deviceId) {
        AppVersionCheckVo vo = new AppVersionCheckVo();

        if (StringUtils.isNull(channel) || channel.trim().isEmpty()) {
            channel = "production";
        }
        if (versionCode == null || versionCode < 0) {
            versionCode = 0;
        }

        // 取当前平台+通道下已上线且已到发布时间的最高版本
        AppVersion latest = appVersionMapper.selectOne(
                new QueryWrapper<AppVersion>()
                        .eq("platform", platform)
                        .eq("channel", channel)
                        .eq("status", "online")
                        .le("publish_time", System.currentTimeMillis() / 1000)
                        .isNull("delete_time")
                        .orderByDesc("version_code")
                        .last("limit 1"));

        if (latest == null || latest.getVersionCode() == null || latest.getVersionCode() <= versionCode) {
            return vo;
        }

        // 灰度: hash(deviceId) % 100 < grayPercent 才提示更新
        int grayPercent = latest.getGrayPercent() == null ? 100 : latest.getGrayPercent();
        if (grayPercent < 100) {
            int bucket = 100;
            if (StringUtils.isNotNull(deviceId) && !deviceId.trim().isEmpty()) {
                bucket = Math.abs(deviceId.hashCode() % 100);
            }
            if (bucket >= grayPercent) {
                return vo;
            }
        }

        boolean isForce = (latest.getIsForce() != null && latest.getIsForce() == 1)
                || (latest.getMinVersionCode() != null && versionCode < latest.getMinVersionCode());

        vo.setHasUpdate(true);
        vo.setIsForce(isForce);
        vo.setVersionName(latest.getVersionName());
        vo.setVersionCode(latest.getVersionCode());
        vo.setInstallUrl(StringUtils.isNotNull(latest.getInstallUrl()) ? latest.getInstallUrl() : "");
        vo.setDownloadUrl(StringUtils.isNotNull(latest.getDownloadUrl()) ? latest.getDownloadUrl() : "");
        vo.setPackageSize(latest.getPackageSize() == null ? 0L : latest.getPackageSize());
        vo.setPackageMd5(StringUtils.isNotNull(latest.getPackageMd5()) ? latest.getPackageMd5() : "");
        vo.setReleaseNote(StringUtils.isNotNull(latest.getReleaseNote()) ? latest.getReleaseNote() : "");
        vo.setPublishTime(latest.getPublishTime() == null ? 0L : latest.getPublishTime());
        return vo;
    }

}
