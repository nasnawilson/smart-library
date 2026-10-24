package com.smartlibrary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartlibrary.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户收藏Mapper接口
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {

    /**
     * 查询用户的收藏列表
     */
    @Select("SELECT uf.*, " +
            "CASE WHEN uf.target_type = 'BOOK_LIST' THEN bl.title ELSE b.title END as targetName, " +
            "CASE WHEN uf.target_type = 'BOOK_LIST' THEN bl.cover_image ELSE b.cover_image END as targetCover " +
            "FROM user_favorite uf " +
            "LEFT JOIN book_list bl ON uf.target_type = 'BOOK_LIST' AND uf.target_id = bl.id " +
            "LEFT JOIN book b ON uf.target_type = 'BOOK' AND uf.target_id = b.id " +
            "WHERE uf.user_id = #{userId} " +
            "ORDER BY uf.create_time DESC")
    List<UserFavorite> selectByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已收藏
     */
    @Select("SELECT COUNT(*) FROM user_favorite " +
            "WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    int checkFavorite(@Param("userId") Long userId,
                      @Param("targetType") String targetType,
                      @Param("targetId") Long targetId);
}
