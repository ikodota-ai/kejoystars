package com.mdd.front.service.impl.k;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.admin.Admin;
import com.mdd.common.entity.k.MoviesInfo;
import com.mdd.common.entity.k.StarInfo;
import com.mdd.common.entity.k.UserHistory;
import com.mdd.common.mapper.admin.AdminMapper;
import com.mdd.common.mapper.k.MoviesInfoMapper;
import com.mdd.common.mapper.k.StarInfoMapper;
import com.mdd.common.mapper.k.UserHistoryMapper;
import com.mdd.common.util.TimeUtils;
import com.mdd.common.util.UrlUtils;
import com.mdd.front.LikeFrontThreadLocal;
import com.mdd.front.service.k.IMoviesInfoService;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.validate.k.MoviesInfoCreateValidate;
import com.mdd.front.validate.k.MoviesInfoSearchValidate;
import com.mdd.front.validate.k.MoviesInfoUpdateValidate;
import com.mdd.front.vo.k.MoviesInfoDetailVo;
import com.mdd.front.vo.k.MoviesInfoListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * 【请填写功能名称】实现类
 * @author 十八子
 */
@Service
public class MoviesInfoServiceImpl implements IMoviesInfoService {
        
    @Resource
    MoviesInfoMapper moviesInfoMapper;

    @Resource
    AdminMapper systemAuthAdminMapper;

    @Resource
    UserHistoryMapper userHistoryMapper;

    @Resource
    StarInfoMapper starInfoMapper;

    /**
     * 【请填写功能名称】列表
     *
     * @author 十八子
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MoviesInfoListedVo>
     */
    @Override
    public PageResult<MoviesInfoListedVo> list(Integer userid, PageValidate pageValidate, MoviesInfoSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<MoviesInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (searchValidate.getMovieName()!= null && !searchValidate.getMovieName().isEmpty())
            queryWrapper.like("movie_name", searchValidate.getMovieName()).or().like("movie_en_name", searchValidate.getMovieName());

        moviesInfoMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "=:image:str",
                "=:company:str",
                "=:playPlatform@play_platform:str",
                "=:quantity:int",
                "=:startPlayTime@start_play_time:str",
                "=:updateWeek@update_week:str",
                "=:updateTime@update_time:str",
                "=:cp:str",
                "=:content:str",
                "=:country:int",
                "=:movieType@movie_type:str",
                "=:status:str",
                "json:actorList@actor_list:sid:starId"
        });
        if(searchValidate.getCollect() != null && searchValidate.getCollect() == 1 ){
            //只查询关注收藏 用left join
            queryWrapper.inSql("id",
                    "SELECT s_m_id FROM `la_user_history` WHERE userid = " + userid + " AND type = 'M'");
        }

        IPage<MoviesInfo> iPage = moviesInfoMapper.selectPage(new Page<>(page, limit), queryWrapper);
        Integer userId = LikeFrontThreadLocal.getUserId();

        List<MoviesInfoListedVo> list = new LinkedList<>();
        for(MoviesInfo item : iPage.getRecords()) {
            MoviesInfoListedVo vo = new MoviesInfoListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setImage(UrlUtils.toAbsoluteUrl(item.getImage()));

            if (userId > 0) {
//                查看是否收藏
                vo.setIsHistory(Math.toIntExact(userHistoryMapper.selectCount(new QueryWrapper<UserHistory>()
                        .eq("userid", userId)
                        .eq("type", "M")
                        .eq("s_m_id", item.getId())
                        .last("limit 1"))));
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
     * @return MoviesInfo
     */
    @Override
    public MoviesInfoDetailVo detail(Integer id) {
        MoviesInfo model = moviesInfoMapper.selectOne(
                new QueryWrapper<MoviesInfo>()
                    .eq("id", id)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在");

        MoviesInfoDetailVo vo = new MoviesInfoDetailVo();
        Integer userId = LikeFrontThreadLocal.getUserId();
        if (userId > 0) {
            // 查看是否收藏
            vo.setIsHistory( Math.toIntExact(userHistoryMapper.selectCount(new QueryWrapper<UserHistory>()
                    .eq("userid", userId)
                    .eq("type", "M")
                    .eq("s_m_id", model.getId())
                    .last("limit 1"))));
        }

        BeanUtils.copyProperties(model, vo);
        vo.setImage(UrlUtils.toAbsoluteUrl(model.getImage()));
        if (model.getUpdateWeek() != null) {
            vo.setUpdateWeek(convertWeekText(model.getUpdateWeek()));
        }

        JSONArray actors = JSONArray.parse(vo.getActorList());
        //通过actors里的sid取得明星头像资料
        for (int i = 0; i < actors.size(); i++) {
            Long starId = actors.getJSONObject(i).getLong("sid");
            // 假设有StarInfoMapper和StarInfo实体
            StarInfo star = starInfoMapper.selectOne(new QueryWrapper<StarInfo>().eq("id", starId).last("limit 1"));
            if (star != null) {
                // 假设VO有addActorAvatar方法
                actors.getJSONObject(i).put("avatar", UrlUtils.toAbsoluteUrl(star.getAvatar()));
            } else {
                //未找到明星信息，移除数据
                actors.remove(i);
            }
        }
        vo.setActorList( actors.toJSONString() );
        return vo;
    }
    /**
     * 将周数字转换为文字
     * @param weekValue 1-7 或 0
     * @return 周一/周二...周日
     */
    private String convertWeekText(String weekValue) {
        switch (weekValue) {
            case "1": return "周一";
            case "2": return "周二";
            case "3": return "周三";
            case "4": return "周四";
            case "5": return "周五";
            case "6": return "周六";
            case "7": return "周日";
            case "0": return "周日";
            default: return weekValue;
        }
    }

    /**
     * 【请填写功能名称】新增
     *
     * @author 十八子
     * @param createValidate 参数
     */
    @Override
    public void add(MoviesInfoCreateValidate createValidate) {
        MoviesInfo model = new MoviesInfo();
        model.setMovieName(createValidate.getMovieName());
        model.setMovieType(createValidate.getMovieType());
        model.setImage(UrlUtils.toRelativeUrl(createValidate.getImage()));
        model.setCompany(createValidate.getCompany());
        model.setPlayPlatform(createValidate.getPlayPlatform());
        model.setQuantity(createValidate.getQuantity());
        model.setStartPlayTime(createValidate.getStartPlayTime());
        model.setUpdateWeek(createValidate.getUpdateWeek());
        model.setUpdateTime(createValidate.getUpdateTime());
        model.setCp(createValidate.getCp());
        model.setContent(createValidate.getContent());
        model.setCountry(createValidate.getCountry());
        model.setActorList(createValidate.getActorList());
        model.setStatus(createValidate.getStatus());
        model.setCreateTime(System.currentTimeMillis() / 1000);

        Object id = StpUtil.getLoginId();

        Admin adminUser = systemAuthAdminMapper.selectOne(
                new QueryWrapper<Admin>()
                        .select("id,name,disable")
                        .eq("id", Integer.parseInt(id.toString()))
                        .isNull("delete_time")
                        .last("limit 1"));
        if (adminUser != null) {
            model.setCreateUser(adminUser.getName());
        }

        moviesInfoMapper.insert(model);
    }

    /**
     * 【请填写功能名称】编辑
     *
     * @author 十八子
     * @param updateValidate 参数
     */
    @Override
    public void edit(MoviesInfoUpdateValidate updateValidate) {
        MoviesInfo model = moviesInfoMapper.selectOne(
                new QueryWrapper<MoviesInfo>()
                    .eq("id",  updateValidate.getId())
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");

        model.setId(updateValidate.getId());
        model.setMovieName(updateValidate.getMovieName());
        model.setMovieType(updateValidate.getMovieType());
        model.setImage(UrlUtils.toRelativeUrl(updateValidate.getImage()));
        model.setCompany(updateValidate.getCompany());
        model.setPlayPlatform(updateValidate.getPlayPlatform());
        model.setQuantity(updateValidate.getQuantity());
        model.setStartPlayTime(updateValidate.getStartPlayTime());
        model.setUpdateWeek(updateValidate.getUpdateWeek());
        model.setUpdateTime(updateValidate.getUpdateTime());
        model.setCp(updateValidate.getCp());
        model.setContent(updateValidate.getContent());
        model.setCountry(updateValidate.getCountry());
        model.setActorList(updateValidate.getActorList());
        model.setStatus(updateValidate.getStatus());

        Object id = StpUtil.getLoginId();

        Admin adminUser = systemAuthAdminMapper.selectOne(
                new QueryWrapper<Admin>()
                        .select("id,name,disable")
                        .eq("id", Integer.parseInt(id.toString()))
                        .isNull("delete_time")
                        .last("limit 1"));
        if (adminUser != null) {
            model.setUpUser(adminUser.getName());
        }

        moviesInfoMapper.updateById(model);
    }

    /**
     * 【请填写功能名称】删除
     *
     * @author 十八子
     * @param id 主键ID
     */
    @Override
    public void del(Integer id) {
        MoviesInfo model = moviesInfoMapper.selectOne(
                new QueryWrapper<MoviesInfo>()
                    .eq("id", id)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");

        moviesInfoMapper.delete(new QueryWrapper<MoviesInfo>().eq("id", id));
    }

}
