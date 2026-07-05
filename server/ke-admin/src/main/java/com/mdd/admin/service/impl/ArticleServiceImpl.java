package com.mdd.admin.service.impl;

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
import com.github.yulichang.query.MPJQueryWrapper;
import com.mdd.admin.service.IArticleService;
import com.mdd.admin.service.IJPushJiGuangService;
import com.mdd.admin.service.impl.k.MoviesInfoServiceImpl;
import com.mdd.admin.validate.article.ArticleCreateValidate;
import com.mdd.admin.validate.article.ArticleSearchValidate;
import com.mdd.admin.validate.article.ArticleUpdateValidate;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.vo.article.ArticleDetailVo;
import com.mdd.admin.vo.article.ArticleListedVo;
import com.mdd.common.config.GlobalConfig;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.admin.Admin;
import com.mdd.common.entity.article.Article;
import com.mdd.common.entity.article.ArticleCate;
import com.mdd.common.entity.k.Couple;
import com.mdd.common.entity.k.UserHistory;
import com.mdd.common.entity.user.User;
import com.mdd.common.mapper.admin.AdminMapper;
import com.mdd.common.mapper.article.ArticleCateMapper;
import com.mdd.common.mapper.article.ArticleMapper;
import com.mdd.common.mapper.k.CoupleMapper;
import com.mdd.common.mapper.k.UserHistoryMapper;
import com.mdd.common.mapper.user.UserMapper;
import com.mdd.common.util.StringUtils;
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
 * 文章服务实现类
 */
@Service
public class ArticleServiceImpl implements IArticleService {

    @Resource
    ArticleMapper articleMapper;

    @Resource
    ArticleCateMapper articleCategoryMapper;

    @Resource
    AdminMapper systemAuthAdminMapper;
    /**
     * 文章列表
     *
     * @author fzr
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<ArticleListVo>
     */
    @Override
    public PageResult<ArticleListedVo> list(PageValidate pageValidate, ArticleSearchValidate searchValidate) {
        Integer pageNo   = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        MPJQueryWrapper<Article> mpjQueryWrapper = new MPJQueryWrapper<Article>()
                .selectAll(Article.class)
                .select("ac.name as cateName")
                .innerJoin("?_article_cate ac ON ac.id=t.cid".replace("?_", GlobalConfig.tablePrefix))
                .isNull("t.delete_time")
                .orderByDesc(Arrays.asList("t.sort", "t.id"));

        articleMapper.setSearch(mpjQueryWrapper, searchValidate, new String[]{
                "like:title@t.title:str",
                "=:cid@t.cid:int",
                "=:cpid@t.cpid:int",
                "=:is_show@t.is_show:int",
                "datetime:startTime-endTime@t.create_time:str"
        });
        String s = mpjQueryWrapper.getTargetSql();
        IPage<ArticleListedVo> iPage = articleMapper.selectJoinPage(
                new Page<>(pageNo, pageSize),
                ArticleListedVo.class,
                mpjQueryWrapper);

        for (ArticleListedVo vo : iPage.getRecords()) {
            vo.setImage(UrlUtils.toAdminAbsoluteUrl(vo.getImage()));

            Integer clickActual = vo.getClickActual() != null ? vo.getClickActual() : 0;

            Integer clickVirtual = vo.getClickVirtual() != null ? vo.getClickVirtual() : 0;

            Integer click = clickActual + clickVirtual;

            vo.setClick(click);
//            if (vo.getMovies()!=null) {
//                vo.setMovieName(JSONObject.parse(vo.getMovies()).getString("name"));
//            }
            vo.setCreateTime(TimeUtils.timestampToDate(vo.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(vo.getUpdateTime()));
        }

        return PageResult.iPageHandle(iPage);
    }

    /**
     * 文章详情
     *
     * @author fzr
     * @param id 主键ID
     */
    @Override
    public ArticleDetailVo detail(Integer id) {
        Article model = articleMapper.selectOne(
                new QueryWrapper<Article>()
                        .select(Article.class, info->
                          !info.getColumn().equals("delete_time"))
                        .eq("id", id)
                        .isNull("delete_time"));

        Assert.notNull(model, "文章不存在");

        ArticleDetailVo vo = new ArticleDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setContent(StringUtils.isNull(model.getContent()) ? "" : model.getContent());
        vo.setImage(UrlUtils.toAdminAbsoluteUrl(model.getImage()));
        vo.setCreateTime(model.getCreateTime()==null?"":TimeUtils.timestampToDate(model.getCreateTime()));
        vo.setUpdateTime(model.getUpdateTime()==null?"":TimeUtils.timestampToDate(model.getUpdateTime()));
        if (vo.getMovies()!=null) {
            JSONObject json = JSONObject.parse(vo.getMovies());
            vo.setMovieId(json.getInteger("id"));
            vo.setMovieName(json.getString("name"));
        }

        return vo;
    }

    /**
     * 文章新增
     *
     * @author fzr
     * @param createValidate 文章参数
     */
    @Override
    public void add(ArticleCreateValidate createValidate) {
        Article model = new Article();
        model.setCid(createValidate.getCid());
        model.setCpid(createValidate.getCpid());
        model.setTitle(createValidate.getTitle());
        model.setUrl(createValidate.getUrl());
        model.setImage(UrlUtils.toRelativeUrl(createValidate.getImage()));
        model.setContent(createValidate.getContent());
        model.setSort(createValidate.getSort());
        model.setIsShow(createValidate.getIsShow());
        Object id = StpUtil.getLoginId();

        Admin adminUser = systemAuthAdminMapper.selectOne(
                new QueryWrapper<Admin>()
                        .select("id,name,disable")
                        .eq("id", Integer.parseInt(id.toString()))
                        .isNull("delete_time")
                        .last("limit 1"));
        if (adminUser != null) {
            model.setAuthor(adminUser.getName());
        } else {
            model.setAuthor("未知");
        }

        model.setAbstractField(createValidate.getAbstractFied());
        model.setDesc(createValidate.getDesc());
        if (createValidate.getCreateTime()<=0) {
            model.setCreateTime(TimeUtils.timestamp());
        } else {
            model.setCreateTime(createValidate.getCreateTime());
        }
        model.setMovies( createValidate.getMovies() );
        model.setCreateTime(TimeUtils.timestamp());
        model.setUpdateTime(TimeUtils.timestamp());
        model.setClickVirtual(createValidate.getClick_virtual());
        articleMapper.insert(model);

        if (model.getCpid() != null && model.getCpid()>0) {
            List<UserHistory> actorList = userHistoryMapper.selectList(new QueryWrapper<UserHistory>().eq("type", "C").in("s_m_id", model.getCpid()));
            Couple cp = coupleMapper.selectById( model.getCpid() );
            if (actorList != null && !actorList.isEmpty()) {
                String msg = String.format("您关注的CP[%s]有新的资讯信息！", cp.getName());
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
    IJPushJiGuangService jPushJiGuangService;
    Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);
    @Resource
    PushApi pushApi;
    @Resource
    UserHistoryMapper userHistoryMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    CoupleMapper coupleMapper;
    /**
     * 文章编辑
     *
     * @author fzr
     * @param updateValidate 文章参数
     */
    @Override
    public void edit(ArticleUpdateValidate updateValidate) {
        Article model = articleMapper.selectOne(
                new QueryWrapper<Article>()
                .eq("id", updateValidate.getId())
                .isNull("delete_time"));

        Assert.notNull(model, "文章不存在!");

        Assert.notNull(articleCategoryMapper.selectOne(
                new QueryWrapper<ArticleCate>()
                .eq("id", updateValidate.getCid())
                .isNull("delete_time")), "分类不存在");

        model.setCid(updateValidate.getCid());
        model.setCpid(updateValidate.getCpid());
        model.setTitle(updateValidate.getTitle());
        model.setUrl(updateValidate.getUrl());
        model.setMovies( updateValidate.getMovies() );
        model.setImage(UrlUtils.toRelativeUrl(updateValidate.getImage()));
        model.setContent(updateValidate.getContent());
        model.setIsShow(updateValidate.getIsShow());
        model.setAuthor(updateValidate.getAuthor());
        model.setSort(updateValidate.getSort());
        model.setAbstractField(updateValidate.getAbstractFied());
        model.setDesc(updateValidate.getDesc());
        model.setUpdateTime(TimeUtils.timestamp());
        model.setClickVirtual(updateValidate.getClick_virtual());
        articleMapper.updateById(model);
    }

    /**
     * 文章删除
     *
     * @author fzr
     * @param id 文章ID
     */
    @Override
    public void del(Integer id) {
        Article article = articleMapper.selectOne(
                new QueryWrapper<Article>()
                        .select("id, is_show")
                        .eq("id", id)
                        .isNull("delete_time"));

        Assert.notNull(article, "文章不存在!");
        article.setDeleteTime(TimeUtils.timestamp());
        articleMapper.updateById(article);
    }

    /**
     * 文章状态
     *
     * @author fzr
     * @param id 文章主键
     */
    @Override
    public void change(Integer id) {
        Article article = articleMapper.selectOne(
                new QueryWrapper<Article>()
                        .select("id, is_show")
                        .eq("id", id)
                        .isNull("delete_time"));

        Assert.notNull(article, "文章不存在!");

        article.setIsShow(article.getIsShow() == 0 ? 1:0);
        article.setUpdateTime(TimeUtils.timestamp());
        articleMapper.updateById(article);
    }

}
