package com.bookkeeping.mapper;

import com.bookkeeping.entity.Liability;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface LiabilityMapper {
    
    @Select("SELECT * FROM liability WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Liability> findByUserId(@Param("userId") Long userId);
    
    @Select("SELECT * FROM liability WHERE id = #{id} AND user_id = #{userId}")
    Liability findById(@Param("id") Long id, @Param("userId") Long userId);
    
    @Insert("INSERT INTO liability(user_id, name, type, amount, used_amount, remaining_amount, description, due_date, created_at) " +
            "VALUES(#{userId}, #{name}, #{type}, #{amount}, #{usedAmount}, #{remainingAmount}, #{description}, #{dueDate}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Liability liability);
    
    @Update("UPDATE liability SET name = #{name}, type = #{type}, amount = #{amount}, " +
            "used_amount = #{usedAmount}, remaining_amount = #{remainingAmount}, " +
            "description = #{description}, due_date = #{dueDate}, updated_at = NOW() " +
            "WHERE id = #{id} AND user_id = #{userId}")
    int update(Liability liability);
    
    @Delete("DELETE FROM liability WHERE id = #{id} AND user_id = #{userId}")
    int delete(@Param("id") Long id, @Param("userId") Long userId);
    
    @Select("SELECT COALESCE(SUM(amount), 0) FROM liability WHERE user_id = #{userId} AND type = #{type}")
    BigDecimal sumAmount(@Param("userId") Long userId, @Param("type") Integer type);
}
