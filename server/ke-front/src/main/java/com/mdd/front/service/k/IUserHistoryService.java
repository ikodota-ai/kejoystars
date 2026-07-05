package com.mdd.front.service.k;

import com.mdd.common.core.PageResult;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.validate.k.UserHistoryCreateValidate;
import com.mdd.front.validate.k.UserHistorySearchValidate;
import com.mdd.front.validate.k.UserHistoryUpdateValidate;
import com.mdd.front.vo.k.UserHistoryDetailVo;
import com.mdd.front.vo.k.UserHistoryListedVo;

/**
 * 【请填写功能名称】服务接口类
 * @author LikeAdmin
 */
public interface IUserHistoryService {

    /**
     * 【请填写功能名称】列表
     *
     * @author LikeAdmin
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<UserHistoryListedVo>
     */
    PageResult<UserHistoryListedVo> list(PageValidate pageValidate, UserHistorySearchValidate searchValidate);

    /**
     * 【请填写功能名称】详情
     *
     * @author LikeAdmin
     * @param id 主键ID
     * @return UserHistoryDetailVo
     */
    UserHistoryDetailVo detail(Integer id);

    /**
     * 【请填写功能名称】新增
     *
     * @author LikeAdmin
     * @param createValidate 参数
     */
    void add(UserHistoryCreateValidate createValidate);

    /**
     * 【请填写功能名称】编辑
     *
     * @author LikeAdmin
     * @param updateValidate 参数
     */
    void edit(UserHistoryUpdateValidate updateValidate);

    /**
     * 【请填写功能名称】删除
     *
     * @author LikeAdmin
     * @param id 主键ID
     */
    void del(Integer id, Integer userId);

    void remove(UserHistoryCreateValidate createValidate);
}
