package com.mdd.admin.service.k;

import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.validate.k.CoupleCreateValidate;
import com.mdd.admin.validate.k.CoupleUpdateValidate;
import com.mdd.admin.validate.k.CoupleSearchValidate;
import com.mdd.admin.vo.k.CoupleListedVo;
import com.mdd.admin.vo.k.CoupleDetailVo;
import com.mdd.common.core.PageResult;

import java.util.List;

/**
 * couple服务接口类
 * @author 十八子
 */
public interface ICoupleService {

    /**
     * couple列表
     *
     * @author 十八子
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<CoupleListedVo>
     */
    PageResult<CoupleListedVo> list(PageValidate pageValidate, CoupleSearchValidate searchValidate);

    /**
     * couple详情
     *
     * @author 十八子
     * @param id 主键ID
     * @return CoupleDetailVo
     */
    CoupleDetailVo detail(Integer id);

    /**
     * couple新增
     *
     * @author 十八子
     * @param createValidate 参数
     */
    void add(CoupleCreateValidate createValidate);

    /**
     * couple编辑
     *
     * @author 十八子
     * @param updateValidate 参数
     */
    void edit(CoupleUpdateValidate updateValidate);

    /**
     * couple删除
     *
     * @author 十八子
     * @param ids 主键ID
     */
    void del(List<Integer> ids);

}
