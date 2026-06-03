package com.bookkeeping.mapper;

import com.bookkeeping.entity.Bill;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BillMapper {
    
    @Select("<script>" +
            "SELECT * FROM bill WHERE user_id = #{userId} " +
            "<if test='type != null'>AND type = #{type}</if>" +
            "<if test='categoryId != null'>AND category_id = #{categoryId}</if>" +
            "<if test='startDate != null'>AND bill_date &gt;= #{startDate}</if>" +
            "<if test='endDate != null'>AND bill_date &lt;= #{endDate}</if>" +
            "ORDER BY bill_date DESC, created_at DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Bill> findPage(@Param("userId") Long userId, 
                        @Param("type") Integer type,
                        @Param("categoryId") Long categoryId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("offset") Integer offset,
                        @Param("limit") Integer limit);
    
    @Select("<script>" +
            "SELECT COUNT(*) FROM bill WHERE user_id = #{userId} " +
            "<if test='type != null'>AND type = #{type}</if>" +
            "<if test='categoryId != null'>AND category_id = #{categoryId}</if>" +
            "<if test='startDate != null'>AND bill_date &gt;= #{startDate}</if>" +
            "<if test='endDate != null'>AND bill_date &lt;= #{endDate}</if>" +
            "</script>")
    long count(@Param("userId") Long userId, 
               @Param("type") Integer type,
               @Param("categoryId") Long categoryId,
               @Param("startDate") LocalDate startDate,
               @Param("endDate") LocalDate endDate);
    
    @Select("SELECT * FROM bill WHERE id = #{id} AND user_id = #{userId}")
    @Results({
        @Result(property = "paymentAccountId", column = "payment_account_id")
    })
    Bill findById(@Param("id") Long id, @Param("userId") Long userId);
    
    @Insert("INSERT INTO bill(user_id, amount, type, category_id, payment_account_id, description, bill_date, created_at) " +
            "VALUES(#{userId}, #{amount}, #{type}, #{categoryId}, #{paymentAccountId}, #{description}, #{billDate}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Bill bill);
    
    @Update("UPDATE bill SET amount = #{amount}, type = #{type}, category_id = #{categoryId}, " +
            "payment_account_id = #{paymentAccountId}, description = #{description}, bill_date = #{billDate}, updated_at = NOW() WHERE id = #{id} AND user_id = #{userId}")
    int update(Bill bill);
    
    @Delete("DELETE FROM bill WHERE id = #{id} AND user_id = #{userId}")
    int delete(@Param("id") Long id, @Param("userId") Long userId);
    
    @Select("<script>" +
            "SELECT COALESCE(SUM(amount), 0) FROM bill WHERE user_id = #{userId} AND type = #{type} " +
            "<if test='startDate != null'>AND bill_date &gt;= #{startDate}</if>" +
            "<if test='endDate != null'>AND bill_date &lt;= #{endDate}</if>" +
            "</script>")
    BigDecimal sumAmount(@Param("userId") Long userId, 
                         @Param("type") Integer type,
                         @Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate);
    
    @Select("<script>" +
            "SELECT category_id, SUM(amount) as total FROM bill " +
            "WHERE user_id = #{userId} AND type = 2 " +
            "<if test='startDate != null'>AND bill_date &gt;= #{startDate}</if>" +
            "<if test='endDate != null'>AND bill_date &lt;= #{endDate}</if>" +
            "GROUP BY category_id ORDER BY total DESC" +
            "</script>")
    @Results({
        @Result(property = "categoryId", column = "category_id"),
        @Result(property = "total", column = "total")
    })
    List<CategoryAmountVO> sumByCategory(@Param("userId") Long userId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
    
    @Select("<script>" +
            "SELECT DATE(bill_date) as date, type, SUM(amount) as total " +
            "FROM bill WHERE user_id = #{userId} " +
            "<if test='startDate != null'>AND bill_date &gt;= #{startDate}</if>" +
            "<if test='endDate != null'>AND bill_date &lt;= #{endDate}</if>" +
            "GROUP BY DATE(bill_date), type ORDER BY date" +
            "</script>")
    @Results({
        @Result(property = "date", column = "date"),
        @Result(property = "type", column = "type"),
        @Result(property = "total", column = "total")
    })
    List<DailyAmountVO> sumByDate(@Param("userId") Long userId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);
    
    @Select("SELECT * FROM bill WHERE user_id = #{userId} ORDER BY bill_date DESC LIMIT #{limit}")
    List<Bill> findRecent(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    @Select("SELECT * FROM bill WHERE user_id = #{userId} AND bill_date = #{date}")
    List<Bill> findByDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Select("SELECT COUNT(*) FROM bill WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);
    
    @Select("SELECT COALESCE(SUM(amount), 0) FROM bill WHERE user_id = #{userId} AND type = #{type}")
    BigDecimal sumAmountByType(@Param("userId") Long userId, @Param("type") Integer type);
    
    @Select("<script>" +
            "SELECT MONTH(bill_date) as month, type, COALESCE(SUM(amount), 0) as total " +
            "FROM bill WHERE user_id = #{userId} AND YEAR(bill_date) = #{year} " +
            "GROUP BY MONTH(bill_date), type ORDER BY month" +
            "</script>")
    @Results({
        @Result(property = "month", column = "month"),
        @Result(property = "type", column = "type"),
        @Result(property = "total", column = "total")
    })
    List<MonthlyAmountVO> sumByMonth(@Param("userId") Long userId, @Param("year") Integer year);
    
    class CategoryAmountVO {
        private Long categoryId;
        private BigDecimal total;
        
        public CategoryAmountVO() {}
        
        public CategoryAmountVO(Long categoryId, BigDecimal total) {
            this.categoryId = categoryId;
            this.total = total;
        }
        
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
    }
    
    class DailyAmountVO {
        private LocalDate date;
        private Integer type;
        private BigDecimal total;
        
        public DailyAmountVO() {}
        
        public DailyAmountVO(LocalDate date, Integer type, BigDecimal total) {
            this.date = date;
            this.type = type;
            this.total = total;
        }
        
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public Integer getType() { return type; }
        public void setType(Integer type) { this.type = type; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
    }
    
    class MonthlyAmountVO {
        private Integer month;
        private Integer type;
        private BigDecimal total;
        
        public MonthlyAmountVO() {}
        
        public MonthlyAmountVO(Integer month, Integer type, BigDecimal total) {
            this.month = month;
            this.type = type;
            this.total = total;
        }
        
        public Integer getMonth() { return month; }
        public void setMonth(Integer month) { this.month = month; }
        public Integer getType() { return type; }
        public void setType(Integer type) { this.type = type; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
    }
}
