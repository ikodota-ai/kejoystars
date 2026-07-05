package com.mdd.common.mapper.setting;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.mdd.common.core.basics.IBaseMapper;
import com.mdd.common.entity.setting.HotSearch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 热门搜索Mapper
 */
@Mapper
public interface HotSearchMapper extends IBaseMapper<HotSearch> {

}
