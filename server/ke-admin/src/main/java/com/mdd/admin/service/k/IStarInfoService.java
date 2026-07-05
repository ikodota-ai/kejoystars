package com.mdd.admin.service.k;

import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.validate.k.StarInfoCreateValidate;
import com.mdd.admin.validate.k.StarInfoUpdateValidate;
import com.mdd.admin.validate.k.StarInfoSearchValidate;
import com.mdd.admin.vo.k.StarInfoListedVo;
import com.mdd.admin.vo.k.StarInfoDetailVo;
import com.mdd.common.core.PageResult;

import java.util.List;

/**
 * 明星资料服务接口类
 * @author 十八子
 */
public interface IStarInfoService {

    /**
     * 明星资料列表
     *
     * @author 十八子
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<StarInfoListedVo>
     */
    PageResult<StarInfoListedVo> list(PageValidate pageValidate, StarInfoSearchValidate searchValidate);

    /**
     * 通过明星名字查找明星资料
     *
     * @param name
     * @return
     */
    StarInfoDetailVo detailByName(String name);
    /**
     * 明星资料详情
     *
     * @author 十八子
     * @param id 主键ID
     * @return StarInfoDetailVo
     */
    StarInfoDetailVo detail(Integer id);

    /**
     * 明星资料新增
     *
     * @author 十八子
     * @param createValidate 参数
     */
    int add(StarInfoCreateValidate createValidate);

    /**
     * 明星资料编辑
     *
     * @author 十八子
     * @param updateValidate 参数
     */
    void edit(StarInfoUpdateValidate updateValidate);

    /**
     * 明星资料删除
     *
     * @author 十八子
     * @param idList 主键ID
     */
    void del(List<Integer> idList);

}
