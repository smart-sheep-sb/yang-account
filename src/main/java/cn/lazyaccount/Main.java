package cn.lazyaccount;
// 导入必要的类和项目类
import cn.lazyaccount.server.SimpleHttpServer; // HTTP服务器类
import java.io.IOException; // IO异常类

public class Main {
    public static void main(String[] args) {// 打印启动信息
        System.out.println("========================================");
        System.out.println("  记账管理系统 v1.0");
        System.out.println("  作者: 羊少博");
        System.out.println("  时间: 2026年");
        System.out.println("========================================");
        // 定义服务器端口
        int port = 8080; // HTTP默认端口是80，这里使用8080避免冲突
        try {// 创建HTTP服务器实例
            System.out.println("正在启动HTTP服务器...");
            SimpleHttpServer server = new SimpleHttpServer(port);
            server.start();// 启动服务器
            printUsageInfo(port);// 打印使用说明
            addShutdownHook(server);// 添加关闭钩子（程序退出时自动关闭服务器）
        } catch (IOException e) {
            // 处理启动失败的情况
            System.err.println("服务器启动失败！");
            System.err.println("可能的原因：");
            System.err.println("1. 端口 " + port + " 已被其他程序占用");
            System.err.println("2. 没有足够的系统权限");
            System.err.println("3. 网络配置问题");
            System.err.println("错误详情: " + e.getMessage());
            e.printStackTrace(); // 打印异常堆栈
            tryAlternativePorts();// 尝试使用其他端口
        } catch (Exception e) {
            System.err.println("程序发生未预期的错误！");// 处理其他所有异常
            e.printStackTrace();
            System.exit(1); // 异常退出
        }
    }
    private static void printUsageInfo(int port) {
        System.out.println("\n使用说明：");
        System.out.println("----------------------------------------");
        System.out.println("前端访问地址:");
        System.out.println("  主页面: http://localhost:" + port + "/index.html");
        System.out.println("  注册页: http://localhost:" + port + "/register.html");
        System.out.println("\nAPI接口地址:");
        System.out.println("  用户注册: POST http://localhost:" + port + "/api/user/register");
        System.out.println("  用户登录: POST http://localhost:" + port + "/api/user/login");
        System.out.println("  添加账单: POST http://localhost:" + port + "/api/bill/add");
        System.out.println("  账单列表: GET  http://localhost:" + port + "/api/bill/list?userId=1");
        System.out.println("  删除账单: DELETE http://localhost:" + port + "/api/bill/delete/1?userId=1");
        System.out.println("\n默认测试账号:");
        System.out.println("  管理员: admin / admin123");
        System.out.println("\n调试信息:");
        System.out.println("  数据库文件: account_book.db");
        System.out.println("  服务器端口: " + port);
        System.out.println("  按 Ctrl+C 停止服务器");
        System.out.println("----------------------------------------");
    }
    private static void tryAlternativePorts() {
        int[] alternativePorts = {8081, 8082, 8088, 8090, 8888};// 备选端口列表
        System.out.println("\n正在尝试其他端口...");
        for (int altPort : alternativePorts) {
            try {
                System.out.println("尝试端口: " + altPort);
                SimpleHttpServer server = new SimpleHttpServer(altPort);
                server.start();
                System.out.println("服务器在端口 " + altPort + " 启动成功！");
                printUsageInfo(altPort);
                addShutdownHook(server);
                return; // 成功，退出方法
            } catch (IOException e) {
                // 这个端口也被占用，继续尝试下一个
                System.out.println("端口 " + altPort + " 也被占用，继续尝试...");
            }
        }
        // 所有端口都失败
        System.err.println("所有尝试的端口都被占用，请关闭占用端口的程序后重试");
        System.exit(1); // 退出程序
    }
    private static void addShutdownHook(SimpleHttpServer server) {
        // 创建关闭钩子线程
        Thread shutdownHook = new Thread(() -> {
            System.out.println("\n\n正在关闭服务器...");
            server.stop(); // 停止服务器
            System.out.println("服务器已关闭，再见！");
        });
        // 注册关闭钩子到JVM
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        System.out.println("关闭钩子已注册，按 Ctrl+C 可安全关闭服务器");
    }
    private static void checkEnvironment() {
        System.out.println("检查系统环境...");
        String javaVersion = System.getProperty("java.version");// 检查Java版本
        System.out.println("  Java版本: " + javaVersion);
        if (javaVersion.startsWith("1.8") || javaVersion.startsWith("8")) {// 建议使用Java 17或更高版本
            System.out.println("检测到Java 8，建议升级到Java 17以获得更好性能");
        }
        String osName = System.getProperty("os.name"); // 检查操作系统
        System.out.println("  操作系统: " + osName);
        String userDir = System.getProperty("user.dir"); // 检查用户工作目录
        System.out.println("  工作目录: " + userDir);
        System.out.println("环境检查完成");
    }
}