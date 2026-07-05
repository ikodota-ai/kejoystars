package com.mdd.front.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.mdd.common.config.GlobalConfig;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.article.Article;
import com.mdd.common.entity.article.ArticleCate;
import com.mdd.common.entity.article.ArticleCollect;
import com.mdd.common.entity.k.UserHistory;
import com.mdd.common.enums.YesNoEnum;
import com.mdd.common.mapper.article.ArticleCateMapper;
import com.mdd.common.mapper.article.ArticleCollectMapper;
import com.mdd.common.mapper.article.ArticleMapper;
import com.mdd.common.mapper.k.UserHistoryMapper;
import com.mdd.common.util.StringUtils;
import com.mdd.common.util.TimeUtils;
import com.mdd.common.util.UrlUtils;
import com.mdd.front.service.IArticleService;
import com.mdd.front.service.k.IUserHistoryService;
import com.mdd.front.validate.article.ArticleSearchValidate;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.validate.k.UserHistorySearchValidate;
import com.mdd.front.vo.article.ArticleCateVo;
import com.mdd.front.vo.article.ArticleCollectVo;
import com.mdd.front.vo.article.ArticleDetailVo;
import com.mdd.front.vo.article.ArticleListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
    ArticleCollectMapper articleCollectMapper;

    @Resource
    UserHistoryMapper userHistoryMapper;

    /**
     * 文章分类
     *
     * @author fzr
     * @return List<ArticleCateVo>
     */
    @Override
    public List<ArticleCateVo> category() {
        List<ArticleCate> articleCateVos = articleCategoryMapper.selectList(
                new QueryWrapper<ArticleCate>()
                    .select("id,name")
                    .eq("is_show", 1)
                    .isNull("delete_time")
                    .orderByDesc(Arrays.asList("sort", "id")));

        List<ArticleCateVo> list = new LinkedList<>();
        for (ArticleCate category: articleCateVos) {
            ArticleCateVo vo = new ArticleCateVo();
            BeanUtils.copyProperties(category, vo);
            list.add(vo);
        }

        return list;
    }

    /**
     * 文章列表
     *
     * @author fzr
     * @param userId 用户ID
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<ArticleListVo>
     */
    @Override
    public PageResult<ArticleListedVo> list( Integer userId, PageValidate pageValidate, ArticleSearchValidate searchValidate) {
        Integer pageNo   = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id,cid,title,movies, url, content, `desc`,image,click_virtual,click_actual,create_time");
        queryWrapper.isNull("delete_time");
        queryWrapper.apply("is_show=1");

        articleMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "like:keyword@title:str",
                "=:cpid@cpid:int"
        });

        if (StringUtils.isNotNull(searchValidate.getCid()) && searchValidate.getCid() > 0) {
            queryWrapper.eq("cid", searchValidate.getCid());
        }
        if (searchValidate.getDate()!=null && !searchValidate.getDate().isEmpty()) {
            queryWrapper.between("create_time", TimeUtils.dateToTimestamp(searchValidate.getDate()), TimeUtils.dateToTimestamp(searchValidate.getDate()) + 86400);
        }
        String Cpids = "";
        if (searchValidate.getCollect()!=null && searchValidate.getCollect() == 1) {
            QueryWrapper<UserHistory> hQueryWrapper = new QueryWrapper<>();
            hQueryWrapper.select("s_m_id")
                    .eq("userid", userId)
                    .eq("type", "C");
//                    .eq("status", YesNoEnum.YES.getCode());
            List<UserHistory> list = userHistoryMapper.selectList(hQueryWrapper);
            //将list 中所有SMId属性组成字符串，以","分隔
            List<String> smIdStrList = new ArrayList<>();
            for (UserHistory item : list) {
                smIdStrList.add(item.getSMId().toString());
            }
            Cpids = String.join(",", smIdStrList);
//        } else if (searchValidate.getMovieId()!=null && !searchValidate.getMovieId().isEmpty()) {
//            movieId = searchValidate.getMovieId();
        }

        if (!Cpids.isEmpty()) {
//            String[] arr = Cpids.split(",");
//            List<String> movieIds = Arrays.asList(arr);
//            if (!movieIds.isEmpty()) {
//                queryWrapper.apply("JSON_EXTRACT(movies, '$.id') IN (" + String.join(",", movieIds) + ")");
            queryWrapper.apply("cpid IN (" + String.join(",", Cpids) + ")");
//            }
        }

        // sort 未传或未识别时，默认按 create_time DESC
        String sort = StringUtils.isNotNull(searchValidate.getSort()) ? searchValidate.getSort() : "";
        switch (sort) {
            case "hot": // 最热
                queryWrapper.orderByDesc("click_actual + click_virtual").orderByDesc("id");
                break;
            case "new": // 最新
                queryWrapper.orderByDesc("id");
                break;
            case "asc":
                queryWrapper.orderByAsc("create_time").orderByAsc("id");
                break;
            case "desc":
            case "default":
            default:    // 默认：创建时间倒序
                queryWrapper.orderByDesc("create_time").orderByDesc("id");
                break;
        }
        System.out.println(queryWrapper.getSqlSegment());
        IPage<Article> iPage = articleMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);

        List<ArticleListedVo> list = new LinkedList<>();
        for (Article article : iPage.getRecords()) {
            ArticleListedVo vo = new ArticleListedVo();
            BeanUtils.copyProperties(article, vo);
            vo.setImage(UrlUtils.toAbsoluteUrl(article.getImage()));
            vo.setCreateTime(TimeUtils.timestampToDate(article.getCreateTime()));

            if (vo.getMovies()!=null) {
                vo.setMovieName(JSONObject.parse(vo.getMovies()).getString("name"));
            }
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 文章详情
     *
     * @author fzr
     * @param id 文章主键
     * @param userId 用户ID
     * @return ArticleDetailVo
     */
    @Override
    public ArticleDetailVo detail(Integer id, Integer userId) {
        Article article = articleMapper.selectOne(new QueryWrapper<Article>()
                .eq("id", id)
                .eq("is_show", 1)
                .isNull("delete_time" )
                .last("limit 1"));

        Assert.notNull(article, "数据不存在!");

        ArticleCollect articleCollect = articleCollectMapper.selectOne(new QueryWrapper<ArticleCollect>()
                .eq("user_id", userId)
                .eq("article_id", article.getId())
                .isNull("delete_time")
                .eq("status", YesNoEnum.YES.getCode())
                .last("limit 1"));

        ArticleDetailVo vo = new ArticleDetailVo();
        BeanUtils.copyProperties(article, vo);
        vo.setCollect(articleCollect != null);
        vo.setImage(UrlUtils.toAbsoluteUrl(article.getImage()));
        vo.setCreateTime(TimeUtils.timestampToDate(article.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(article.getUpdateTime()));
        vo.setClick(article.getClickActual() + article.getClickVirtual());

        article.setClickActual(article.getClickActual() + 1);
        articleMapper.updateById(article);

        return vo;
    }

    /**
     * 收藏列表
     *
     * @author fzr
     * @param pageValidate 分页参数
     * @param userId 用户ID
     * @return PageResult<ArticleCollectVo>
     */
    @Override
    public PageResult<ArticleCollectVo> collect(PageValidate pageValidate, Integer userId) {
        Integer pageNo   = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        MPJQueryWrapper<Article> mpjQueryWrapper = new MPJQueryWrapper<>();
        mpjQueryWrapper.setAlias("a").select("c.id,c.article_id,a.title,a.image,a.desc,a.is_show,a.click_virtual, a.click_actual,a.create_time, c.create_time as collect_time")
                .eq("c.user_id", userId)
                .isNull("c.delete_time")
                .isNull("a.delete_time")
                .eq("c.status", YesNoEnum.YES.getCode())
                .eq("a.is_show", YesNoEnum.YES.getCode())
                .orderByDesc("c.id")
                .innerJoin("?_article_collect c ON c.article_id=a.id".replace("?_", GlobalConfig.tablePrefix));

        IPage<ArticleCollectVo> iPage = articleMapper.selectJoinPage(
                new Page<>(pageNo, pageSize),
                ArticleCollectVo.class,
                mpjQueryWrapper);

        for (ArticleCollectVo vo : iPage.getRecords()) {
            vo.setImage(UrlUtils.toAbsoluteUrl(vo.getImage()));
            vo.setCreateTime(TimeUtils.timestampToDate(Long.valueOf(vo.getCreateTime().toString())));
            vo.setCollectTime(TimeUtils.timestampToDate(Long.valueOf(vo.getCollectTime().toString())));
        }
        return PageResult.iPageHandle(iPage);
    }

    /**
     * 加入收藏
     *
     * @author fzr
     * @param articleId 主键
     * @param userId 用户ID
     */
    @Override
    public void addCollect(Integer articleId, Integer userId) {
        ArticleCollect articleCollect = articleCollectMapper.selectOne(
                new QueryWrapper<ArticleCollect>()
                    .eq("article_id", articleId)
                    .eq("user_id", userId)
                        .isNull("delete_time")
                    .last("limit 1"));

        if (StringUtils.isNotNull(articleCollect)) {
            articleCollect.setUpdateTime(System.currentTimeMillis() / 1000);
            articleCollect.setStatus(YesNoEnum.YES.getCode());
            articleCollectMapper.updateById(articleCollect);
        } else {
            ArticleCollect model = new ArticleCollect();
            model.setArticleId(articleId);
            model.setUserId(userId);
            model.setStatus(YesNoEnum.YES.getCode());
            model.setCreateTime(System.currentTimeMillis() / 1000);
            model.setUpdateTime(System.currentTimeMillis() / 1000);
            articleCollectMapper.insert(model);
        }
    }

    /**
     * 取消收藏
     *
     * @author fzr
     * @param articleId 文章ID
     * @param userId 用户ID
     */
    @Override
    public void cancelCollect(Integer articleId, Integer userId) {
        ArticleCollect articleCollect = articleCollectMapper.selectOne(
                new QueryWrapper<ArticleCollect>()
                        .eq("article_id", articleId)
                        .eq("user_id", userId)
                        .isNull("delete_time")
                        .last("limit 1"));

        Assert.notNull(articleCollect, "收藏不存在!");

        articleCollect.setStatus(YesNoEnum.NO.getCode());
        articleCollect.setUpdateTime(System.currentTimeMillis() / 1000);
        articleCollectMapper.updateById(articleCollect);
    }

}
