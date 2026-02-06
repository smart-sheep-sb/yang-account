package cn.lazyaccount.server;
// 导入Java内置HTTP服务器相关类
import com.sun.net.httpserver.HttpServer; // HTTP服务器主类
import com.sun.net.httpserver.HttpHandler; // HTTP请求处理器接口
import com.sun.net.httpserver.HttpExchange; // HTTP交换对象
import cn.lazyaccount.controller.UserController; // 用户控制器
import cn.lazyaccount.controller.BillController; // 账单控制器
import java.io.IOException; // 输入输出异常
import java.net.InetSocketAddress; // 网络地址类
import java.util.concurrent.Executors; // 线程池执行器
import java.nio.file.Files; // 文件操作类
import java.nio.file.Paths; // 路径操作类
import java.io.OutputStream; // 输出流

public class SimpleHttpServer {
    private HttpServer server; // Java内置的HTTP服务器实例
    private final int port; // 服务器监听的端口号
    public SimpleHttpServer(int port) {
        this.port = port; // 保存端口号
        System.out.println("创建HTTP服务器，端口: " + port);
    }
    public void start() throws IOException {
        // 创建HTTP服务器实例，绑定到指定端口
        // 参数1：InetSocketAddress 指定IP和端口（null表示所有IP）
        // 参数2：backlog队列长度，0表示使用默认值
        server = HttpServer.create(new InetSocketAddress(port), 0);
        System.out.println("HTTP服务器实例创建成功");
        // 创建控制器实例
        UserController userController = new UserController(); // 用户相关控制器
        BillController billController = new BillController(); // 账单相关控制器
        System.out.println("注册API路由...");
        server.createContext("/api/user/register", userController.registerHandler);// 用户相关接口
        System.out.println("  POST /api/user/register - 用户注册");
        server.createContext("/api/user/login", userController.loginHandler);
        System.out.println("  POST /api/user/login - 用户登录");
        server.createContext("/api/bill/add", billController.addBillHandler);// 账单相关接口
        System.out.println("  POST /api/bill/add - 添加账单");
        server.createContext("/api/bill/delete/", billController.deleteBillHandler);
        System.out.println("  DELETE /api/bill/delete/{id} - 删除账单");
        server.createContext("/api/bill/list", billController.getBillsHandler);
        System.out.println("  GET /api/bill/list - 获取账单列表");
        server.createContext("/api/bill/categories", billController.getCategoriesHandler);
        System.out.println("  GET /api/bill/categories - 获取分类列表");
        server.createContext("/", new StaticFileHandler());// 静态文件服务（用于访问HTML页面）
        System.out.println("  GET / - 静态文件服务");
        server.setExecutor(Executors.newFixedThreadPool(10)); // 创建固定大小的线程池，10个线程处理并发请求
        System.out.println("线程池已创建（10个线程）");
        server.start(); // 启动服务器（非阻塞，立即返回）
        System.out.println("HTTP服务器已启动在端口 " + port);
    }
    public void stop() {
        if (server != null) {
            server.stop(0);// 停止服务器，0表示立即停止
            System.out.println("HTTP服务器已停止");
        } else {
            System.out.println("服务器实例为null，无需停止");
        }
    }
    public int getPort() {
        return port; // 返回端口号
    }
    public boolean isRunning() {
        return server != null; // 如果server不为null，表示正在运行
    }
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();// 获取请求的路径
            System.out.println("请求静态文件: " + path);
            if(path.equals("/favicon.ico")){//返回一个空的图标响应，避免404错误
                exchange.getResponseHeaders().set("Content-Type" , "image/x-icon");
                exchange.sendResponseHeaders(204 , -1);
                return;
            }
            if (path.startsWith("/api/")) {// 处理API请求（交给API处理器）
                sendNotFound(exchange);// API请求返回404，因为应该在API路由中处理
                return; // 结束处理
            }
            if (path.equals("/") || path.equals("/index.html")) {// 处理根路径或index.html
                serveFile(exchange, "webapp/index.html", "text/html");
            }
            else if (path.equals("/register.html")) {// 处理注册页面
                serveFile(exchange, "webapp/register.html", "text/html");
            }
            else if (path.equals("/favicon.ico")){
                exchange.getResponseHeaders().set("Content-Type" , "image/x-icon");//返回204 NO Content，让浏览器停止请求
                exchange.sendResponseHeaders(204 , -1);
                System.out.println("阻止favicon请求");
                return;
            }
            else if (path.equals("/main.html")) {// 处理主页面
                serveFile(exchange, "webapp/main.html", "text/html");
            }
            else if (path.endsWith(".css")) {// 处理CSS文件
                serveFile(exchange, "webapp" + path, "text/css");
            }
            else if (path.endsWith(".js")) {// 处理JS文件
                serveFile(exchange, "webapp" + path, "application/javascript");
            }
            else if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".ico")) {// 处理图片文件
                String contentType = path.endsWith(".png") ? "image/png" :
                        path.endsWith(".jpg") ? "image/jpeg" : "image/x-icon";
                serveFile(exchange, "webapp" + path, contentType);
            }
            else {// 其他文件或路径不存在
                sendNotFound(exchange); // 发送404响应
            }
        }
        private void serveFile(HttpExchange exchange, String filePath, String contentType) throws IOException {
            try {
                byte[] fileContent = Files.readAllBytes(Paths.get(filePath)); // 读取文件内容为字节数组
                exchange.getResponseHeaders().set("Content-Type", contentType);// 设置响应头
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, fileContent.length);// 发送响应头（200状态码，内容长度）
                OutputStream os = exchange.getResponseBody();// 获取响应输出流
                os.write(fileContent);// 写入文件内容
                os.close();// 关闭输出流
                System.out.println("发送文件: " + filePath + " (" + fileContent.length + " 字节)");
            } catch (IOException e) {
                System.err.println("无法读取文件: " + filePath);// 文件不存在或其他IO错误
                System.err.println("错误信息: " + e.getMessage());
                sendNotFound(exchange); // 发送404
            }
        }
        private void sendNotFound(HttpExchange exchange) throws IOException {
            String response = "<html><body><h1>404 - 页面未找到</h1>" +// 404页面HTML内容
                    "<p>请求的页面不存在: " + exchange.getRequestURI().getPath() + "</p>" +
                    "<p><a href='/index.html'>返回首页</a></p></body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 设置响应头
            byte[] responseBytes = response.getBytes("UTF-8");// 发送404状态码
            exchange.sendResponseHeaders(404, responseBytes.length);
            OutputStream os = exchange.getResponseBody();// 写入响应内容
            os.write(responseBytes);
            os.close();
            System.out.println("返回404: " + exchange.getRequestURI().getPath());
        }
    }
}