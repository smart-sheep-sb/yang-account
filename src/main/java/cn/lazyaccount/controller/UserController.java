package cn.lazyaccount.controller;
// 导入必要的类和项目类
import cn.lazyaccount.model.User; // 用户模型类
import cn.lazyaccount.service.UserService; // 用户服务类
import cn.lazyaccount.util.JsonUtil; // JSON工具类
import cn.lazyaccount.util.WebUtil; // Web工具类
import com.sun.net.httpserver.HttpExchange; // HTTP交换对象
import com.sun.net.httpserver.HttpHandler; // HTTP处理器接口
import java.io.IOException; // IO异常
import java.util.Map; // Map接口
import java.util.HashMap; // HashMap实现

public class UserController {
    private final UserService userService = new UserService();// 用户服务实例，处理业务逻辑
    public HttpHandler registerHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {// 检查HTTP方法，只允许POST请求
                WebUtil.sendError(exchange, "只允许POST方法", 405); // 405 Method Not Allowed
                return; // 结束处理
            }
            try {
                System.out.println("处理用户注册请求");
                String requestBody = WebUtil.readRequestBody(exchange);//读取请求体（JSON格式）
                System.out.println("   请求数据: " + requestBody);
                User user = JsonUtil.fromJson(requestBody, User.class);//将JSON转换为User对象
                if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                    WebUtil.sendError(exchange, "用户名不能为空", 400); // 400 Bad Request
                    return;
                }
                if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                    WebUtil.sendError(exchange, "密码不能为空", 400);
                    return;
                }
                boolean success = userService.register(user);//调用服务层进行用户注册
                if (success) { //根据结果返回响应
                    System.out.println("用户注册成功: " + user.getUsername());
                    Map<String, Object> responseData = new HashMap<>(); // 创建响应数据
                    responseData.put("username", user.getUsername());
                    responseData.put("message", "注册成功");
                    WebUtil.sendSuccess(exchange, responseData);// 发送成功响应
                } else {
                    System.out.println("用户注册失败: 用户名已存在");
                    WebUtil.sendError(exchange, "用户名已存在", 400);
                }
            } catch (Exception e) {
                System.err.println("用户注册过程发生异常");// 处理所有异常
                e.printStackTrace(); // 打印异常堆栈（调试用）
                WebUtil.sendError(exchange, "注册失败: " + e.getMessage(), 500); // 500 Internal Server Error
            }
        }
    };
    public HttpHandler loginHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) { // 检查HTTP方法
                WebUtil.sendError(exchange, "只允许POST方法", 405);
                return;
            }
            try {
                System.out.println("处理用户登录请求");
                String requestBody = WebUtil.readRequestBody(exchange);//读取请求体
                System.out.println("   请求数据: " + requestBody);
                Map<String, Object> requestMap = JsonUtil.fromJsonToMap(requestBody);//将JSON转换为Map（因为只需要username和password）
                String username =(String) requestMap.get("username");//获取用户名和密码,从Object转换为String
                String password =(String) requestMap.get("password");
                if (username == null || username.trim().isEmpty()) {//验证必要字段
                    WebUtil.sendError(exchange, "用户名不能为空", 400);
                    return;
                }
                if (password == null || password.trim().isEmpty()) {
                    WebUtil.sendError(exchange, "密码不能为空", 400);
                    return;
                }
                User user = userService.login(username, password);//调用服务层进行登录验证
                if (user != null) {// 根据结果返回响应
                    System.out.println("用户登录成功: " + username);
                    user.setPassword(null);// 移除密码（安全考虑，不返回密码给前端）
                    WebUtil.sendSuccess(exchange, user);// 发送成功响应，包含用户信息
                } else {
                    System.out.println("用户登录失败: 用户名或密码错误");
                    WebUtil.sendError(exchange, "用户名或密码错误", 401); // 401 Unauthorized
                }
            } catch (Exception e) {
                System.err.println("用户登录过程发生异常");// 处理异常
                e.printStackTrace();
                WebUtil.sendError(exchange, "登录失败: " + e.getMessage(), 500);
            }
        }
    };
    public HttpHandler getUserInfoHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            WebUtil.sendError(exchange, "功能尚未实现", 501); // 501 Not Implemented // TODO: 后续版本实现
        }
    };
}