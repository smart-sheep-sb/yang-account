//用户管理模块，实现用户注册，登录，获取用户信息的HTTP请求
package cn.lazyaccount.controller;
// 导入必要的类和项目类
import cn.lazyaccount.model.User; // 用户模型类，封装用户属性
import cn.lazyaccount.service.UserService; // 用户服务类，处理用户注册/登录的业务逻辑
import cn.lazyaccount.util.JsonUtil; // JSON工具类，实现JSON与对象/Map的互转
import cn.lazyaccount.util.WebUtil; // Web工具类，封装HTTP请求/响应的通用操作
import com.sun.net.httpserver.HttpExchange; // HTTP交换对象，封装一次HTTP请求和响应
import com.sun.net.httpserver.HttpHandler; // HTTP处理器接口，自定义处理器需实现handle方法
import java.io.IOException; // IO异常，处理请求/响应的输入输出异常
import java.util.Map; // Map接口，用于存储键值对数据
import java.util.HashMap; // HashMap实现，Map接口的具体实现

public class UserController {//用户操控器，专门处理所有和用户相关的HTTP请求（注册，登录等）
    private final UserService userService = new UserService();//创建用户服务实例，处理业务逻辑
    public HttpHandler registerHandler = new HttpHandler() {//定义用户注册的HTTP处理器，实现HttpHandler接口处理POST请求
        @Override
        public void handle(HttpExchange exchange) throws IOException {//重写handle方法，核心逻辑，处理用户注册请求
            if (!"POST".equals(exchange.getRequestMethod())) {// 检查HTTP方法，只允许POST请求，非POST返回405错误
                WebUtil.sendError(exchange, "只允许POST方法", 405); // 405 Method Not Allowed
                return; // 结束处理，终止方法执行
            }
            try {//捕获注册过程中的异常
                System.out.println("处理用户注册请求");//打印日志，便于调试
                String requestBody = WebUtil.readRequestBody(exchange);//读取请求体（JSON格式）数据
                System.out.println("   请求数据: " + requestBody);//打印请求体内容
                User user = JsonUtil.fromJson(requestBody, User.class);//将JSON转换为User对象（反序列化）
                if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {// 验证必填字段：用户名不能为空（去除首尾空格后也不能为空）
                    WebUtil.sendError(exchange, "用户名不能为空", 400); // 400参数错误
                    return;
                }
                if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {// 验证必填字段：密码不能为空（去除首尾空格后也不能为空）
                    WebUtil.sendError(exchange, "密码不能为空", 400);//400参数错误
                    return;
                }
                boolean success = userService.register(user);//调用服务层register进行用户注册,返回布尔值表示是否注册成功
                if (success) { //注册成功
                    System.out.println("用户注册成功: " + user.getUsername());//打印成功日志
                    Map<String, Object> responseData = new HashMap<>(); // 创建响应数据Map
                    responseData.put("username", user.getUsername());//包含用户名
                    responseData.put("message", "注册成功");//成功提示词
                    WebUtil.sendSuccess(exchange, responseData);// 发送成功响应200，携带响应数据
                } else {//注册失败（用户名已经存在）
                    System.out.println("用户注册失败: 用户名已存在");//打印失败原因
                    WebUtil.sendError(exchange, "用户名已存在", 400);//发送400错误响应
                }
            } catch (Exception e) {//捕获所有未预期的异常
                System.err.println("用户注册过程发生异常");// 打印异常提示
                e.printStackTrace(); // 打印异常堆栈，便于定位问题
                WebUtil.sendError(exchange, "注册失败: " + e.getMessage(), 500); //发送500响应，携带具体异常信息
            }
        }
    };
    public HttpHandler loginHandler = new HttpHandler() {//定义用户登录的HTTP处理器，实现HttpHandler接口处理POST请求
        @Override
        public void handle(HttpExchange exchange) throws IOException {//重写handle方法，核心逻辑，处理用户登录请求
            if (!"POST".equals(exchange.getRequestMethod())) { // 检查HTTP方法是否为POST
                WebUtil.sendError(exchange, "只允许POST方法", 405);//非POST返回405错误
                return;
            }
            try {//捕获登录过程中的异常
                System.out.println("处理用户登录请求");//打印日志
                String requestBody = WebUtil.readRequestBody(exchange);//读取请求体中的JSON数据
                System.out.println("   请求数据: " + requestBody);//打印请求体内容
                Map<String, Object> requestMap = JsonUtil.fromJsonToMap(requestBody);//将JSON转换为Map（因为只需要username和password）
                String username =(String) requestMap.get("username");//从Map中获取用户名，强制转换为String类型
                String password =(String) requestMap.get("password");//从Map中获取密码，强制转换为String类型
                if (username == null || username.trim().isEmpty()) {//验证必填字段，用户名不能为空（去除首位空格后）
                    WebUtil.sendError(exchange, "用户名不能为空", 400);//返回400错误
                    return;
                }
                if (password == null || password.trim().isEmpty()) { // 验证必填字段：密码不能为空（去除首尾空格后）
                    WebUtil.sendError(exchange, "密码不能为空", 400);//返回400错误
                    return;
                }
                User user = userService.login(username, password);//调用服务层login方法验证登录，返回User对象（登陆成功）或null（失败）
                if (user != null) {// 根据结果返回响应
                    System.out.println("用户登录成功: " + username);//打印成功日志
                    user.setPassword(null);// 移除密码（安全考虑，不返回密码给前端）
                    WebUtil.sendSuccess(exchange, user);// 发送成功响应，包含用户信息
                } else {//登陆失败
                    System.out.println("用户登录失败: 用户名或密码错误");//打印失败原因
                    WebUtil.sendError(exchange, "用户名或密码错误", 401); //返回401未授权
                }
            } catch (Exception e) {//捕获所有未预期的异常
                System.err.println("用户登录过程发生异常");//打印异常提示
                e.printStackTrace();//打印异常堆栈
                WebUtil.sendError(exchange, "登录失败: " + e.getMessage(), 500);//返回500错误响应，携带具体异常信息
            }
        }
    };
    public HttpHandler getUserInfoHandler = new HttpHandler() {//定义获取用户信息的HTTP处理器（暂无）
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            WebUtil.sendError(exchange, "功能尚未实现", 501); //后续版本实现
        }
    };
}