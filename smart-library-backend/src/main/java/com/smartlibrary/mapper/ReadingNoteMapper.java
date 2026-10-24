package com.smartlibrary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartlibrary.entity.ReadingNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 读书笔记Mapper接口
 */
@Mapper
public interface ReadingNoteMapper extends BaseMapper<ReadingNote> {

    /**
     * 查询用户的读书笔记（带图书信息）
     */
    @Select("SELECT rn.*, b.title as bookTitle, b.author as bookAuthor, u.name as userName " +
            "FROM reading_note rn " +
            "LEFT JOIN book b ON rn.book_id = b.id " +
            "LEFT JOIN sys_user u ON rn.user_id = u.id " +
            "WHERE rn.user_id = #{userId} AND rn.deleted = 0 " +
            "ORDER BY rn.create_time DESC")
    List<ReadingNote> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询某图书的所有笔记
     */
    @Select("SELECT rn.*, b.title as bookTitle, b.author as bookAuthor, u.name as userName " +
            "FROM reading_note rn " +
            "LEFT JOIN book b ON rn.book_id = b.id " +
            "LEFT JOIN sys_user u ON rn.user_id = u.id " +
            "WHERE rn.book_id = #{bookId} AND rn.deleted = 0 " +
            "ORDER BY rn.create_time DESC")
    List<ReadingNote> selectByBookId(@Param("bookId") Long bookId);

    /**
     * 查询笔记详情
     */
    @Select("SELECT rn.*, b.title as bookTitle, b.author as bookAuthor, u.name as userName " +
            "FROM reading_note rn " +
            "LEFT JOIN book b ON rn.book_id = b.id " +
            "LEFT JOIN sys_user u ON rn.user_id = u.id " +
            "WHERE rn.id = #{id} AND rn.deleted = 0")
    ReadingNote selectByIdWithDetail(@Param("id") Long id);
}
