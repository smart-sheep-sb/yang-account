//基于Java内置HttpServer的轻量级Web服务器，提供API接口和静态文件服务
package cn.lazyaccount.server;
// 导入Java内置HTTP服务器相关类
import com.sun.net.httpserver.HttpServer; // HTTP服务器主类，用于创建和启动HTTP服务器
import com.sun.net.httpserver.HttpHandler; // HTTP请求处理器接口，所有请求处理器必须实现此接口
import com.sun.net.httpserver.HttpExchange; // HTTP交换对象，封装了请求和响应
import cn.lazyaccount.controller.UserController; // 用户控制器，处理用户注册，登录请求
import cn.lazyaccount.controller.BillController; // 账单控制器，处理账单增删改查等请求
import java.io.IOException; // 输入输出异常，处理文件读写和网络IO异常
import java.net.InetSocketAddress; // 网络地址类，封装IP地址和端口号
import java.util.concurrent.Executors; // 线程池执行器，用于创建线程池
import java.nio.file.Files; // 文件操作类，提供读取文件内容的方法
import java.nio.file.Paths; // 路径操作类，将字符串路径转换为Path对象
import java.io.OutputStream; // 输出流，用于向客户端发送响应数据
import java.io.File; // 导入File类用于路径调试

public class SimpleHttpServer {//简易HTTP服务器类
    private HttpServer server; // Java内置的HTTP服务器实例，负责监听端口，分发请求
    private final int port; // 服务器监听的端口号，例如8080

    public SimpleHttpServer(int port) {//构造方法，服务器监听的端口号
        this.port = port; // 保存端口号到成员变量
        System.out.println("创建HTTP服务器，端口: " + port);//控制台输出启动信息
    }

    public void start() throws IOException {//启动服务器
        // 创建HTTP服务器实例，绑定到指定端口
        // 参数1：InetSocketAddress 指定IP和端口（null表示所有IP）
        // 参数2：backlog队列长度，0表示使用默认值
        server = HttpServer.create(new InetSocketAddress(port), 0);
        System.out.println("HTTP服务器实例创建成功");//控制台输出成功信息

        UserController userController = new UserController(); // 用户相关控制器
        BillController billController = new BillController(); // 账单相关控制器

        System.out.println("注册API路由...");//控制台输出路由注册开始
        // 注册接口：POST /api/user/register
        server.createContext("/api/user/register", userController.registerHandler);//用户相关接口
        System.out.println("  POST /api/user/register - 用户注册");//控制台输出录音信息
        // 登录接口：POST /api/user/login
        server.createContext("/api/user/login", userController.loginHandler);//用户登录处理器
        System.out.println("  POST /api/user/login - 用户登录");//控制台输出路由信息
        // 添加账单：POST /api/bill/add
        server.createContext("/api/bill/add", billController.addBillHandler);// 账单相关接口
        System.out.println("  POST /api/bill/add - 添加账单");//控制台输出路由信息
        // 删除账单：DELETE /api/bill/delete/{id}
        server.createContext("/api/bill/delete/", billController.deleteBillHandler);//删除账单处理器
        System.out.println("  DELETE /api/bill/delete/{id} - 删除账单");//控制台输出路由信息
        // 获取账单列表：GET /api/bill/list
        server.createContext("/api/bill/list", billController.getBillsHandler);//获取账单列表处理器
        System.out.println("  GET /api/bill/list - 获取账单列表");//控制台输出路由信息
        // 获取分类列表：GET /api/bill/categories
        server.createContext("/api/bill/categories", billController.getCategoriesHandler);//获取分类列表处理器
        System.out.println("  GET /api/bill/categories - 获取分类列表");//控制台输出路由信息
        // 根路径处理：所有非API请求都交给静态文件处理器
        server.createContext("/", new StaticFileHandler());// 静态文件服务（用于访问HTML页面）
        System.out.println("  GET / - 静态文件服务");//控制台输出路由信息
        // 设置线程池：创建固定大小的线程池处理并发请求
        server.setExecutor(Executors.newFixedThreadPool(10)); // 创建固定大小的线程池，10个线程处理并发请求
        System.out.println("线程池已创建（10个线程）");//控制台输出线程池信息
        // 启动服务器（非阻塞，立即返回）
        server.start(); // 启动服务器（非阻塞，立即返回）
        System.out.println("HTTP服务器已启动在端口 " + port);// 控制台输出服务器启动成功
        System.out.println("当前工作目录: " + System.getProperty("user.dir"));// 打印工作目录，方便调试
        server.createContext("/api/bill/update", billController.updateBillHandler);
        System.out.println("  PUT /api/bill/update - 更新账单");//更新账单的路由
    }

    public void stop() {//停止服务器
        if (server != null) {//检查服务器实例是否存在
            server.stop(0);// 停止服务器，0表示立即停止，不等待现有请求完成
            System.out.println("HTTP服务器已停止");//控制台输出停止信息
        } else {
            System.out.println("服务器实例为null，无需停止");//服务器未启动的情况
        }
    }

    public int getPort() {//获取端口号
        return port; // 返回端口号
    }

    public boolean isRunning() {//检查服务器运行状态
        return server != null; // 如果server不为null，表示正在运行
    }

    static class StaticFileHandler implements HttpHandler {//静态文件处理器内部类
        @Override
        public void handle(HttpExchange exchange) throws IOException {//处理HTTP请求
            String path = exchange.getRequestURI().getPath();// 获取请求的URL路径
            System.out.println("请求静态文件: " + path);//控制台输出请求路径

            // 处理favicon.ico（只处理一次）
            if(path.equals("/favicon.ico")){
                exchange.getResponseHeaders().set("Content-Type" , "image/x-icon");//设置响应头，内容类型为图表
                exchange.sendResponseHeaders(204 , -1);//-1表示没有响应体
                System.out.println("处理favicon请求");
                return;//结束处理，不继续执行
            }

            // 处理API请求
            if (path.startsWith("/api/")) {
                sendNotFound(exchange);// API请求返回404，因为应该在API路由中处理
                return; // 结束处理
            }

            // 处理各种静态文件
            if (path.equals("/") || path.equals("/index.html")) {
                serveFile(exchange, "webapp/index.html", "text/html");//返回首页HTML
            }
            else if (path.equals("/register.html")) {
                serveFile(exchange, "webapp/register.html", "text/html");//返回注册页
            }
            else if (path.equals("/main.html")) {
                serveFile(exchange, "webapp/main.html", "text/html");//返回主页面HTML
            }
            else if (path.startsWith("/css/") && path.endsWith(".css")) {
                // 关键修复：CSS文件路径处理
                serveFile(exchange, "webapp" + path, "text/css");//返回CSS文件
            }
            else if (path.startsWith("/js/") && path.endsWith(".js")) {
                // JS文件路径处理
                serveFile(exchange, "webapp" + path, "application/javascript");//返回JS文件
            }
            else if (path.endsWith(".css")) {
                // 兼容旧版：直接放在根目录的CSS
                serveFile(exchange, "webapp/css" + path, "text/css");
            }
            else if (path.endsWith(".js")) {
                // 兼容旧版：直接放在根目录的JS
                serveFile(exchange, "webapp/js" + path, "application/javascript");
            }
            else if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".ico")) {
                String contentType = path.endsWith(".png") ? "image/png" :
                        path.endsWith(".jpg") ? "image/jpeg" : "image/x-icon";
                serveFile(exchange, "webapp" + path, contentType);//返回图片文件
            }
            else {
                sendNotFound(exchange); // 发送404响应
            }
        }

        private void serveFile(HttpExchange exchange, String filePath, String contentType) throws IOException {//发送文件内容到客户端
            try {
                System.out.println("尝试读取文件: " + filePath);// 调试输出
                File file = new File(filePath);
                System.out.println("绝对路径: " + file.getAbsolutePath());// 打印绝对路径

                byte[] fileContent = Files.readAllBytes(Paths.get(filePath)); // 读取文件内容为字节数组

                exchange.getResponseHeaders().set("Content-Type", contentType);// 设置内容类型
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");//允许跨域访问
                exchange.sendResponseHeaders(200, fileContent.length);// 发送响应头（200状态码，内容长度）

                OutputStream os = exchange.getResponseBody();// 获取响应输出流并写入文件内容
                os.write(fileContent);// 写入文件内容
                os.close();// 关闭输出流

                System.out.println("成功发送文件: " + filePath + " (" + fileContent.length + " 字节)");//控制台输出
            } catch (IOException e) {//捕获异常
                System.err.println("无法读取文件: " + filePath);//错误输出
                System.err.println("错误信息: " + e.getMessage());//错误详情
                System.err.println("当前工作目录: " + System.getProperty("user.dir"));// 打印工作目录
                sendNotFound(exchange); // 发送404响应
            }
        }

        private void sendNotFound(HttpExchange exchange) throws IOException {//发送404页面未找到响应
            String response = "<html><body><h1>404 - 页面未找到</h1>" +// 404页面HTML内容
                    "<p>请求的页面不存在: " + exchange.getRequestURI().getPath() + "</p>" +
                    "<p><a href='/index.html'>返回首页</a></p></body></html>";

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 设置响应头
            byte[] responseBytes = response.getBytes("UTF-8");//将响应字符串转换为字节数组
            exchange.sendResponseHeaders(404, responseBytes.length);//发送404状态码和响应体长度

            OutputStream os = exchange.getResponseBody();// 写入响应内容
            os.write(responseBytes);
            os.close();

            System.out.println("返回404: " + exchange.getRequestURI().getPath());//控制台输出
        }
    }
}