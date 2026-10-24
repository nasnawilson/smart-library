package com.smartlibrary.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 用户表启动迁移器。
 * 每次启动都会执行，所有动作都是幂等的（重复执行不会出问题）：
 * 1. 老数据库没有 role 列 → 自动补上（默认 USER）
 * 2. 没有管理员账号 → 自动创建 admin / admin（BCrypt 加密存储，角色 ADMIN）
 * 3. 老数据里的明文密码 → 自动改成 BCrypt 哈希
 */
@Component
@Order(1)
public class UserDataMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserDataMigration.class);

    private static final String ADMIN_ACCOUNT = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        try {
            ensureRoleColumn();
            ensureAdminAccount();
            upgradePlainPasswords();
        } catch (Exception e) {
            log.warn("用户表启动迁移未完成（若数据库还没建表可忽略）：{}", e.getMessage());
        }
    }

    /** 补 role 列 */
    private void ensureRoleColumn() {
        try {
            jdbcTemplate.execute("SELECT role FROM sys_user WHERE 1 = 0");
        } catch (Exception e) {
            jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER'");
            log.info("已为 sys_user 表新增 role 列（默认 USER）");
        }
    }

    /** 确保管理员账号存在且角色正确 */
    private void ensureAdminAccount() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE student_id = ?", Integer.class, ADMIN_ACCOUNT);
        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO sys_user (student_id, password, dept, violation_count, role) VALUES (?, ?, ?, ?, ?)",
                    ADMIN_ACCOUNT, passwordEncoder.encode(ADMIN_PASSWORD), "系统管理", 0, "ADMIN");
            log.info("已创建管理员账号 admin（初始密码 admin，请登录后尽快修改）");
            return;
        }
        jdbcTemplate.update("UPDATE sys_user SET role = 'ADMIN' WHERE student_id = ?", ADMIN_ACCOUNT);
    }

    /** 明文密码升级为 BCrypt 哈希 */
    private void upgradePlainPasswords() {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT id, password FROM sys_user");
        int upgraded = 0;
        for (Map<String, Object> row : users) {
            Object id = row.get("id");
            Object password = row.get("password");
            if (id == null || password == null) {
                continue;
            }
            String stored = String.valueOf(password);
            if (isHashed(stored)) {
                continue;
            }
            jdbcTemplate.update("UPDATE sys_user SET password = ? WHERE id = ?",
                    passwordEncoder.encode(stored), id);
            upgraded++;
        }
        if (upgraded > 0) {
            log.info("已把 {} 个账号的明文密码升级为 BCrypt 哈希", upgraded);
        }
    }

    private boolean isHashed(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }
}
