package cn.lazyaccount.dao;

import cn.lazyaccount.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {
    public User findByUsername(String username) {
        String sql = "SELECT id, username, password, nickname, create_time FROM t_user WHERE username = ?";// SQL查询语句：根据用户名查找用户
        return BaseDao.queryForObject(sql, new BaseDao.RowMapper<User>() {// 使用BaseDao执行查询并映射结果
            @Override
            public User mapRow(ResultSet rs) throws SQLException {
                User user = new User();// 创建User对象并设置属性
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setNickname(rs.getString("nickname"));
                user.setCreateTime(rs.getTimestamp("create_time"));
                return user; // 返回用户对象
            }
        }, username); // username是SQL参数
    }
    public boolean addUser(User user) {// SQL插入语句
        String sql = "INSERT INTO t_user (username, password, nickname) VALUES (?, ?, ?)";
        int result = BaseDao.executeUpdate(sql,// 执行插入操作，返回受影响的行数
                user.getUsername(), // 第一个参数：用户名
                user.getPassword(), // 第二个参数：密码
                user.getNickname() != null ? user.getNickname() : user.getUsername() // 第三个参数：昵称
        );
        return result > 0;// 受影响行数大于0表示成功
    }
    public User login(String username, String password) {
        User user = findByUsername(username);// 先根据用户名查找用户
        if (user != null && user.getPassword().equals(password)) {// 检查用户是否存在且密码匹配
            return user; // 登录成功
        }
        return null; // 登录失败
    }
    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM t_user WHERE username = ?";// SQL查询：统计指定用户名的数量
        Integer count = BaseDao.queryForObject(sql, new BaseDao.RowMapper<Integer>() {// 执行查询，返回计数
            @Override
            public Integer mapRow(ResultSet rs) throws SQLException {
                return rs.getInt(1); // 获取第一列的值（COUNT(*)的结果）
            }
        }, username);
        return count != null && count > 0;// count不为null且大于0表示用户名已存在
    }
    public boolean updateUser(User user) {
        String sql = "UPDATE t_user SET nickname = ? WHERE id = ?";// SQL更新语句
        int result = BaseDao.executeUpdate(sql,// 执行更新操作
                user.getNickname(),
                user.getId()
        );
        return result > 0; // 返回操作结果
    }
}