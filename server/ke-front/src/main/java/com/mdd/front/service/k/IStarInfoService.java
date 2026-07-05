package com.mdd.front.service.k;

import com.mdd.common.core.PageResult;
import com.mdd.common.entity.k.StarGiftLog;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.validate.k.StarInfoSearchValidate;
import com.mdd.front.vo.k.StarGiftLogListedVo;
import com.mdd.front.vo.k.StarInfoDetailVo;
import com.mdd.front.vo.k.StarInfoListedVo;

import java.util.List;
import java.util.Map;

/**
 * 明星资料服务接口类
 * @author 十八子
 */
public interface IStarInfoService {

    /**
     * 明星资料列表
     *
     * @author 十八子
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<StarInfoListedVo>
     */
    PageResult<StarInfoListedVo> list(Integer userid, PageValidate pageValidate, StarInfoSearchValidate searchValidate);

    /**
     * 通过明星名字查找明星资料
     *
     */
    StarInfoDetailVo detailByName(String name);
    /**
     * 明星资料详情
     *
     * @author 十八子
     * @param id 主键ID
     * @return StarInfoDetailVo
     */
    StarInfoDetailVo detail(Integer id);

    /**
     * 明星资料删除
     *
     * @author 十八子
     * @param id 主键ID
     */
    void del(Integer id);

    /**
     * 赠送糖果
     * @param starId 明星ID
     * @param giftCount 礼物数
     */
    Map<String, Object> gift(Integer starId, Integer giftCount);

    /**
     * 赠送礼物记录按日统计
     *
     * @param month 年月 2025-09
     */
    List<Map<String, Object>> giftLogByMonth(String month);

    /**
     * 赠送礼物记录按明星统计
     *
     * @param by 排序方式 0=最大倒序，1=最少排序
     */
    List<Map<String, Object>> giftLogGroupByStarID(Integer userId, Integer limit, Integer by);

    PageResult<StarGiftLogListedVo> giftLogGroupByStarID(PageValidate pageValidate, Integer userId, Integer by);
}
