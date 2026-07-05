package com.mdd.common.mapper.user;

import com.mdd.common.core.basics.IBaseMapper;
import com.mdd.common.entity.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends IBaseMapper<User> {
    @Update("update `la_user` set user_money = user_money + #{candy} where id = #{userId} and user_money + #{user_money} >= 0")
    int updateUserMoneyAdd(@Param("userId") Integer userId, @Param("candy") Integer candy, @Param("user_money") Integer user_money);
}
