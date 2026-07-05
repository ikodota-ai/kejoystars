package com.mdd.admin.service.k;

import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.validate.k.StarInstagramCreateValidate;
import com.mdd.admin.validate.k.StarInstagramUpdateValidate;
import com.mdd.admin.validate.k.StarInstagramSearchValidate;
import com.mdd.admin.vo.k.StarInstagramListedVo;
import com.mdd.admin.vo.k.StarInstagramDetailVo;
import com.mdd.common.core.PageResult;

/**
 * 【请填写功能名称】服务接口类
 * @author LikeAdmin
 */
public interface IStarInstagramService {

    /**
     * 【请填写功能名称】列表
     *
     * @author LikeAdmin
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<StarInstagramListedVo>
     */
    PageResult<StarInstagramListedVo> list(PageValidate pageValidate, StarInstagramSearchValidate searchValidate);

    /**
     * 【请填写功能名称】详情
     *
     * @author LikeAdmin
     * @param id 主键ID
     * @return StarInstagramDetailVo
     */
    StarInstagramDetailVo detail(Integer id);

    /**
     * 【请填写功能名称】新增
     *
     * @author LikeAdmin
     * @param createValidate 参数
     */
    void add(StarInstagramCreateValidate createValidate);

    /**
     * 【请填写功能名称】编辑
     *
     * @author LikeAdmin
     * @param updateValidate 参数
     */
    void edit(StarInstagramUpdateValidate updateValidate);

    /**
     * 【请填写功能名称】删除
     *
     * @author LikeAdmin
     * @param id 主键ID
     */
    void del(String id);

}
