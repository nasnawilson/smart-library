package com.smartlibrary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("student_id")
    private String studentNo;
    private String password;
    @TableField("dept")
    private String department;
    @TableField("`class`")
    private String className;
    private Integer violationCount;
    private LocalDateTime forbidTime;

    /** 角色：真实数据库列（ADMIN=管理员，USER=普通读者） */
    private String role;

    @TableField(exist = false)
    private String name;
    @TableField(exist = false)
    private String status = "ACTIVE";
    @TableField(exist = false)
    private Integer deleted = 0;
}
