package com.bookkeeping.mapper;

import com.bookkeeping.entity.PartTimeJob;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PartTimeJobMapper {

    @Select("SELECT * FROM part_time_job WHERE user_id = #{userId} ORDER BY work_date DESC")
    List<PartTimeJob> findByUserId(Long userId);

    @Select("SELECT * FROM part_time_job WHERE id = #{id} AND user_id = #{userId}")
    PartTimeJob findById(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT * FROM part_time_job WHERE user_id = #{userId} AND work_date BETWEEN #{startDate} AND #{endDate} ORDER BY work_date DESC")
    List<PartTimeJob> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT * FROM part_time_job WHERE user_id = #{userId} AND work_date BETWEEN #{startDate} AND #{endDate} ORDER BY work_date DESC LIMIT #{offset}, #{limit}")
    List<PartTimeJob> findByUserIdAndDateRangeWithPage(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM part_time_job WHERE user_id = #{userId} AND work_date BETWEEN #{startDate} AND #{endDate}")
    int countByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT * FROM part_time_job WHERE user_id = #{userId} ORDER BY work_date DESC LIMIT #{offset}, #{limit}")
    List<PartTimeJob> findByUserIdWithPage(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM part_time_job WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM part_time_job WHERE user_id = #{userId}")
    BigDecimal getTotalAmount(Long userId);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM part_time_job WHERE user_id = #{userId} AND work_date BETWEEN #{startDate} AND #{endDate}")
    BigDecimal getAmountByDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM part_time_job WHERE user_id = #{userId} AND work_date BETWEEN #{startDate} AND #{endDate}")
    BigDecimal getAmountByYearMonthRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Insert("INSERT INTO part_time_job (user_id, amount, description, work_date) VALUES (#{userId}, #{amount}, #{description}, #{workDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PartTimeJob partTimeJob);

    @Update("UPDATE part_time_job SET amount = #{amount}, description = #{description}, work_date = #{workDate} WHERE id = #{id} AND user_id = #{userId}")
    void update(PartTimeJob partTimeJob);

    @Delete("DELETE FROM part_time_job WHERE id = #{id} AND user_id = #{userId}")
    void delete(@Param("id") Long id, @Param("userId") Long userId);
}
