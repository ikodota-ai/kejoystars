package com.mdd.common.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.mdd.common.core.basics.IBaseMapper;
import com.mdd.common.entity.Config;
import com.mdd.common.entity.Search;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统配置
 */
@Mapper
public interface SearchMapper extends IBaseMapper<Search> {
    //mybatis-plus union all la_star_info la_movies_info la_couple la_article
    @Select("SELECT name as title, id, 'S' as type FROM la_star_info WHERE name LIKE CONCAT('%', #{k1}, '%') " +
            "UNION ALL " +
            "SELECT movie_name as title, id, 'M' as type FROM la_movies_info WHERE movie_name LIKE CONCAT('%', #{k2}, '%') " +
            "UNION ALL " +
            "SELECT name as title, id, 'C' as type FROM la_couple WHERE name LIKE CONCAT('%', #{k3}, '%') " +
            "UNION ALL " +
            "SELECT title, id, 'A' as type FROM la_article WHERE title LIKE CONCAT('%', #{k4}, '%')")
    <P extends IPage<Search>> P selectPage(P page, @Param("k1") String k1, @Param("k2") String k2, @Param("k3") String k3, @Param("k4") String k4);
}
