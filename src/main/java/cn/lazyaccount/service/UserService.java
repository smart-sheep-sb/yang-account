//用户业务逻辑层，处理注册，登录等用户身份相关的业务流程
package cn.lazyaccount.service;

import cn.lazyaccount.dao.UserDao;//用户数据访问对象，负责用户表的数据库操作
import cn.lazyaccount.model.User;//用户实体类
public class UserService {//用户业务逻辑类
    private final UserDao userDao = new UserDao();//用户数据访问对象实例，负责t_user表的CRUD操作
    public boolean register(User user) {//用户注册业务
        System.out.println("检查用户名是否存在: " + user.getUsername());//检查用户名是否存在
        if (userDao.isUsernameExists(user.getUsername())) {//调用DAO层查询用户名是否存在
            System.out.println("❌ 用户名已存在: " + user.getUsername());//用户名已存在
            return false; // 注册失败，用户名被占用
        }
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {//如果没有昵称，使用用户名作为昵称，处理昵称默认值
            user.setNickname(user.getUsername()); // 设置昵称为用户名
        }
        System.out.println("添加新用户: " + user.getUsername());//添加用户到数据库
        boolean success = userDao.addUser(user);//调用DAO层执行数据库插入操作
        if (success) {//记录日志
            System.out.println("用户注册成功: " + user.getUsername());
        } else {
            System.out.println("用户注册失败（数据库错误）");
        }
        return success; // 返回操作结果
    }
    public User login(String username, String password) {//用户登录业务
        System.out.println("用户登录尝试: " + username);//记录登录尝试
        User user = userDao.login(username, password);// 调用DAO层验证登录（同时完成查询和密码比对）
        if (user != null) {//返回结果并记录日志
            System.out.println("用户登录成功: " + username);//登录成功
        } else {
            System.out.println("用户登录失败: " + username);//用户名不存在或密码错误
        }
        return user; // 返回用户对象或null
    }
    public boolean checkUsernameExists(String username) {//检查用户名是否已经存在
        return userDao.isUsernameExists(username); // 直接调用DAO方法，返回用户名是否存在
    }
}