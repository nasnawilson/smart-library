package com.smartlibrary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartlibrary.entity.ReadingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 阅读记录Mapper接口
 */
@Mapper
public interface ReadingRecordMapper extends BaseMapper<ReadingRecord> {

    /**
     * 查询用户的阅读记录（带图书信息）
     */
    @Select("SELECT rr.*, b.title as bookTitle, b.author as bookAuthor " +
            "FROM reading_record rr " +
            "LEFT JOIN book b ON rr.book_id = b.id " +
            "WHERE rr.user_id = #{userId} " +
            "ORDER BY rr.read_date DESC")
    List<ReadingRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户某段时间的阅读统计（按日期汇总）
     */
    @Select("SELECT read_date as date, SUM(duration_minutes) as totalMinutes, COUNT(*) as bookCount " +
            "FROM reading_record " +
            "WHERE user_id = #{userId} " +
            "AND read_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY read_date " +
            "ORDER BY read_date")
    List<Map<String, Object>> selectDailyStats(@Param("userId") Long userId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * 查询用户某段时间的阅读统计（按图书汇总）
     */
    @Select("SELECT rr.book_id, b.title as bookTitle, SUM(rr.duration_minutes) as totalMinutes, COUNT(*) as readCount " +
            "FROM reading_record rr " +
            "LEFT JOIN book b ON rr.book_id = b.id " +
            "WHERE rr.user_id = #{userId} " +
            "AND rr.read_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY rr.book_id, b.title " +
            "ORDER BY totalMinutes DESC")
    List<Map<String, Object>> selectBookStats(@Param("userId") Long userId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * 查询用户总阅读时长
     */
    @Select("SELECT COALESCE(SUM(duration_minutes), 0) FROM reading_record WHERE user_id = #{userId}")
    Integer selectTotalDuration(@Param("userId") Long userId);

    /**
     * 查询用户阅读图书数量
     */
    @Select("SELECT COUNT(DISTINCT book_id) FROM reading_record WHERE user_id = #{userId}")
    Integer selectTotalBooks(@Param("userId") Long userId);
}
