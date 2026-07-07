package com.mdd.admin.service;

import com.mdd.admin.validate.app.AppVersionCreateValidate;
import com.mdd.admin.validate.app.AppVersionSearchValidate;
import com.mdd.admin.validate.app.AppVersionUpdateValidate;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.vo.app.AppVersionDetailVo;
import com.mdd.admin.vo.app.AppVersionListedVo;
import com.mdd.common.core.PageResult;

/**
 * APP版本服务接口类
 */
public interface IAppVersionService {

    /**
     * APP版本列表
     */
    PageResult<AppVersionListedVo> list(PageValidate pageValidate, AppVersionSearchValidate searchValidate);

    /**
     * APP版本详情
     */
    AppVersionDetailVo detail(Integer id);

    /**
     * APP版本新增
     */
    void add(AppVersionCreateValidate createValidate);

    /**
     * APP版本编辑
     */
    void edit(AppVersionUpdateValidate updateValidate);

    /**
     * APP版本删除
     */
    void del(Integer id);

    /**
     * APP版本发布/上下线
     */
    void publish(Integer id, String status);

}
