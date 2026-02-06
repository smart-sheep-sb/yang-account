package cn.lazyaccount.service;

import cn.lazyaccount.dao.UserDao;
import cn.lazyaccount.model.User;
public class UserService {
    private final UserDao userDao = new UserDao();// 用户数据访问对象实例
    public boolean register(User user) {
        System.out.println("检查用户名是否存在: " + user.getUsername());
        if (userDao.isUsernameExists(user.getUsername())) {//检查用户名是否已存在
            System.out.println("❌ 用户名已存在: " + user.getUsername());
            return false; // 注册失败
        }
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {//如果没有昵称，使用用户名作为昵称
            user.setNickname(user.getUsername()); // 设置昵称为用户名
        }
        System.out.println("添加新用户: " + user.getUsername());//添加用户到数据库
        boolean success = userDao.addUser(user);
        if (success) {
            System.out.println("用户注册成功: " + user.getUsername());
        } else {
            System.out.println("用户注册失败（数据库错误）");
        }
        return success; // 返回操作结果
    }
    public User login(String username, String password) {
        System.out.println("用户登录尝试: " + username);
        User user = userDao.login(username, password);// 调用DAO层验证登录
        if (user != null) {
            System.out.println("用户登录成功: " + username);
        } else {
            System.out.println("用户登录失败: " + username);
        }
        return user; // 返回用户对象或null
    }
    public boolean checkUsernameExists(String username) {
        return userDao.isUsernameExists(username); // 直接调用DAO方法
    }
}