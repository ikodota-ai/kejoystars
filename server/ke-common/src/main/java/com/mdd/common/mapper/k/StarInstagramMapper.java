package com.mdd.common.mapper.k;

import com.mdd.common.core.basics.IBaseMapper;
import com.mdd.common.entity.k.StarInstagram;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 【请填写功能名称】Mapper
 * @author LikeAdmin
 */
@Mapper
public interface StarInstagramMapper extends IBaseMapper<StarInstagram> {
    @Update("UPDATE la_star_instagram SET status = #{status}, verify_time = UNIX_TIMESTAMP() WHERE id IN (${ids})")
    public int updateBatchStatus(@Param("ids") String ids, @Param("status") String status);

    @Delete("DELETE FROM la_star_instagram WHERE id IN (${ids})")
    public int deleteBatchIds(@Param("ids") String ids);
}
