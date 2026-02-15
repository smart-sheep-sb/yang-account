//数据库配置和初始化类
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
    static {//加载数据库驱动和初始化数据库
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
    public static Connection getConnection() throws SQLException {//创建数据库的连接
        Connection conn = DriverManager.getConnection(DB_URL);// 通过驱动管理器获取数据库连接
        try (Statement stmt = conn.createStatement()) { // 创建接口的stmt对象去把SQL语句提交到数据库
            stmt.execute("PRAGMA foreign_keys = ON;");//启用外键约束（SQLite默认关闭）
        }
        return conn; // 返回配置好的数据库的连接对象
    }
    private static void initializeDatabase() {//私有的初始化方法，创建表结构，插入默认数据，外部无法直接调用
        if (initialized) { // 检查是否已经初始化过，避免重复
            System.out.println("数据库已经初始化过，跳过初始化步骤");//输出内容
            return; // 直接返回，不再执行初始化
        }
        Connection conn = null; // 声明数据库连接变量，初始化为null
        Statement stmt = null; // 声明数据库执行语句变量，初始化为null
        BufferedReader reader = null; // 声明缓冲读取器变量，初始化为null
        try {
            System.out.println("开始初始化数据库...");//输出内容
            conn = DriverManager.getConnection(DB_URL); // 调用上面的getConnection方法，获取数据库连接
            System.out.println("数据库连接建立成功");//输出内容
            stmt = conn.createStatement();//创建Statement对象，用来执行SQL语句
            String createUserTable = "CREATE TABLE IF NOT EXISTS t_user (" +//创建用户表t_user，要做不存在的时候才能创建
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +//主键
                    "username TEXT UNIQUE NOT NULL," +//用户名
                    "password TEXT NOT NULL," +//密码，非空
                    "nickname TEXT," +//昵称，可选
                    "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";//创建时间，默认当前时间
            stmt.execute(createUserTable);//执行创建用户表的SQL
            System.out.println("用户表创建： t_user");//输出内容
            String createCategoryTable = "CREATE TABLE IF NOT EXISTS t_bill_category (" +//创建账单分类表t_bill_category
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +//主键
                    "name TEXT NOT NULL UNIQUE," +//分类名称，唯一并且为空
                    "type INTEGER NOT NULL)";//类型，1代表收入，2代表支出
            stmt.execute(createCategoryTable);//执行创建分类表的SQL
            System.out.println("创建分类表: t_bill_category");//输出内容
            String createBillTable = "CREATE TABLE IF NOT EXISTS t_bill (" +//创建账单表t_bill
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +//主键
                    "user_id INTEGER NOT NULL," +//关联的用户ID，非空
                    "category_id INTEGER NOT NULL," +//关联的分类ID，非空
                    "amount REAL NOT NULL," +//金额，非空（浮点型）
                    "remark TEXT," +//备注
                    "bill_time TIMESTAMP NOT NULL," +//账单的发生时间
                    "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +//创建时间
                    "FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE," +// 外键约束：user_id关联t_user的id，用户删除时级联删除账单
                    "FOREIGN KEY (category_id) REFERENCES t_bill_category(id))";//外键约束：category_id关联t_bill_category的id
            stmt.execute(createBillTable);//执行创建账单表的SQl
            System.out.println("创建账单表: t_bill");//输出内容
            String inseryCategories = "INSERT OR IGNORE INTO t_bill_category (name , type) VALUES " +//插入默认分类数据，INSERT OR IGNORE表示重复时忽略
                    "('餐饮', 2), ('交通', 2), ('购物', 2), ('娱乐', 2), ('住房', 2), " +//支出类的（type = 2）
                    "('工资', 1), ('奖金', 1), ('兼职', 1), ('投资', 1), ('其他', 2)";//收入类的（type = 1）+其他支出
            stmt.execute(inseryCategories);// 执行插入分类数据的SQL
            System.out.println("插入默认分类数据");//输出内容
            String insertAdmin = "INSERT OR IGNORE INTO t_user (username , password , nickname) VALUES" + "('admin' , 'admin123' , '系统管理员')";//插入默认管理员用户insertAdmin
            stmt.execute("PRAGMA foreign_keys = ON;");//再次启动外键约束
            System.out.println("启动外键约束");//输出内容
            InputStream is = DatabaseConfig.class.getClassLoader() // 从classpath（src/main/resources）读取init_database.sql文件，读取初始化SQL脚本
                    .getResourceAsStream("init_database.sql");
            if (is == null) {//检查脚本文件是否存在
                System.err.println("错误：找不到数据库初始化脚本 init_database.sql");//提示文件不存在
                System.err.println("请确认文件是否在 src/main/resources/ 目录下");
                throw new RuntimeException("数据库初始化脚本不存在");//抛出运行时异常终止初始化
            }
            stmt = conn.createStatement();//重新创建Statement对象
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));//创建缓冲读取器，指定UTF-8编码读取SQL脚本，避免中文乱码
            StringBuilder sqlBuilder = new StringBuilder(); // 用于拼接多行SQL语句
            String line; // 存储读取每一行内容
            System.out.println("读取SQL初始化脚本...");//输出内容
            while ((line = reader.readLine()) != null) {// 跳过注释行和空行
                line = line.trim(); // 去除首尾空格，换行符
                if (line.startsWith("--") || line.isEmpty()) {//跳过注释行（--开头）和空行
                    continue;
                }
                sqlBuilder.append(line);// 有效行拼接SQL语句中
                if (line.endsWith(";")) {// 如果行以分号结束，表示一个完整的SQL语句
                    String sql = sqlBuilder.toString(); // 获取拼接好的完整SQL
                    try {
                        stmt.execute(sql);// 执行SQL语句
                        System.out.println("  执行SQL: " +//输出内容
                                (sql.length() > 50 ? sql.substring(0, 50) + "..." : sql));
                    } catch (SQLException e) {//捕获SQL执行的异常
                        System.out.println("  SQL执行跳过: " + e.getMessage());
                    }
                    sqlBuilder.setLength(0);// 清空StringBuilder，准备下一个SQL语句
                } else {
                    sqlBuilder.append(" ");// 如果没结束，加个空格（避免关键词连在一起）
                }
            }
            initialized = true; // 设置标志为true，标记初始化完成
            System.out.println("数据库初始化完成！");//输出内容
            System.out.println("数据库文件位置: " +
                    Paths.get("").toAbsolutePath().toString() + "\\account_book.db");//打印数据库文件的绝对路径，方便查找
        } catch (Exception e) {//捕获初始化过程中的所有异常
            System.err.println("数据库初始化失败！");//输出内容显示的失败
            e.printStackTrace(); // 打印异常堆栈
            throw new RuntimeException("数据库初始化失败", e); // 抛出运行时异常，终止程序
        } finally {//最终块，无论是否异常，都要关闭资源（避免内存泄露）
            try { // 按创建的反顺序关闭资源，释放资源（避免内存泄漏）
                if (reader != null) reader.close(); // 关闭缓冲读取器
            } catch (Exception e) {//捕捉异常
                System.err.println("关闭读取器时出错: " + e.getMessage());//输出内容
            }
            try {
                if (stmt != null) stmt.close(); // 关闭Statement对象
            } catch (Exception e) {//捕捉异常
                System.err.println("关闭语句时出错: " + e.getMessage());//输出内容
            }
            try {
                if (conn != null) conn.close(); // 关闭数据库连接
            } catch (Exception e) {//捕捉异常
                System.err.println("关闭连接时出错: " + e.getMessage());//输出内容
            }
        }
    }
    public static boolean isInitialized() {//获取数据库初始化状态的方法
        return initialized; // 返回初始化状态
    }
    public static void resetInitialization() {//重置初始化状态的公共方法
        initialized = false; // 重置标志
        System.out.println("数据库初始化状态已重置");//输出内容
    }
}
