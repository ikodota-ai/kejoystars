package com.mdd.admin.service.impl.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.mdd.admin.service.IAppVersionService;
import com.mdd.admin.validate.app.AppVersionCreateValidate;
import com.mdd.admin.validate.app.AppVersionSearchValidate;
import com.mdd.admin.validate.app.AppVersionUpdateValidate;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.vo.app.AppVersionDetailVo;
import com.mdd.admin.vo.app.AppVersionListedVo;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.app.AppVersion;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.app.AppVersionMapper;
import com.mdd.common.util.TimeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * APP版本服务实现类
 */
@Service
public class AppVersionServiceImpl implements IAppVersionService {

    @Resource
    AppVersionMapper appVersionMapper;

    @Override
    public PageResult<AppVersionListedVo> list(PageValidate pageValidate, AppVersionSearchValidate searchValidate) {
        Integer pageNo   = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        MPJQueryWrapper<AppVersion> mpjQueryWrapper = new MPJQueryWrapper<AppVersion>()
                .selectAll(AppVersion.class)
                .isNull("t.delete_time")
                .orderByDesc(Arrays.asList("t.platform", "t.version_code"));

        appVersionMapper.setSearch(mpjQueryWrapper, searchValidate, new String[]{
                "=:platform@t.platform:str",
                "=:channel@t.channel:str",
                "like:versionName@t.version_name:str",
                "=:status@t.status:str"
        });

        IPage<AppVersionListedVo> iPage = appVersionMapper.selectJoinPage(
                new Page<>(pageNo, pageSize),
                AppVersionListedVo.class,
                mpjQueryWrapper);

        for (AppVersionListedVo vo : iPage.getRecords()) {
            vo.setCreateTime(TimeUtils.timestampToDate(vo.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(vo.getUpdateTime()));
            String publishTime = vo.getPublishTime();
            vo.setPublishTime(publishTime != null && !publishTime.equals("") && !publishTime.equals("0")
                    ? TimeUtils.timestampToDate(publishTime) : "");
        }

        return PageResult.iPageHandle(iPage);
    }

    @Override
    public AppVersionDetailVo detail(Integer id) {
        AppVersion model = appVersionMapper.selectOne(
                new QueryWrapper<AppVersion>()
                        .eq("id", id)
                        .isNull("delete_time"));

        Assert.notNull(model, "版本不存在!");

        AppVersionDetailVo vo = new AppVersionDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setPublishTime(model.getPublishTime() != null && model.getPublishTime() > 0
                ? TimeUtils.timestampToDate(model.getPublishTime()) : "");
        vo.setCreateTime(TimeUtils.timestampToDate(model.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(model.getUpdateTime()));
        return vo;
    }

    @Override
    public void add(AppVersionCreateValidate createValidate) {
        this.checkVersionCodeUnique(createValidate.getPlatform(), createValidate.getChannel(),
                createValidate.getVersionCode(), null);

        AppVersion model = new AppVersion();
        BeanUtils.copyProperties(createValidate, model);
        long now = TimeUtils.timestamp();
        model.setCreateTime(now);
        model.setUpdateTime(now);
        if ("online".equals(model.getStatus())) {
            model.setPublishTime(now);
        } else {
            model.setPublishTime(0L);
        }
        appVersionMapper.insert(model);

        if ("online".equals(model.getStatus())) {
            this.offlineOthers(model);
        }
    }

    @Override
    public void edit(AppVersionUpdateValidate updateValidate) {
        AppVersion model = appVersionMapper.selectOne(
                new QueryWrapper<AppVersion>()
                        .eq("id", updateValidate.getId())
                        .isNull("delete_time"));

        Assert.notNull(model, "版本不存在!");

        this.checkVersionCodeUnique(updateValidate.getPlatform(), updateValidate.getChannel(),
                updateValidate.getVersionCode(), updateValidate.getId());

        String beforeStatus = model.getStatus();
        BeanUtils.copyProperties(updateValidate, model);
        model.setUpdateTime(TimeUtils.timestamp());
        if ("online".equals(model.getStatus()) && !"online".equals(beforeStatus)) {
            model.setPublishTime(TimeUtils.timestamp());
        }
        appVersionMapper.updateById(model);

        if ("online".equals(model.getStatus())) {
            this.offlineOthers(model);
        }
    }

    @Override
    public void del(Integer id) {
        AppVersion model = appVersionMapper.selectOne(
                new QueryWrapper<AppVersion>()
                        .select("id")
                        .eq("id", id)
                        .isNull("delete_time"));

        Assert.notNull(model, "版本不存在!");
        model.setDeleteTime(TimeUtils.timestamp());
        appVersionMapper.updateById(model);
    }

    @Override
    public void publish(Integer id, String status) {
        if (!Arrays.asList("draft", "online", "offline").contains(status)) {
            throw new OperateException("状态不合法");
        }

        AppVersion model = appVersionMapper.selectOne(
                new QueryWrapper<AppVersion>()
                        .eq("id", id)
                        .isNull("delete_time"));

        Assert.notNull(model, "版本不存在!");

        model.setStatus(status);
        model.setUpdateTime(TimeUtils.timestamp());
        if ("online".equals(status) && (model.getPublishTime() == null || model.getPublishTime() <= 0)) {
            model.setPublishTime(TimeUtils.timestamp());
        }
        appVersionMapper.updateById(model);

        if ("online".equals(status)) {
            this.offlineOthers(model);
        }
    }

    /**
     * 同平台同通道下, 保证只有一个版本处于 online 状态
     */
    private void offlineOthers(AppVersion current) {
        AppVersion update = new AppVersion();
        update.setStatus("offline");
        update.setUpdateTime(TimeUtils.timestamp());
        appVersionMapper.update(update, new QueryWrapper<AppVersion>()
                .eq("platform", current.getPlatform())
                .eq("channel", current.getChannel())
                .eq("status", "online")
                .ne("id", current.getId())
                .isNull("delete_time"));
    }

    /**
     * 校验 平台+通道+版本号 唯一
     */
    private void checkVersionCodeUnique(String platform, String channel, Integer versionCode, Integer excludeId) {
        QueryWrapper<AppVersion> wrapper = new QueryWrapper<AppVersion>()
                .eq("platform", platform)
                .eq("channel", channel == null ? "production" : channel)
                .eq("version_code", versionCode)
                .isNull("delete_time");
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        if (appVersionMapper.selectCount(wrapper) > 0) {
            throw new OperateException("同平台同通道下版本号已存在");
        }
    }

}
