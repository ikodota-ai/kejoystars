package com.mdd.front.service.impl.k;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.k.StarInfo;
import com.mdd.common.entity.k.StarInstagram;
import com.mdd.common.entity.k.StarInstagramDownLog;
import com.mdd.common.entity.user.User;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.k.StarInfoMapper;
import com.mdd.common.mapper.k.StarInstagramDownLogMapper;
import com.mdd.common.mapper.k.StarInstagramMapper;
import com.mdd.common.mapper.user.UserMapper;
import com.mdd.common.util.TimeUtils;
import com.mdd.common.util.UrlUtils;
import com.mdd.front.service.k.IStarInstagramService;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.validate.k.StarInstagramCreateValidate;
import com.mdd.front.validate.k.StarInstagramSearchValidate;
import com.mdd.front.validate.k.StarInstagramUpdateValidate;
import com.mdd.front.vo.k.StarInstagramDetailVo;
import com.mdd.front.vo.k.StarInstagramListedVo;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 明星照片墙实现类
 * @author LikeAdmin
 */
@Service
public class StarInstagramServiceImpl implements IStarInstagramService {

    @Resource
    StarInstagramMapper starInstagramMapper;

    @Resource
    StarInfoMapper starInfoMapper;

    @Resource
    StarInstagramDownLogMapper starInstagramDownLogMapper;

    @Resource
    UserMapper userMapper;

    /**
     * 明星照片墙列表
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
     * 明星照片墙详情
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
     * 明星照片墙新增
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
        model.setVerifyTime(createValidate.getVerifyTime());
        model.setCreateTime(System.currentTimeMillis() / 1000);
        starInstagramMapper.insert(model);
    }

    /**
     * 明星照片墙编辑
     *
     * @author LikeAdmin
     * @param updateValidate 参数
     */
    @Override
    public void edit(StarInstagramUpdateValidate updateValidate) {
        starInstagramMapper.updateBatchStatus(updateValidate.getId(), updateValidate.getStatus());
    }

    /**
     * 明星照片墙删除
     *
     * @author LikeAdmin
     * @param id 主键ID
     */
    @Override
    public void del(String id) {
        starInstagramMapper.deleteBatchIds(id);
    }

    @Override
    @Transactional
    public boolean checkDownload(Integer userid, String imageName) {
        String id = userid + "_" + imageName;
        if (starInstagramDownLogMapper.selectCount(new QueryWrapper<StarInstagramDownLog>().eq("id", id)) == 0) {
            //未检查到下载记录，检查今日下载次数，
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userid).last("limit 1"));
            if (user.getVipExpired() < System.currentTimeMillis() / 1000){
                throw new OperateException("VIP已过期，请续费");
            }
            String today = DateUtils.formatDate(new Date(), "yyyy-MM-dd");
            if (starInstagramDownLogMapper.selectCount(new QueryWrapper<StarInstagramDownLog>()
                    .eq("userid", userid).between("create_time", TimeUtils.dateToTimestamp(today + " 00:00:00"),
                            System.currentTimeMillis() / 1000)) < user.getVipDownCount()) {
                StarInstagram ins = starInstagramMapper.selectOne(new QueryWrapper<StarInstagram>().eq("check_code", imageName));
                //保存下载日志
                StarInstagramDownLog log = new StarInstagramDownLog();
                log.setId( id );
                log.setUserid(userid);
                log.setCheckCode(imageName);
                log.setInsid(ins.getId());
                log.setCreateTime(System.currentTimeMillis() / 1000);
                starInstagramDownLogMapper.insert(log);

                return true;
            } else {
                throw new OperateException("今日下载次数已用完，请升级VIP");
            }
        } else {
            return true;
        }
    }

    @Override
    public int getDownloadCount(Integer userid) {
        String today = DateUtils.formatDate(new Date(), "yyyy-MM-dd");
        return Math.toIntExact(starInstagramDownLogMapper.selectCount(new QueryWrapper<StarInstagramDownLog>()
                .eq("userid", userid).between("create_time", TimeUtils.dateToTimestamp(today + " 00:00:00"),
                        System.currentTimeMillis() / 1000)));
    }
}
