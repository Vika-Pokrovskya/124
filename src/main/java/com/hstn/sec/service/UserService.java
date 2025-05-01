package com.hstn.sec.service;

import com.hstn.sec.entity.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    // Конструктор с зависимостями
    public UserService(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }


    public User getUserInfo(String username) {
        String sql = "select full_name, phone_number from user_info where username = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{username}, (rs, rowNum) -> {
            User userInfo = new User();
            userInfo.setFullName(rs.getString("full_name"));
            userInfo.setPhoneNumber(rs.getString("phone_number"));
            return userInfo;
        });
    }
    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }


        // Остальные вставки (user_info, roles и т.д.)
    public void addUser(String username, String password, String fullName, String phoneNumber) {
        if (isUsernameExists(username)) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        else {
        // Хешируем пароль перед сохранением
        String encodedPassword = passwordEncoder.encode(password);

        String sqlUsers = "INSERT INTO users (username, password, enabled) VALUES (?, ?, ?)";
        jdbcTemplate.update(sqlUsers, username, encodedPassword, 1);

        String sqlUserInfo = "INSERT INTO user_info (username, full_name, phone_number) VALUES (?, ?, ?)";
        jdbcTemplate.update(sqlUserInfo, username, fullName, phoneNumber);

        String sqlRoles = "INSERT INTO roles (username, roles) VALUES (?, ?)";
        jdbcTemplate.update(sqlRoles, username, "ROLE_USER");}
    }


    // Метод для удаления пользователя
    public void deleteUser(String username) {
        String sqlRoles = "DELETE FROM roles WHERE username = ?";
        jdbcTemplate.update(sqlRoles, username);

        String sqlUserInfo = "DELETE FROM user_info WHERE username = ?";
        jdbcTemplate.update(sqlUserInfo, username);

        String sqlUsers = "DELETE FROM users WHERE username = ?";
        jdbcTemplate.update(sqlUsers, username);
    }

    public void migrateOldPasswords() {
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT username, password FROM users WHERE password NOT LIKE '$2a$%'"
        );

        for (Map<String, Object> user : users) {
            String username = (String) user.get("username");
            String rawPassword = (String) user.get("password");

            if (rawPassword.startsWith("{noop}")) {
                rawPassword = rawPassword.substring(6);
            }

            String encodedPassword = passwordEncoder.encode(rawPassword);
            jdbcTemplate.update(
                    "UPDATE users SET password = ? WHERE username = ?",
                    encodedPassword, username
            );
        }}
    // Метод для получения всех пользователей
    public List<User> getAllUsers() {
        String sql = "SELECT ui.username, ui.full_name, ui.phone_number, GROUP_CONCAT(r.roles) AS roles " +
                "FROM user_info ui " +
                "LEFT JOIN roles r ON ui.username = r.username " +
                "GROUP BY ui.username";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setUserName(rs.getString("username"));
            user.setFullName(rs.getString("full_name"));
            user.setPhoneNumber(rs.getString("phone_number"));
            user.setRole(rs.getString("roles"));
            return user;
        });
    }

    // Метод для обновления информации о пользователе
    public void updateUser(String username, String fullName, String phoneNumber) {
        String sql = "UPDATE user_info SET full_name = ?, phone_number = ? WHERE username = ?";
        jdbcTemplate.update(sql, fullName, phoneNumber, username);
       /* String sqlUsers = "UPDATE users SET password = ? WHERE username = ?";
        jdbcTemplate.update(sqlUsers, username);*/
    }

    public void updateRole(String username, String roles) {
        // Сначала удаляем все существующие роли для пользователя
        String deleteSql = "DELETE FROM roles WHERE username = ?";
        jdbcTemplate.update(deleteSql, username);

        // Добавляем новые роли
        if (roles != null && !roles.isEmpty()) {
            String[] rolesArray = roles.split(",");
            for (String role : rolesArray) {
                String insertSql = "INSERT INTO roles (username, roles) VALUES (?, ?)";
                jdbcTemplate.update(insertSql, username, role.trim());
            }
        }
    }

    public User getUserForAdmin(String username) {
        String sql = "SELECT ui.full_name, ui.phone_number, GROUP_CONCAT(r.roles) AS roles " +
                "FROM user_info ui " +
                "LEFT JOIN roles r ON ui.username = r.username " +
                "WHERE ui.username = ? " +
                "GROUP BY ui.full_name, ui.phone_number";
        return jdbcTemplate.queryForObject(sql, new Object[]{username}, (rs, rowNum) -> {
            User userInfo = new User();
            userInfo.setFullName(rs.getString("full_name"));
            userInfo.setPhoneNumber(rs.getString("phone_number"));
            userInfo.setRole(rs.getString("roles"));
            System.out.println("Full Name: " + userInfo.getFullName());
            System.out.println("Phone Number: " +userInfo.getPhoneNumber());
            System.out.println("Roles: " + userInfo.getRole());
            return userInfo;
        });
    }

    // Метод для получения задач работника по его username

}
