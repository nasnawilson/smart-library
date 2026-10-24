package com.smartlibrary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户违规记录实体类
 * 对应数据库表: user_violation
 */
@Data
@TableName("user_violation")
public class UserViolation {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 违规类型：NO_CHECK_IN-预约未签到，OVERDUE_RETURN-图书逾期未还，TEMP_LEAVE_TIMEOUT-暂离超时未归 */
    private String type;

    /** 关联预约记录ID（座位违规时） */
    private Long reservationId;

    /** 关联借阅记录ID（图书逾期时） */
    private Long borrowId;

    /** 违规时间 */
    private LocalDateTime violationTime;

    /** 是否已处理：0-未处理，1-已处理 */
    private Integer isProcessed;

    /** 备注说明 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    // ========== 非数据库字段，用于关联查询展示 ==========

    /** 违规者姓名 */
    private transient String userName;

    /** 违规者学号 */
    private transient String studentNo;
}
