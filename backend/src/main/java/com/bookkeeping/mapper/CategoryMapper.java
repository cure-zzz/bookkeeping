package com.bookkeeping.mapper;

import com.bookkeeping.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {
    
    @Select("SELECT * FROM category WHERE user_id IS NULL OR user_id = #{userId} ORDER BY type, name")
    List<Category> findAll(@Param("userId") Long userId);
    
    @Select("SELECT * FROM category WHERE type = #{type} AND (user_id IS NULL OR user_id = #{userId}) ORDER BY name")
    List<Category> findByType(@Param("type") Integer type, @Param("userId") Long userId);
    
    @Select("SELECT * FROM category WHERE id = #{id}")
    Category findById(@Param("id") Long id);
    
    @Insert("INSERT INTO category(name, icon, type, color, user_id, created_at) " +
            "VALUES(#{name}, #{icon}, #{type}, #{color}, #{userId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);
    
    @Update("UPDATE category SET name = #{name}, icon = #{icon}, color = #{color} WHERE id = #{id} AND user_id = #{userId}")
    int update(Category category);
    
    @Delete("DELETE FROM category WHERE id = #{id} AND user_id = #{userId}")
    int delete(@Param("id") Long id, @Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM category WHERE user_id IS NULL OR user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);
}
