package com.mdd.admin.service.impl.k;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.service.k.ICoupleService;
import com.mdd.admin.validate.k.CoupleCreateValidate;
import com.mdd.admin.validate.k.CoupleUpdateValidate;
import com.mdd.admin.validate.k.CoupleSearchValidate;
import com.mdd.admin.vo.k.CoupleListedVo;
import com.mdd.admin.vo.k.CoupleDetailVo;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.admin.Admin;
import com.mdd.common.entity.k.Couple;
import com.mdd.common.entity.k.MoviesInfo;
import com.mdd.common.entity.k.StarInfo;
import com.mdd.common.mapper.admin.AdminMapper;
import com.mdd.common.mapper.k.CoupleMapper;
import com.mdd.common.mapper.k.MoviesInfoMapper;
import com.mdd.common.mapper.k.StarInfoMapper;
import com.mdd.common.util.TimeUtils;
import com.mdd.common.util.UrlUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

/**
 * couple实现类
 * @author 十八子
 */
@Service
public class CoupleServiceImpl implements ICoupleService {

    @Resource
    AdminMapper systemAuthAdminMapper;

    @Resource
    CoupleMapper coupleMapper;

    @Resource
    StarInfoMapper starInfoMapper;

    @Resource
    MoviesInfoMapper moviesInfoMapper;

    /**
     * 【请填写功能名称】列表
     *
     * @author 十八子
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<CoupleListedVo>
     */
    @Override
    public PageResult<CoupleListedVo> list(PageValidate pageValidate, CoupleSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<Couple> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("name");

        coupleMapper.setSearch(queryWrapper, searchValidate, new String[]{
            "like:name:str",
            "like:en_name:str",
            "=:country:long",
            "=:image:str",
            "=:star:str",
            "=:movies:str",
            "=:historyCount@history_count:int",
            "=:createUser@create_user:str",
            "=:updateUser@update_user:str",
        });

        IPage<Couple> iPage = coupleMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<CoupleListedVo> list = new LinkedList<>();
        for(Couple item : iPage.getRecords()) {
            CoupleListedVo vo = new CoupleListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setImage(UrlUtils.toAbsoluteUrl(item.getImage()));
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            if (item.getUpdateTime() != null) {
                vo.setUpdateTime(TimeUtils.formatChinaDate(item.getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
            }
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 【请填写功能名称】详情
     *
     * @author 十八子
     * @param id 主键参数
     * @return Couple
     */
    @Override
    public CoupleDetailVo detail(Integer id) {
        Couple model = coupleMapper.selectOne(
                new QueryWrapper<Couple>()
                    .eq("id", id)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在");

        CoupleDetailVo vo = new CoupleDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setImage(UrlUtils.toAbsoluteUrl(model.getImage()));
        if (!model.getStar().isEmpty()) {
            JSONArray stars = JSONArray.parse(model.getStar());
            List<Map<String, String>> list = new ArrayList<>();
            for (Object obj : stars) {
                StarInfo sInfo = starInfoMapper.selectOne(new QueryWrapper<StarInfo>().eq("id", obj.toString()).last("limit 1"));
                if (sInfo != null) {
                    Map<String, String> map = new HashMap<String, String>(){{
                        put("id", sInfo.getId().toString());
                        put("name", sInfo.getName());
                        put("birthday", sInfo.getBirthday()==null ? "" : TimeUtils.formatChinaDate(sInfo.getBirthday(),"yyyy-MM-dd"));
                        put("constellation", sInfo.getConstellation());

                    }};
                    list.add(map);
                }
            }
            vo.setStarInfo(list);
        }
        if (!model.getMovies().isEmpty()) {
            JSONArray movies = JSONArray.parse(model.getMovies());
            List<Map<String, String>> list = new ArrayList<>();
            for (Object obj : movies) {
                MoviesInfo mInfo = moviesInfoMapper.selectOne(new QueryWrapper<MoviesInfo>().eq("id", obj.toString()).last("limit 1"));
                if (mInfo != null) {
                    Map<String, String> map = new HashMap<String, String>(){{
                        put("id", mInfo.getId().toString());
                        put("movieName", mInfo.getMovieName());
                        put("image", UrlUtils.toAbsoluteUrl(mInfo.getImage()));
                    }};
                    list.add(map);
                }
            }
            vo.setMoviesInfo(list);
        }
        return vo;
    }

    /**
     * 【请填写功能名称】新增
     *
     * @author 十八子
     * @param createValidate 参数
     */
    @Override
    public void add(CoupleCreateValidate createValidate) {
        Couple model = new Couple();
        model.setName(createValidate.getName());
        model.setCountry(createValidate.getCountry());
        model.setImage(UrlUtils.toRelativeUrl(createValidate.getImage()));
        model.setStar(createValidate.getStar());
        model.setMovies(createValidate.getMovies());
        model.setHistoryCount(createValidate.getHistoryCount());
        model.setCreateTime(System.currentTimeMillis() / 1000);

        Object id = StpUtil.getLoginId();
        // 将name是中文的，转换成拼音，并存储到en_name字段中
        model.setEnName(cn.hutool.extra.pinyin.PinyinUtil.getPinyin(model.getName(), ""));

        Admin adminUser = systemAuthAdminMapper.selectOne(
                new QueryWrapper<Admin>()
                        .select("id,name,disable")
                        .eq("id", Integer.parseInt(id.toString()))
                        .isNull("delete_time")
                        .last("limit 1"));
        if (adminUser != null) {
            model.setCreateUser(adminUser.getName());
        }

        coupleMapper.insert(model);
    }

    /**
     * 【请填写功能名称】编辑
     *
     * @author 十八子
     * @param updateValidate 参数
     */
    @Override
    public void edit(CoupleUpdateValidate updateValidate) {
        Couple model = coupleMapper.selectOne(
                new QueryWrapper<Couple>()
                    .eq("id",  updateValidate.getId())
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");

        model.setId(updateValidate.getId());
        model.setName(updateValidate.getName());
        // 将name是中文的，转换成拼音，并存储到en_name字段中
        model.setEnName(PinyinUtil.getPinyin(model.getName(), ""));
        model.setCountry(updateValidate.getCountry());
        model.setImage(UrlUtils.toRelativeUrl(updateValidate.getImage()));
        model.setStar(updateValidate.getStar());
        model.setMovies(updateValidate.getMovies());
        model.setHistoryCount(updateValidate.getHistoryCount());

        Object id = StpUtil.getLoginId();

        Admin adminUser = systemAuthAdminMapper.selectOne(
                new QueryWrapper<Admin>()
                        .select("id,name,disable")
                        .eq("id", Integer.parseInt(id.toString()))
                        .isNull("delete_time")
                        .last("limit 1"));
        if (adminUser != null) {
            model.setUpdateUser(adminUser.getName());
        }

        coupleMapper.updateById(model);
    }

    /**
     * 【请填写功能名称】删除
     *
     * @author 十八子
     * @param ids 主键ID
     */
    @Override
    public void del(List<Integer> ids) {
//        Couple model = coupleMapper.selectOne(
//                new QueryWrapper<Couple>()
//                    .eq("id", id)
//                    .last("limit 1"));
//
//        Assert.notNull(model, "数据不存在!");

        coupleMapper.delete(new QueryWrapper<Couple>().in("id", ids));
    }

}
