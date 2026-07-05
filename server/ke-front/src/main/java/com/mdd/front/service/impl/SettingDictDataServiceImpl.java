package com.mdd.front.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.setting.DictData;
import com.mdd.common.entity.setting.DictType;
import com.mdd.common.mapper.setting.DictDataMapper;
import com.mdd.common.mapper.setting.DictTypeMapper;
import com.mdd.common.util.StringUtils;
import com.mdd.common.util.TimeUtils;
import com.mdd.front.service.ISettingDictDataService;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.vo.SettingDictDataVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 字典数据服务实现类
 */
@Service
public class SettingDictDataServiceImpl implements ISettingDictDataService {

    @Resource
    DictDataMapper dictDataMapper;

    @Resource
    DictTypeMapper dictTypeMapper;

    /**
     * 字典数据列表
     *
     * @author fzr
     * @param params 搜索参数
     * @return PageResult<DictDataVo>
     */
    @Override
    public List<SettingDictDataVo> list(Map<String, String> params) {
        List<DictData> iPage = dictDataMapper.selectList( new QueryWrapper<DictData>().eq("type_id", params.get("type_id")) );

        List<SettingDictDataVo> list = new LinkedList<>();
        for (DictData dictData : iPage) {
            SettingDictDataVo vo = new SettingDictDataVo();
            BeanUtils.copyProperties(dictData, vo);
            list.add(vo);
        }

        return list;
    }
}
