package cn.lazyaccount.config;

import java.sql.Connection; // 导入Connection接口,数据库连接接口
import java.sql.DriverManager; // 导入DriverManager类，数据库驱动管理器
import java.sql.SQLException; // 导入SQLException异常类，SQL异常类
import java.sql.Statement; // 导入Statement接口，SQL语句执行接口
import java.nio.file.Files; // 导入Files类，文件操作类
import java.nio.file.Paths; // 导入Paths类，路径操作类
import java.io.InputStream; // 导入InputStream类，输入流类
import java.io.InputStreamReader;//输入流读取器
import java.io.BufferedReader;//缓冲读取器
public class DatabaseConfig {//数据库URL：使用相对路径，数据库文件名为account_book.db
    private static final String DB_URL = "jdbc:sqlite:account_book.db";//jdbc:sqlite:是SQLite的JDBC连接协议
    private static boolean initialized = false;//数据库初始化标志：避免重复初始化
    static {
        try {//注册sqlite JDBC驱动
            Class.forName("org.sqlite.JDBC");//通过Class.forName加载驱动类
            System.out.println("数据库驱动加载成功，数据库初始化完成");
            initializeDatabase();//初始化数据库（创建表和插入默认数据）
        }catch (ClassNotFoundException e){
            System.err.println("错误：找不到SQLite JDBC驱动类！");// 驱动类未找到异常处理
            System.err.println("1.请检查pom.xml中是否添加了sqlite-jdbc依赖");
            System.err.println("2.Maven依赖没有正确下载");
            System.err.println("3.项目没有正确刷新Maven依赖");
            e.printStackTrace(); // 打印异常堆栈
            System.exit(1); // 退出程序，错误代码1
        } catch (Exception e) {
            System.err.println("错误：数据库初始化失败！");// 其他异常处理
            e.printStackTrace(); // 打印异常堆栈
            System.exit(1); // 退出程序，错误代码1
        }
    }
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) { // 启用外键约束（SQLite默认关闭）
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn; // 返回连接对象
    }
    private static void initializeDatabase() {
        if (initialized) { // 检查是否已经初始化过
            System.out.println("数据库已经初始化过，跳过初始化步骤");
            return; // 直接返回，不再执行初始化
        }
        Connection conn = null; // 声明连接变量
        Statement stmt = null; // 声明语句变量
        BufferedReader reader = null; // 声明读取器变量
        try {
            System.out.println("开始初始化数据库...");
            conn = DriverManager.getConnection(DB_URL); // 调用上面的getConnection方法，获取数据库连接
            System.out.println("数据库连接建立成功");
            stmt = conn.createStatement();//创建表
            String createUserTable = "CREATE TABLE IF NOT EXISTS t_user (" +//创建用户表
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "nickname TEXT," +
                    "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createUserTable);
            System.out.println("用户表创建： t_user");
            String createCategoryTable = "CREATE TABLE IF NOT EXISTS t_bill_category (" +//创建账单分类表
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL UNIQUE," +
                    "type INTEGER NOT NULL)";
            stmt.execute(createCategoryTable);
            System.out.println("创建分类表: t_bill_category");
            String createBillTable = "CREATE TABLE IF NOT EXISTS t_bill (" +//创建账单表
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "category_id INTEGER NOT NULL," +
                    "amount REAL NOT NULL," +
                    "remark TEXT," +
                    "bill_time TIMESTAMP NOT NULL," +
                    "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (category_id) REFERENCES t_bill_category(id))";
            stmt.execute(createBillTable);
            System.out.println("创建账单表: t_bill");
            String inseryCategories = "INSERT OR IGNORE INTO t_bill_category (name , type) VALUES " +//插入默认分类数据
                    "('餐饮', 2), ('交通', 2), ('购物', 2), ('娱乐', 2), ('住房', 2), " +
                    "('工资', 1), ('奖金', 1), ('兼职', 1), ('投资', 1), ('其他', 2)";
            stmt.execute(inseryCategories);
            System.out.println("插入默认分类数据");
            String insertAdmin = "INSERT OR IGNORE INTO t_user (username , password , nickname) VALUES" + "('admin' , 'admin123' , '系统管理员'";//插入默认管理员用户
            stmt.execute("PRAGMA foreign_keys = ON;");//启动外键约束
            System.out.println("启动外键约束");
            InputStream is = DatabaseConfig.class.getClassLoader() // 从classpath（src/main/resources）读取init_database.sql文件，读取初始化SQL脚本
                    .getResourceAsStream("init_database.sql");
            if (is == null) {
                System.err.println("错误：找不到数据库初始化脚本 init_database.sql");// 如果找不到SQL文件
                System.err.println("请确认文件是否在 src/main/resources/ 目录下");
                throw new RuntimeException("数据库初始化脚本不存在");
            }
            stmt = conn.createStatement();//创建SQL语句执行器
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));//读取并执行SQL脚本
            StringBuilder sqlBuilder = new StringBuilder(); // 用于拼接SQL语句
            String line; // 存储每一行内容
            System.out.println("读取SQL初始化脚本...");
            while ((line = reader.readLine()) != null) {// 跳过注释行和空行
                line = line.trim(); // 去除首尾空格
                if (line.startsWith("--") || line.isEmpty()) {
                    continue; // 跳过注释和空行
                }
                sqlBuilder.append(line);// 拼接SQL语句
                if (line.endsWith(";")) {// 如果行以分号结束，表示一个完整的SQL语句
                    String sql = sqlBuilder.toString(); // 获取完整SQL
                    try {
                        stmt.execute(sql);// 执行SQL语句
                        System.out.println("  执行SQL: " +
                                (sql.length() > 50 ? sql.substring(0, 50) + "..." : sql));
                    } catch (SQLException e) {
                        System.out.println("  SQL执行跳过: " + e.getMessage());// 处理SQL执行错误（如表已存在）
                    }
                    sqlBuilder.setLength(0);// 清空StringBuilder，准备下一个SQL语句
                } else {
                    sqlBuilder.append(" ");// 如果没结束，加个空格（避免关键词连在一起）
                }
            }
            initialized = true; // 设置标志为true，标记初始化完成
            System.out.println("数据库初始化完成！");
            System.out.println("数据库文件位置: " +
                    Paths.get("").toAbsolutePath().toString() + "\\account_book.db");
        } catch (Exception e) {
            System.err.println("数据库初始化失败！");// 处理初始化过程中的异常
            e.printStackTrace(); // 打印异常堆栈
            throw new RuntimeException("数据库初始化失败", e); // 抛出运行时异常
        } finally {
            try { // 按创建的反顺序关闭资源，释放资源（重要！避免内存泄漏）
                if (reader != null) reader.close(); // 关闭读取器
            } catch (Exception e) {
                System.err.println("关闭读取器时出错: " + e.getMessage());
            }
            try {
                if (stmt != null) stmt.close(); // 关闭语句
            } catch (Exception e) {
                System.err.println("关闭语句时出错: " + e.getMessage());
            }
            try {
                if (conn != null) conn.close(); // 关闭连接
            } catch (Exception e) {
                System.err.println("关闭连接时出错: " + e.getMessage());
            }
        }
    }
    public static boolean isInitialized() {
        return initialized; // 返回初始化状态
    }
    public static void resetInitialization() {
        initialized = false; // 重置标志
        System.out.println("数据库初始化状态已重置");
    }
}
