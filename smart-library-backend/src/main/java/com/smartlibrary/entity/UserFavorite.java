package com.smartlibrary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 * 对应数据库表: user_favorite
 */
@Data
@TableName("user_favorite")
public class UserFavorite {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 收藏目标类型：BOOK_LIST-书单，BOOK-图书 */
    private String targetType;

    /** 收藏目标ID */
    private Long targetId;

    /** 创建时间 */
    private LocalDateTime createTime;

    // ========== 非数据库字段，用于关联查询展示 ==========

    /** 收藏目标名称 */
    private transient String targetName;

    /** 收藏目标封面 */
    private transient String targetCover;
}
