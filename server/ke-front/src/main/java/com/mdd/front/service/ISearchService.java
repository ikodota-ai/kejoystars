package com.mdd.front.service;

import com.alibaba.fastjson.JSONObject;
import com.mdd.common.core.PageResult;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.vo.SearchListedVo;

/**
 * 搜索接口类
 */
public interface ISearchService {

    /**
     * 热搜
     *
     * @author fzr
     */
    JSONObject hotLists();


    /**
     * couple列表
     *
     * @author 十八子
     * @param pageValidate 分页参数
     * @return PageResult<CoupleListedVo>
     */
    PageResult<SearchListedVo> list(PageValidate pageValidate, String k);

}
