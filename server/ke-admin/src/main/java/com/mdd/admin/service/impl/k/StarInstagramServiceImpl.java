package com.mdd.admin.service.impl.k;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.service.k.IStarInstagramService;
import com.mdd.admin.validate.k.StarInstagramCreateValidate;
import com.mdd.admin.validate.k.StarInstagramUpdateValidate;
import com.mdd.admin.validate.k.StarInstagramSearchValidate;
import com.mdd.admin.vo.k.StarInstagramListedVo;
import com.mdd.admin.vo.k.StarInstagramDetailVo;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.k.StarInfo;
import com.mdd.common.entity.k.StarInstagram;
import com.mdd.common.mapper.k.StarInfoMapper;
import com.mdd.common.mapper.k.StarInstagramMapper;
import com.mdd.common.util.TimeUtils;
import com.mdd.common.util.UrlUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

/**
 * 【请填写功能名称】实现类
 * @author LikeAdmin
 */
@Service
public class StarInstagramServiceImpl implements IStarInstagramService {
        
    @Resource
    StarInstagramMapper starInstagramMapper;

    @Resource
    StarInfoMapper starInfoMapper;

    /**
     * 【请填写功能名称】列表
     *
     * @author LikeAdmin
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<StarInstagramListedVo>
     */
    @Override
    public PageResult<StarInstagramListedVo> list(PageValidate pageValidate, StarInstagramSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<StarInstagram> queryWrapper = new QueryWrapper<>();
//        queryWrapper.orderByDesc("id");
        queryWrapper.orderByDesc("create_time");

        starInstagramMapper.setSearch(queryWrapper, searchValidate, new String[]{
            "=:starId@star_id:int",
            "=:image:str",
            "=:status:str",
            "=:source:str",
            "=:verifyTime@verify_time:int",
        });

        IPage<StarInstagram> iPage = starInstagramMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<StarInstagramListedVo> list = new LinkedList<>();
        for(StarInstagram item : iPage.getRecords()) {
            StarInstagramListedVo vo = new StarInstagramListedVo();

            BeanUtils.copyProperties(item, vo);
            vo.setImage(UrlUtils.toAbsoluteUrl(item.getImage()));
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            if (vo.getStarId() != null) {
                StarInfo info = starInfoMapper.selectById(vo.getStarId());
                if (info != null) {
                    vo.setStarName(info.getName());
                }
            }

            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 【请填写功能名称】详情
     *
     * @author LikeAdmin
     * @param id 主键参数
     * @return StarInstagram
     */
    @Override
    public StarInstagramDetailVo detail(Integer id) {
        StarInstagram model = starInstagramMapper.selectOne(
                new QueryWrapper<StarInstagram>()
                    .eq("id", id)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在");

        StarInstagramDetailVo vo = new StarInstagramDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setImage(UrlUtils.toAbsoluteUrl(model.getImage()));
        return vo;
    }

    /**
     * 【请填写功能名称】新增
     *
     * @author LikeAdmin
     * @param createValidate 参数
     */
    @Override
    public void add(StarInstagramCreateValidate createValidate) {
        StarInstagram model = new StarInstagram();
        model.setStarId(createValidate.getStarId());
        model.setImage(UrlUtils.toRelativeUrl(createValidate.getImage()));
        model.setStatus(createValidate.getStatus());
        model.setSource(createValidate.getSource());
        model.setVerifyTime(createValidate.getVerifyTime());
        model.setCreateTime(System.currentTimeMillis() / 1000);
        starInstagramMapper.insert(model);
    }

    /**
     * 【请填写功能名称】编辑
     *
     * @author LikeAdmin
     * @param updateValidate 参数
     */
    @Override
    public void edit(StarInstagramUpdateValidate updateValidate) {
        starInstagramMapper.updateBatchStatus(updateValidate.getId(), updateValidate.getStatus());
    }

    /**
     * 【请填写功能名称】删除
     *
     * @author LikeAdmin
     * @param id 主键ID
     */
    @Override
    public void del(String id) {
        starInstagramMapper.deleteBatchIds(id);
    }

}
