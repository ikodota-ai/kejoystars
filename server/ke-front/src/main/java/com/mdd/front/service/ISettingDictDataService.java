package com.mdd.front.service;

import com.mdd.common.core.PageResult;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.vo.SettingDictDataVo;

import java.util.List;
import java.util.Map;

/**
 * 字典数据服务接口类
 */
public interface ISettingDictDataService {
    /**
     * 字典数据列表
     *
     * @author fzr
     * @param params 搜索参数
     * @return PageResult<DictDataVo>
     */
    List<SettingDictDataVo> list(Map<String, String> params);

}
