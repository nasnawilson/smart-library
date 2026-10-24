package com.smartlibrary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartlibrary.entity.UserViolation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户违规记录Mapper接口
 */
@Mapper
public interface UserViolationMapper extends BaseMapper<UserViolation> {

    /**
     * 查询用户的违规记录
     */
    @Select("SELECT uv.*, u.name as userName, u.student_no as studentNo " +
            "FROM user_violation uv " +
            "LEFT JOIN sys_user u ON uv.user_id = u.id " +
            "WHERE uv.user_id = #{userId} " +
            "ORDER BY uv.violation_time DESC")
    List<UserViolation> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询未处理的违规记录
     */
    @Select("SELECT uv.*, u.name as userName, u.student_no as studentNo " +
            "FROM user_violation uv " +
            "LEFT JOIN sys_user u ON uv.user_id = u.id " +
            "WHERE uv.is_processed = 0 " +
            "ORDER BY uv.violation_time DESC")
    List<UserViolation> selectUnprocessed();

    /**
     * 标记违规已处理
     */
    @Update("UPDATE user_violation SET is_processed = 1 WHERE id = #{id}")
    int markProcessed(@Param("id") Long id);
}
