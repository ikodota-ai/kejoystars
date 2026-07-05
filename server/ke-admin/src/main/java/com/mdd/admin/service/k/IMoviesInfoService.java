package com.mdd.admin.service.k;

import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.validate.k.MoviesInfoCreateValidate;
import com.mdd.admin.validate.k.MoviesInfoUpdateValidate;
import com.mdd.admin.validate.k.MoviesInfoSearchValidate;
import com.mdd.admin.vo.k.MoviesInfoListedVo;
import com.mdd.admin.vo.k.MoviesInfoDetailVo;
import com.mdd.common.core.PageResult;

import java.util.List;

/**
 * 【请填写功能名称】服务接口类
 * @author 十八子
 */
public interface IMoviesInfoService {

    /**
     * 【请填写功能名称】列表
     *
     * @author 十八子
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MoviesInfoListedVo>
     */
    PageResult<MoviesInfoListedVo> list(PageValidate pageValidate, MoviesInfoSearchValidate searchValidate);

    /**
     * 【请填写功能名称】详情
     *
     * @author 十八子
     * @param id 主键ID
     * @return MoviesInfoDetailVo
     */
    MoviesInfoDetailVo detail(Integer id);

    /**
     * 【请填写功能名称】新增
     *
     * @author 十八子
     * @param createValidate 参数
     */
    void add(MoviesInfoCreateValidate createValidate);

    /**
     * 【请填写功能名称】编辑
     *
     * @author 十八子
     * @param updateValidate 参数
     */
    void edit(MoviesInfoUpdateValidate updateValidate);

    /**
     * 【请填写功能名称】删除
     *
     * @author 十八子
     * @param ids 主键ID
     */
    void del(List<Integer> ids);

}
