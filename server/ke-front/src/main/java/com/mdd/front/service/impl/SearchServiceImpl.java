package com.mdd.front.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.Search;
import com.mdd.common.entity.k.Couple;
import com.mdd.common.entity.setting.HotSearch;
import com.mdd.common.mapper.SearchMapper;
import com.mdd.common.mapper.setting.HotSearchMapper;
import com.mdd.common.util.*;
import com.mdd.front.service.ISearchService;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.vo.SearchListedVo;
import com.mdd.front.vo.k.CoupleListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 首页服务实现类
 */
@Service
public class SearchServiceImpl implements ISearchService {


    @Resource
    HotSearchMapper hotSearchMapper;

    @Resource
    SearchMapper searchMapper;

    @Override
    public JSONObject hotLists() {

        List<HotSearch> hotSearches = hotSearchMapper.selectList(new QueryWrapper<HotSearch>().orderByDesc("sort"));
        JSONObject result = new JSONObject(){{
            put("status", ConfigUtils.get("hot_search", "status", "0"));
            put("data", hotSearches);
        }};

        return result;
    }

    /**
     * 【请填写功能名称】列表
     *
     * @author 十八子
     * @param pageValidate 分页参数
     * @return PageResult<CoupleListedVo>
     */
    @Override
    public PageResult<SearchListedVo> list(PageValidate pageValidate, String k) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<SearchListedVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("name");
        //mybatis-plus union all la_star_info la_movies_info la_couple la_article

        IPage<Search> iPage = searchMapper.selectPage(new Page<>(page, limit), k, k, k, k);
        List<SearchListedVo> list = new LinkedList<>();
        for(Search item : iPage.getRecords()) {
            SearchListedVo vo = new SearchListedVo();
            BeanUtils.copyProperties(item, vo);

            list.add( vo );
        }


        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }
}
