package com.bookkeeping.mapper;

import com.bookkeeping.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);
    
    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(@Param("email") String email);
    
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(@Param("id") Long id);
    
    @Insert("INSERT INTO user(username, password, nickname, email, avatar, created_at) " +
            "VALUES(#{username}, #{password}, #{nickname}, #{email}, #{avatar}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Update("UPDATE user SET nickname = #{nickname}, email = #{email}, updated_at = NOW() WHERE id = #{id}")
    int update(User user);
    
    @Update("UPDATE user SET password = #{password}, updated_at = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
