package com.mdd.front.service.impl.k;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.k.MoviesInfo;
import com.mdd.common.entity.k.StarGiftLog;
import com.mdd.common.entity.k.StarInfo;
import com.mdd.common.entity.k.UserHistory;
import com.mdd.common.entity.setting.DictData;
import com.mdd.common.entity.user.User;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.k.MoviesInfoMapper;
import com.mdd.common.mapper.k.StarGiftLogMapper;
import com.mdd.common.mapper.k.StarInfoMapper;
import com.mdd.common.mapper.k.UserHistoryMapper;
import com.mdd.common.mapper.setting.DictDataMapper;
import com.mdd.common.mapper.user.UserMapper;
import com.mdd.common.util.TimeUtils;
import com.mdd.common.util.UrlUtils;
import com.mdd.front.LikeFrontThreadLocal;
import com.mdd.front.service.k.IStarInfoService;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.validate.k.StarInfoSearchValidate;
import com.mdd.front.vo.k.MoviesInfoDetailVo;
import com.mdd.front.vo.k.StarGiftLogListedVo;
import com.mdd.front.vo.k.StarInfoDetailVo;
import com.mdd.front.vo.k.StarInfoListedVo;
import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

/**
 * 明星资料实现类
 * @author 十八子
 */
@Service
public class StarInfoServiceImpl implements IStarInfoService {
        
    @Resource
    StarInfoMapper starInfoMapper;

    @Resource
    DictDataMapper dictDataMapper;

    @Resource
    UserMapper userMapper;

    @Resource
    MoviesInfoMapper moviesInfoMapper;

    @Resource
    UserHistoryMapper userHistoryMapper;

    @Resource
    StarGiftLogMapper starGiftLogMapper;
    /**
     * 明星资料列表
     *
     * @author 十八子
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<StarInfoListedVo>
     */
    @Override
    public PageResult<StarInfoListedVo> list(Integer userid, PageValidate pageValidate, StarInfoSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<StarInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("candy");
        if (searchValidate.getName()!=null && !searchValidate.getName().isEmpty()) {
            queryWrapper.apply(" (name REGEXP {0} OR LOWER(en_name) REGEXP LOWER({1}) )",
                    "^" + searchValidate.getName() + ".*$", "^" + searchValidate.getName() + ".*$");
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

        if(searchValidate.getCollect() != null && searchValidate.getCollect() == 1 ){
            //只查询关注收藏 用left join
            queryWrapper.inSql("id",
                    "SELECT s_m_id FROM `la_user_history` WHERE userid = " + userid + " AND type = 'S'");
        }

        IPage<StarInfo> iPage = starInfoMapper.selectPage(new Page<>(page, limit), queryWrapper);
        Integer userId = LikeFrontThreadLocal.getUserId();

        List<StarInfoListedVo> list = new LinkedList<>();
        for(StarInfo item : iPage.getRecords()) {
            StarInfoListedVo vo = new StarInfoListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setAvatar(UrlUtils.toAbsoluteUrl(item.getAvatar()));

            DictData dictData = dictDataMapper.selectById(item.getCountry());
            if (dictData != null) {
                vo.setCountryName(dictData.getName());
            }
            if (userId > 0) {
//                查看是否收藏
                vo.setIsHistory(Math.toIntExact(userHistoryMapper.selectCount(new QueryWrapper<UserHistory>()
                        .eq("userid", userId)
                        .eq("type", "S")
                        .eq("s_m_id", item.getId())
                        .last("limit 1"))));
            }

            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    @Override
    public StarInfoDetailVo detailByName(String name) {
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

        Integer userId = LikeFrontThreadLocal.getUserId();
        StarInfoDetailVo vo = new StarInfoDetailVo();
        BeanUtils.copyProperties(model, vo);
//        vo.setBirthday( TimeUtils.formatChinaDate(model.getBirthday(), "yyyy-MM-dd") );
        vo.setAvatar(UrlUtils.toAbsoluteUrl(model.getAvatar()));
        if (userId > 0) {
//                查看是否收藏
            vo.setIsHistory(Math.toIntExact(userHistoryMapper.selectCount(new QueryWrapper<UserHistory>()
                    .eq("userid", userId)
                    .eq("type", "S")
                    .eq("s_m_id", model.getId())
                    .last("limit 1"))));
        }
        DictData dictData = dictDataMapper.selectById(model.getCountry());
        if (dictData != null) {
            vo.setCountryName(dictData.getName());
        }

        List<MoviesInfoDetailVo> movieList = new LinkedList<>();
        if (model.getMovies() != null) {
        //处理影视
            JSONArray movies = JSONArray.parseArray(model.getMovies());
            if (movies != null) {
                for (int i = 0; i < movies.size(); i++) {
                    Integer movieId = movies.getInteger(i);
                    MoviesInfo movie = moviesInfoMapper.selectById(movieId);
                    MoviesInfoDetailVo vo1 = new MoviesInfoDetailVo();
                    if (movie != null) {
                        BeanUtils.copyProperties(movie, vo1);
                        vo1.setImage(UrlUtils.toAbsoluteUrl(movie.getImage()));
                        movieList.add(vo1);
                    }
                }
            }
        }
        vo.setMovies(JSONArray.toJSONString(movieList));
        return vo;
    }

    /**
     * 明星资料删除
     *
     * @author 十八子
     * @param id 主键ID
     */
    @Override
    public void del(Integer id) {
        StarInfo model = starInfoMapper.selectOne(
                new QueryWrapper<StarInfo>()
                    .eq("id", id)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");

        starInfoMapper.delete(new QueryWrapper<StarInfo>().eq("id", id));
    }
    /**
     * 赠送糖果
     */
    @Override
    @Transactional
    public Map<String, Object> gift(Integer starId, Integer giftCount){
        StarInfo entity = starInfoMapper.selectById(starId);
        Assert.notNull(entity, "明星数据不存在!");

        Integer userId = LikeFrontThreadLocal.getUserId();
        Map<String, Object> result = new HashMap<String, Object>();
        int rs = starInfoMapper.gift(starId, giftCount);

        if (rs!=0) {
            //扣除用户糖果
            if(userMapper.updateUserMoneyAdd(userId, -giftCount, -giftCount) > 0){
                //写入日志
                StarGiftLog log = new StarGiftLog();
                log.setStarId(starId);
                log.setUserId(userId);
                log.setGiftCount(giftCount);
                log.setCreateDate( new Date());
                log.setCreateTime(System.currentTimeMillis()/1000);
                if( starGiftLogMapper.insert(log) > 0){
                    result.put("total", Math.toIntExact(starGiftLogMapper.selectCount(new QueryWrapper<StarGiftLog>()
                                .eq("star_id", starId)
                                .eq("user_id", userId).last("limit 1"))));
                    StarInfo info = starInfoMapper.selectById(starId);
                    User user = userMapper.selectById(userId);
                    result.put("starGiftTotal", info.getCandy());
                    result.put("userMoney", user.getUserMoney());

                    //查询总赠送数
                    return result;
                }
            } else {
                throw new RuntimeSqlException("用户糖果不足，赠送失败！");
            }
        }
        throw new OperateException("赠送失败！");
    }

    @Override
    public List<Map<String, Object>> giftLogByMonth(String month){
        if(month==null || !month.matches("^[\\d]{4,4}\\-[\\d]{2,2}$")){
            throw new OperateException("请传入正确的年月如:2025-09!");
        }
        return starGiftLogMapper.selectGiftLogGroupByMonth(month);
    }

    @Override
    public List<Map<String, Object>> giftLogGroupByStarID(Integer userId, Integer limit, Integer by){
        if(limit==null || limit <= 0){
            throw new OperateException("请传入需要取得条数");
        }

        QueryWrapper<StarGiftLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("star_id, SUM(gift_count) AS gift_count");
        if(by!=null && by==1){
            queryWrapper.orderByAsc("gift_count");
        }else{
            queryWrapper.orderByDesc("gift_count");
        }


        queryWrapper.last("limit " + limit);
        List<Map<String, Object>> list = starGiftLogMapper.selectMaps(queryWrapper.eq("user_id", userId).groupBy("star_id"));
        QueryWrapper<StarInfo> sQueryWrapper = new QueryWrapper<>();
        sQueryWrapper.select("name");
        for (Map<String, Object> item : list) {
            StarInfo starInfo = starInfoMapper.selectOne(sQueryWrapper.eq("id", item.get("star_id")).last("limit 1"));
            if(starInfo!=null){
                item.put("star_name", starInfo.getName());
            }else{
                item.put("star_name", "未知");
            }
        }
        return list;
    }

    @Override
    public PageResult<StarGiftLogListedVo> giftLogGroupByStarID(PageValidate pageValidate, Integer userId, Integer by) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<StarGiftLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("star_id, SUM(gift_count) AS gift_count");
        if(by!=null && by==1){
            queryWrapper.orderByAsc("gift_count");
        }else{
            queryWrapper.orderByDesc("gift_count");
        }
        queryWrapper.eq("user_id", userId).groupBy("star_id");


        IPage<StarGiftLog> iPage = starGiftLogMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<StarGiftLogListedVo> list = new LinkedList<>();

        for (StarGiftLog item : iPage.getRecords()) {
            StarGiftLogListedVo vo = new StarGiftLogListedVo();
            BeanUtils.copyProperties(item, vo);
            QueryWrapper<StarInfo> sQueryWrapper = new QueryWrapper<>();
            sQueryWrapper.select("name, avatar");
            StarInfo starInfo = starInfoMapper.selectOne(sQueryWrapper.eq("id", item.getStarId()).last("limit 1"));
            if(starInfo!=null){
                vo.setStarName(starInfo.getName());
                vo.setAvatar(UrlUtils.toAbsoluteUrl(starInfo.getAvatar()));
            }else{
                vo.setStarName("未知");
                vo.setAvatar(UrlUtils.toAbsoluteUrl("/static/avatar.png"));
            }
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }
}
