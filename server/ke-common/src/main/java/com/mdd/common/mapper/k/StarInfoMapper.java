package com.mdd.common.mapper.k;

import com.mdd.common.core.basics.IBaseMapper;
import com.mdd.common.entity.k.StarInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 明星资料Mapper
 * @author 十八子
 */
@Mapper
public interface StarInfoMapper extends IBaseMapper<StarInfo> {
    @Update("update la_star_info set candy = candy + #{candy} where id = #{id}")
    int gift(@Param("id") Integer id, @Param("candy") Integer candy);

    @Update("update la_star_info set favorites = favorites + #{count} where id = #{starId}")
    int historyCount(@Param("starId") Integer starId, @Param("count") int count);
}
