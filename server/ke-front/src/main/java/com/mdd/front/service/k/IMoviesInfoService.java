package com.mdd.front.service.k;

import com.mdd.common.core.PageResult;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.validate.k.MoviesInfoCreateValidate;
import com.mdd.front.validate.k.MoviesInfoSearchValidate;
import com.mdd.front.validate.k.MoviesInfoUpdateValidate;
import com.mdd.front.vo.k.MoviesInfoDetailVo;
import com.mdd.front.vo.k.MoviesInfoListedVo;

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
    PageResult<MoviesInfoListedVo> list(Integer userid, PageValidate pageValidate, MoviesInfoSearchValidate searchValidate);

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
     * @param id 主键ID
     */
    void del(Integer id);

}
