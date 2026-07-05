package com.mdd.common.mapper.k;

import com.mdd.common.core.basics.IBaseMapper;
import com.mdd.common.entity.k.StarGiftLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 赠送明星糖果礼物Mapper
 * @author LikeAdmin
 */
@Mapper
public interface StarGiftLogMapper extends IBaseMapper<StarGiftLog> {
    //language=MyBatis 通过 create_date like '2024-09%' 输出字符格式化create_date输出day count(id)
    @Select("SELECT DATE_FORMAT(create_date, '%d') AS day, COUNT(id) AS count FROM la_star_gift_log WHERE create_date LIKE CONCAT(#{month}, '%') GROUP BY day")
    List<Map<String, Object>> selectGiftLogGroupByMonth(@Param("month") String month);
}
