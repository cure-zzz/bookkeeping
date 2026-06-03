package com.bookkeeping.mapper;

import com.bookkeeping.entity.LiabilityRepayment;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface LiabilityRepaymentMapper {
    
    @Select("SELECT id, liability_id, user_id, amount, description, repayment_date, created_at " +
            "FROM liability_repayment WHERE liability_id = #{liabilityId} ORDER BY repayment_date DESC, created_at DESC")
    @Results({
        @Result(property = "liabilityId", column = "liability_id"),
        @Result(property = "repaymentDate", column = "repayment_date")
    })
    List<LiabilityRepayment> findByLiabilityId(@Param("liabilityId") Long liabilityId);
    
    @Select("SELECT id, liability_id, user_id, amount, description, repayment_date, created_at " +
            "FROM liability_repayment WHERE user_id = #{userId} ORDER BY repayment_date DESC, created_at DESC")
    @Results({
        @Result(property = "liabilityId", column = "liability_id"),
        @Result(property = "repaymentDate", column = "repayment_date")
    })
    List<LiabilityRepayment> findByUserId(@Param("userId") Long userId);
    
    @Insert("INSERT INTO liability_repayment(liability_id, user_id, amount, description, repayment_date, created_at) " +
            "VALUES(#{liabilityId}, #{userId}, #{amount}, #{description}, #{repaymentDate}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LiabilityRepayment repayment);
    
    @Delete("DELETE FROM liability_repayment WHERE id = #{id}")
    int delete(@Param("id") Long id);
    
    @Select("SELECT COALESCE(SUM(amount), 0) FROM liability_repayment WHERE liability_id = #{liabilityId}")
    BigDecimal sumByLiabilityId(@Param("liabilityId") Long liabilityId);
}
