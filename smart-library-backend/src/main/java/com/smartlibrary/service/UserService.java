package com.smartlibrary.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlibrary.dto.LoginDTO;
import com.smartlibrary.dto.LoginResponseDTO;
import com.smartlibrary.dto.PageResult;
import com.smartlibrary.dto.RegisterDTO;
import com.smartlibrary.entity.User;
import com.smartlibrary.exception.BusinessException;
import com.smartlibrary.mapper.UserMapper;
import com.smartlibrary.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    /** 管理员固定账号（不再绑定某个学号） */
    public static final String ADMIN_ACCOUNT = "admin";
    /** 读者账号：11 位学号 */
    private static final String STUDENT_NO_PATTERN = "\\d{11}";
    /** 密码规则：4-32 位字母或数字（老账号的 6 位数字密码同样适用） */
    private static final String PASSWORD_PATTERN = "[A-Za-z0-9]{4,32}";
    private static final String PASSWORD_ERROR = "密码为 4-32 位字母或数字，请重新输入";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 判断账号是否为内置管理员账号 */
    public static boolean isAdminAccount(String account) {
        return account != null && ADMIN_ACCOUNT.equalsIgnoreCase(account.trim());
    }

    public LoginResponseDTO login(LoginDTO loginDTO) {
        String account = loginDTO.getStudentNo() == null ? "" : loginDTO.getStudentNo().trim();
        if (isAdminAccount(account)) {
            account = ADMIN_ACCOUNT;
        } else if (!account.matches(STUDENT_NO_PATTERN)) {
            throw new BusinessException("学号必须是11位阿拉伯数字");
        }
        validatePassword(loginDTO.getPassword());

        User user = baseMapper.selectOne(new QueryWrapper<User>().eq("student_id", account));
        if (user == null) {
            throw new BusinessException("账号未注册，请先注册");
        }
        if (!passwordMatches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误，请重新输入");
        }
        // 老库里是明文密码：登录成功后顺手升级成 BCrypt 哈希
        if (!isHashed(user.getPassword())) {
            upgradePassword(user.getId(), loginDTO.getPassword());
        }
        fillView(user);
        String token = jwtUtil.generateToken(user.getId(), user.getStudentNo(), user.getRole());
        LoginResponseDTO response = new LoginResponseDTO();
        user.setPassword(null);
        response.setToken(token);
        response.setUserInfo(user);
        return response;
    }

    public void register(RegisterDTO dto) {
        String studentNo = dto.getStudentNo() == null ? "" : dto.getStudentNo().trim();
        if (isAdminAccount(studentNo)) {
            throw new BusinessException("该账号名为系统保留，请更换学号注册");
        }
        if (!studentNo.matches(STUDENT_NO_PATTERN)) {
            throw new BusinessException("学号必须是11位阿拉伯数字");
        }
        validatePassword(dto.getPassword());
        if (baseMapper.selectOne(new QueryWrapper<User>().eq("student_id", studentNo)) != null) {
            throw new BusinessException("学号已存在");
        }
        User user = new User();
        user.setStudentNo(studentNo);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setDepartment(dto.getDepartment());
        user.setClassName(dto.getClassName());
        user.setViolationCount(0);
        user.setRole("USER");
        save(user);
    }

    public void createUser(User user) {
        String studentNo = user.getStudentNo() == null ? "" : user.getStudentNo().trim();
        if (isAdminAccount(studentNo)) {
            throw new BusinessException("该账号名为系统保留，请更换学号");
        }
        if (!studentNo.matches(STUDENT_NO_PATTERN)) {
            throw new BusinessException("学号必须是11位阿拉伯数字");
        }
        validatePassword(user.getPassword());
        if (baseMapper.selectOne(new QueryWrapper<User>().eq("student_id", studentNo)) != null) {
            throw new BusinessException("学号已存在");
        }
        user.setStudentNo(studentNo);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        if (user.getViolationCount() == null) {
            user.setViolationCount(0);
        }
        save(user);
    }

    public PageResult<User> pageQuery(Integer page, Integer size, String name, String studentNo, String role) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (studentNo != null && !studentNo.isEmpty()) wrapper.like("student_id", studentNo);
        if (role != null && !role.isEmpty()) wrapper.eq("role", role);
        wrapper.orderByAsc("id");
        Page<User> result = page(new Page<>(page, size), wrapper);
        result.getRecords().forEach(u -> {
            fillView(u);
            u.setPassword(null);
        });
        return PageResult.of(result);
    }

    public User getById(Long id) {
        User user = super.getById(id);
        if (user != null) {
            fillView(user);
            user.setPassword(null);
        }
        return user;
    }

    public void updateUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            validatePassword(user.getPassword());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        if (user.getRole() != null && user.getRole().isBlank()) {
            user.setRole(null);
        }
        updateById(user);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        validatePassword(newPassword);
        User user = super.getById(userId);
        if (user == null || !passwordMatches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        upgradePassword(userId, newPassword);
    }

    /** 把某个账号的密码重设为 BCrypt 哈希 */
    public void upgradePassword(Long userId, String rawPassword) {
        User update = new User();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(rawPassword));
        updateById(update);
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isHashed(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        // 兼容历史明文密码
        return rawPassword.equals(storedPassword);
    }

    private boolean isHashed(String password) {
        return password != null
                && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }

    private void validatePassword(String password) {
        if (password == null || !password.matches(PASSWORD_PATTERN)) {
            throw new BusinessException(PASSWORD_ERROR);
        }
    }

    private void fillView(User user) {
        user.setName(user.getStudentNo());
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        user.setStatus(user.getForbidTime() == null ? "ACTIVE" : "DISABLED");
    }
}
