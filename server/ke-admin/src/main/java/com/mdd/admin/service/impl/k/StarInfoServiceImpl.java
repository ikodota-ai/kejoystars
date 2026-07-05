package com.mdd.admin.service.impl.k;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mdd.admin.crontab.CrawlQuartzJob;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.service.k.IStarInfoService;
import com.mdd.admin.validate.k.StarInfoCreateValidate;
import com.mdd.admin.validate.k.StarInfoUpdateValidate;
import com.mdd.admin.validate.k.StarInfoSearchValidate;
import com.mdd.admin.vo.k.StarInfoListedVo;
import com.mdd.admin.vo.k.StarInfoDetailVo;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.admin.Admin;
import com.mdd.common.entity.k.StarInfo;
import com.mdd.common.entity.setting.DictData;
import com.mdd.common.mapper.admin.AdminMapper;
import com.mdd.common.mapper.k.StarInfoMapper;
import com.mdd.common.mapper.setting.DictDataMapper;
import com.mdd.common.util.TimeUtils;
import com.mdd.common.util.UrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

/**
 * 明星资料实现类
 * @author 十八子
 */
@Slf4j
@Service
public class StarInfoServiceImpl implements IStarInfoService {
        
    @Resource
    StarInfoMapper starInfoMapper;

    @Resource
    AdminMapper systemAuthAdminMapper;

    @Resource
    DictDataMapper dictDataMapper;

    @Resource
    CrawlQuartzJob crawlQuartzJob;
    /**
     * 明星资料列表
     *
     * @author 十八子
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<StarInfoListedVo>
     */
    @Override
    public PageResult<StarInfoListedVo> list(PageValidate pageValidate, StarInfoSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<StarInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (searchValidate.getName()!=null && !searchValidate.getName().isEmpty()) {
            queryWrapper.apply(" (LOWER(name) REGEXP LOWER({0}) OR LOWER(en_name) REGEXP LOWER({1}) )",
                    "^\\s?" + searchValidate.getName().trim() + ".*$", "^\\s?" + searchValidate.getName().trim() + ".*$");
        }

        starInfoMapper.setSearch(queryWrapper, searchValidate, new String[]{
            "=:avatar:str",
            "=:country:int",
            "=:birthday:str",
            "=:height:str",
            "=:weight:str",
            "=:constellation:str",
            "=:university:str",
            "=:universityDepartments@university_departments:str",
            "=:x:str",
            "=:twitter:str",
            "=:tiktok:str",
            "=:favorites:int",
            "=:candy:int",
            "=:movies:str",
            "=:createUser@create_user:str",
            "=:updateUser@update_user:str",
        });

        IPage<StarInfo> iPage = starInfoMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<StarInfoListedVo> list = new LinkedList<>();
        for(StarInfo item : iPage.getRecords()) {
            StarInfoListedVo vo = new StarInfoListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setAvatar(UrlUtils.toAbsoluteUrl(item.getAvatar()));
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            if (item.getUpdateTime() != null) {
                vo.setUpdateTime(item.getUpdateTime().toString());
            }
            DictData dictData = dictDataMapper.selectOne(
                    new QueryWrapper<DictData>().eq("value", item.getCountry()).eq("type_id", "6").last("limit 1"));
            if (dictData != null) {
                vo.setCountryName(dictData.getName());
            }

            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    @Override
    public StarInfoDetailVo detailByName(String name) {
        log.info(name);
        StarInfo model = starInfoMapper.selectOne(
                new QueryWrapper<StarInfo>()
                        .eq("name", name)
                        .last("limit 1"));

        if (model != null) {
            StarInfoDetailVo vo = new StarInfoDetailVo();
            BeanUtils.copyProperties(model, vo);
            vo.setAvatar(UrlUtils.toAbsoluteUrl(model.getAvatar()));
            return vo;
        } else {
            return null;
        }
    }
    /**
     * 明星资料详情
     *
     * @author 十八子
     * @param id 主键参数
     * @return StarInfo
     */
    @Override
    public StarInfoDetailVo detail(Integer id) {
        StarInfo model = starInfoMapper.selectOne(
                new QueryWrapper<StarInfo>()
                    .eq("id", id)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在");

        StarInfoDetailVo vo = new StarInfoDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setAvatar(UrlUtils.toAbsoluteUrl(model.getAvatar()));
        return vo;
    }

    /**
     * 明星资料新增
     *
     * @author 十八子
     * @param createValidate 参数
     */
    @Override
    public int add(StarInfoCreateValidate createValidate) {
        StarInfo model = new StarInfo();
        model.setName(createValidate.getName().trim());
        model.setGender( createValidate.getGender() );
        if (createValidate.getEnName() != null){
            model.setEnName(createValidate.getEnName().trim());
        }
        model.setAvatar(UrlUtils.toRelativeUrl(createValidate.getAvatar()));
        model.setCountry(createValidate.getCountry());
        model.setBirthday(createValidate.getBirthday());
        if (createValidate.getBirthday() != null) {
            model.setConstellation(getConstellationByBirthday(TimeUtils.formatChinaDate(createValidate.getBirthday(),"yyyy-MM-dd")));
        }
        model.setBirthplace( createValidate.getBirthplace() );
        model.setHeight(createValidate.getHeight());
        model.setWeight(createValidate.getWeight());
        model.setUniversity(createValidate.getUniversity());
        model.setUniversityDepartments(createValidate.getUniversityDepartments());
        model.setX(createValidate.getX());
        model.setTwitter(createValidate.getTwitter());
        model.setTiktok(createValidate.getTiktok());
        model.setFavorites(createValidate.getFavorites());
        model.setCandy(createValidate.getCandy());
        if (createValidate.getMovies()== null || createValidate.getMovies().isEmpty()){
            model.setMovies("[]");
        } else {
            model.setMovies(createValidate.getMovies());
        }
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setOriginal( createValidate.getOriginal() );
        Object id = 1;//StpUtil.getLoginId();

        Admin adminUser = systemAuthAdminMapper.selectOne(
                new QueryWrapper<Admin>()
                        .select("id,name,disable")
                        .eq("id", Integer.parseInt(id.toString()))
                        .isNull("delete_time")
                        .last("limit 1"));
        if (adminUser != null) {
            model.setCreateUser(adminUser.getName());
        }
        starInfoMapper.insert(model);
        if (model.getX() != null && !model.getX().isEmpty()) {
            crawlQuartzJob.publish("message_channel", new HashMap<String, String>(){{
                put("id", model.getId().toString());
                put("web", "ins");
                put("url", "https://www.instagram.com/" + model.getX());
                put("first", "1");
            }});
        }
        if (model.getTwitter() != null && !model.getTwitter().isEmpty()) {
            crawlQuartzJob.publish("message_channel", new HashMap<String, String>(){{
                put("id", model.getId().toString());
                put("web", "x");
                put("url", "https://x.com/" + model.getTwitter());
                put("first", "1");
            }});
        }

        return model.getId();
    }

    /**
     * 明星资料编辑
     *
     * @author 十八子
     * @param updateValidate 参数
     */
    @Override
    public void edit(StarInfoUpdateValidate updateValidate) {
        StarInfo model = starInfoMapper.selectOne(
                new QueryWrapper<StarInfo>()
                    .eq("id",  updateValidate.getId())
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        StarInfoDetailVo vo = new StarInfoDetailVo();
        BeanUtils.copyProperties(model, vo);

        model.setId(updateValidate.getId());
        if (updateValidate.getName() != null){
            model.setName( updateValidate.getName().trim() );
        }
        if (updateValidate.getEnName() != null){
            model.setEnName(updateValidate.getEnName().trim());
        }
        model.setAvatar(UrlUtils.toRelativeUrl(updateValidate.getAvatar()));
        model.setCountry(updateValidate.getCountry());
        model.setBirthday(updateValidate.getBirthday());
        if (updateValidate.getBirthday() != null) {
            model.setConstellation(getConstellationByBirthday(TimeUtils.formatChinaDate(updateValidate.getBirthday(),"yyyy-MM-dd")));
        }
        model.setHeight(updateValidate.getHeight());
        model.setWeight(updateValidate.getWeight());
        model.setUniversity(updateValidate.getUniversity());
        model.setUniversityDepartments(updateValidate.getUniversityDepartments());
        model.setX(updateValidate.getX());
        model.setTwitter(updateValidate.getTwitter());
        model.setTiktok(updateValidate.getTiktok());
        model.setFavorites(updateValidate.getFavorites());
        model.setCandy(updateValidate.getCandy());
        if (updateValidate.getMovies()!= null && !updateValidate.getMovies().isEmpty()) {
            model.setMovies(updateValidate.getMovies());
        }

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

        starInfoMapper.updateById(model);
        if (model.getX() != null && !model.getX().isEmpty()
               && !model.getX().equals(vo.getX())) {
            crawlQuartzJob.publish("message_channel", new HashMap<String, String>(){{
                put("id", model.getId().toString());
                put("web", "ins");
                put("url", "https://www.instagram.com/" + model.getX());
                put("first", "1");
            }});
        }
        if (model.getTwitter() != null && !model.getTwitter().isEmpty()
               && !model.getTwitter().equals(vo.getTwitter())) {
            crawlQuartzJob.publish("message_channel", new HashMap<String, String>(){{
                put("id", model.getId().toString());
                put("web", "x");
                put("url", "https://x.com/" + model.getTwitter());
                put("first", "1");
            }});
        }
    }

    /**
     * 明星资料删除
     *
     * @author 十八子
     * @param idList 主键ID
     */
    @Override
    public void del(List<Integer> idList) {
//        StarInfo model = starInfoMapper.selectOne(
//                new QueryWrapper<StarInfo>()
//                    .eq("id", id)
//                    .last("limit 1"));
//
//        Assert.notNull(model, "数据不存在!");
        starInfoMapper.delete(new QueryWrapper<StarInfo>().in("id", idList));
    }

    private String getConstellationByBirthday(String birthday) {
        // 假设 birthday 格式为 "yyyy-MM-dd"
        String[] parts = birthday.split("-");
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);
        String[] constellations = {
                "摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座",
                "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"
        };
        int[] days = {20, 19, 21, 21, 21, 22, 23, 23, 23, 24, 23, 22};
        return day < days[month - 1] ? constellations[month - 1] : constellations[month];
    }
}
