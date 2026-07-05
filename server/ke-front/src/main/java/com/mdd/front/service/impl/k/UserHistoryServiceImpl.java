package com.mdd.front.service.impl.k;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.k.UserHistory;
import com.mdd.common.mapper.k.StarInfoMapper;
import com.mdd.common.mapper.k.UserHistoryMapper;
import com.mdd.common.util.TimeUtils;
import com.mdd.front.service.k.IUserHistoryService;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.validate.k.UserHistoryCreateValidate;
import com.mdd.front.validate.k.UserHistorySearchValidate;
import com.mdd.front.validate.k.UserHistoryUpdateValidate;
import com.mdd.front.vo.k.UserHistoryDetailVo;
import com.mdd.front.vo.k.UserHistoryListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

/**
 * 【请填写功能名称】实现类
 * @author LikeAdmin
 */
@Service
public class UserHistoryServiceImpl implements IUserHistoryService {
        
    @Resource
    UserHistoryMapper userHistoryMapper;

    @Resource
    StarInfoMapper starInfoMapper;
    /**
     * 【请填写功能名称】列表
     *
     * @author LikeAdmin
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<UserHistoryListedVo>
     */
    @Override
    public PageResult<UserHistoryListedVo> list(PageValidate pageValidate, UserHistorySearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<UserHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");

        userHistoryMapper.setSearch(queryWrapper, searchValidate, new String[]{
            "=:userid:int",
            "=:type:str",
            "=:sMId@s_m_id:int",
        });

        IPage<UserHistory> iPage = userHistoryMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<UserHistoryListedVo> list = new LinkedList<>();
        for(UserHistory item : iPage.getRecords()) {
            UserHistoryListedVo vo = new UserHistoryListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 【请填写功能名称】详情
     *
     * @author LikeAdmin
     * @param id 主键参数
     * @return UserHistory
     */
    @Override
    public UserHistoryDetailVo detail(Integer id) {
        UserHistory model = userHistoryMapper.selectOne(
                new QueryWrapper<UserHistory>()
                    .eq("id", id)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在");

        UserHistoryDetailVo vo = new UserHistoryDetailVo();
        BeanUtils.copyProperties(model, vo);
        return vo;
    }

    /**
     * 【请填写功能名称】新增
     *
     * @author LikeAdmin
     * @param createValidate 参数
     */
    @Override
    @Transactional
    public void add(UserHistoryCreateValidate createValidate) {
        UserHistory model = new UserHistory();
        model.setUserid(createValidate.getUserid());
        model.setType(createValidate.getType());
        model.setSMId(createValidate.getSMId());
        model.setCreateTime(System.currentTimeMillis() / 1000);
        userHistoryMapper.insert(model);
       //增加明星收藏数
        if (createValidate.getType().equals("S")) {
            starInfoMapper.historyCount(createValidate.getSMId(), 1);
        }
    }

    /**
     * 【请填写功能名称】编辑
     *
     * @author LikeAdmin
     * @param updateValidate 参数
     */
    @Override
    public void edit(UserHistoryUpdateValidate updateValidate) {
        UserHistory model = userHistoryMapper.selectOne(
                new QueryWrapper<UserHistory>()
                    .eq("id",  updateValidate.getId())
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");

        model.setId(updateValidate.getId());
        model.setUserid(updateValidate.getUserid());
        model.setType(updateValidate.getType());
        model.setSMId(updateValidate.getSMId());
        userHistoryMapper.updateById(model);
    }

    /**
     * 【请填写功能名称】删除
     *
     * @author LikeAdmin
     * @param id 主键ID
     */
    @Override
    public void del(Integer id, Integer userId) {
        UserHistory model = userHistoryMapper.selectOne(
                new QueryWrapper<UserHistory>()
                    .eq("id", id)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");

        int result = userHistoryMapper.delete(new QueryWrapper<UserHistory>().eq("id", id).eq("userid", userId));
        if (result == 0) {
            throw new RuntimeException("取消失败");
        }
    }
    @Override
    @Transactional
    public void remove(UserHistoryCreateValidate createValidate) {
        int result = userHistoryMapper.delete(new QueryWrapper<UserHistory>()
                .eq("s_m_id", createValidate.getSMId())
                .eq("userid", createValidate.getUserid())
                .eq("type", createValidate.getType()));
        //减去明星收藏数
        if (createValidate.getType().equals("M")) {
            starInfoMapper.historyCount(createValidate.getSMId(), -1);
        }
        if (result == 0) {
            throw new RuntimeException("取消失败");
        }
    }
}
