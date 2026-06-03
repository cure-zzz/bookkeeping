package com.bookkeeping.mapper;

import com.bookkeeping.entity.Budget;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BudgetMapper {
    
    @Select("SELECT * FROM budget WHERE user_id = #{userId} AND month = #{month}")
    List<Budget> findByMonth(@Param("userId") Long userId, @Param("month") String month);
    
    @Select("SELECT * FROM budget WHERE id = #{id} AND user_id = #{userId}")
    Budget findById(@Param("id") Long id, @Param("userId") Long userId);
    
    @Select("SELECT * FROM budget WHERE user_id = #{userId} AND category_id = #{categoryId} AND month = #{month}")
    Budget findByCategoryAndMonth(@Param("userId") Long userId, 
                                  @Param("categoryId") Long categoryId, 
                                  @Param("month") String month);
    
    @Insert("INSERT INTO budget(user_id, category_id, amount, month, created_at) " +
            "VALUES(#{userId}, #{categoryId}, #{amount}, #{month}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Budget budget);
    
    @Update("UPDATE budget SET amount = #{amount} WHERE id = #{id} AND user_id = #{userId}")
    int update(Budget budget);
    
    @Delete("DELETE FROM budget WHERE id = #{id} AND user_id = #{userId}")
    int delete(@Param("id") Long id, @Param("userId") Long userId);
}
