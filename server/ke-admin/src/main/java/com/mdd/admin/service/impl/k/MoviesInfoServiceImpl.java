package com.mdd.admin.service.impl.k;

import cn.dev33.satoken.stp.StpUtil;
import cn.jiguang.sdk.api.PushApi;
import cn.jiguang.sdk.bean.push.batch.BatchPushParam;
import cn.jiguang.sdk.bean.push.batch.BatchPushSendParam;
import cn.jiguang.sdk.bean.push.batch.BatchPushSendResult;
import cn.jiguang.sdk.bean.push.message.notification.NotificationMessage;
import cn.jiguang.sdk.enums.platform.Platform;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mdd.admin.service.IJPushJiGuangService;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.service.k.IMoviesInfoService;
import com.mdd.admin.validate.k.MoviesInfoCreateValidate;
import com.mdd.admin.validate.k.MoviesInfoUpdateValidate;
import com.mdd.admin.validate.k.MoviesInfoSearchValidate;
import com.mdd.admin.vo.k.MoviesInfoListedVo;
import com.mdd.admin.vo.k.MoviesInfoDetailVo;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.admin.Admin;
import com.mdd.common.entity.k.MoviesInfo;
import com.mdd.common.entity.k.UserHistory;
import com.mdd.common.entity.user.User;
import com.mdd.common.mapper.admin.AdminMapper;
import com.mdd.common.mapper.k.MoviesInfoMapper;
import com.mdd.common.mapper.k.UserHistoryMapper;
import com.mdd.common.mapper.user.UserMapper;
import com.mdd.common.util.TimeUtils;
import com.mdd.common.util.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * 【请填写功能名称】列表
     *
     * @author 十八子
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MoviesInfoListedVo>
     */
    @Override
    public PageResult<MoviesInfoListedVo> list(PageValidate pageValidate, MoviesInfoSearchValidate searchValidate) {
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
            "=:actorList@actor_list:str",
            "=:status:str",
        });

        IPage<MoviesInfo> iPage = moviesInfoMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<MoviesInfoListedVo> list = new LinkedList<>();
        for(MoviesInfo item : iPage.getRecords()) {
            MoviesInfoListedVo vo = new MoviesInfoListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setImage(UrlUtils.toAbsoluteUrl(item.getImage()));
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
//            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setUpTime( item.getUpTime() );
            vo.setUpUser(item.getUpUser() );
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
        BeanUtils.copyProperties(model, vo);
        vo.setImage(UrlUtils.toAbsoluteUrl(model.getImage()));
        return vo;
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
        if (createValidate.getMovieEnName() != null){
            model.setMovieEnName(createValidate.getMovieEnName().trim());
        }
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

        moviesInfoMapper.insert(model);
        //取得当前影视演员关注列表，并发通知给关注过用户发通知，告知当前明星有新剧更亲
        if (model.getActorList() != null && !model.getActorList().isEmpty()) {
            //取得关注用户列表,actorList结构是：[{"sid": 0, "star": "詹妮弗·比尔斯", "cosplay": "Bette Porter"}] sid是明星id
            String actorIds = JSONArray.parse(model.getActorList()).stream().map(
                    jsonObject -> ((JSONObject) jsonObject).getInteger("sid")).map(Object::toString).collect(Collectors.joining(",")
            );
            List<UserHistory> actorList = userHistoryMapper.selectList(new QueryWrapper<UserHistory>().eq("type", "S").in("s_m_id", actorIds));
            if (actorList != null && !actorList.isEmpty()) {
                String msg = "您关注的明星有新参演影视！";
                List<String> registrationIds = new ArrayList<>();
                for (UserHistory userHistory : actorList) {
                    User user = userMapper.selectById(userHistory.getUserid());

                    registrationIds.add(user.getRegistrationId());
                    if (registrationIds.size() >= 1000){
                        //发送通知
                        jPushJiGuangService.sendNotice(msg, msg, registrationIds);
                        registrationIds.clear();
                    }
                }
                if (!registrationIds.isEmpty()) {
                    //发送通知
                    jPushJiGuangService.sendNotice(msg, msg, registrationIds);
                }
            }
        }
    }
    @Autowired
    UserHistoryMapper userHistoryMapper;
    @Resource
    UserMapper userMapper;
    @Autowired
    IJPushJiGuangService jPushJiGuangService;
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
        if (updateValidate.getMovieEnName() != null){
            model.setMovieEnName(updateValidate.getMovieEnName().trim());
        }
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
    public void del(List<Integer> ids) {
//        MoviesInfo model = moviesInfoMapper.selectOne(
//                new QueryWrapper<MoviesInfo>()
//                    .eq("id", id)
//                    .last("limit 1"));
//
//        Assert.notNull(model, "数据不存在!");

        moviesInfoMapper.delete(new QueryWrapper<MoviesInfo>().in("id", ids));
    }

}
