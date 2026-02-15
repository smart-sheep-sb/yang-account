//数据库访问层，封装了数据库增删改查，事务管理，资源释放操作，通过接口实现结果集与Java对象的灵活映射，统一处理异常
package cn.lazyaccount.dao;//定义当前类所属包

import cn.lazyaccount.config.DatabaseConfig; // 数据库配置类，获取数据库连接
import java.sql.Connection; // 数据库连接接口，封装数据库连接
import java.sql.PreparedStatement; // 预编译SQL语句接口，防止SQL注入，提高执行效率
import java.sql.ResultSet; // 查询结果集接口，存储SQL查询返回的数据
import java.sql.SQLException; // SQL异常类，处理数据库操作的异常
import java.sql.Statement; // SQL语句接口，用于指定返回生成的主键
import java.util.ArrayList; // 动态数组列表，动态数组，存储查询结果列表
import java.util.List; // 列表接口，定义列表规范

public class BaseDao {//基础数据访问类，封装通用的数据库操作，提供其他DAO类复用
    public static int executeUpdate(String sql, Object... params) {//通过更新方法，执行增删查改语句，返回受影响行数
        Connection conn = null; // 数据库连接变量
        PreparedStatement pstmt = null; // 预编译语句变量
        try {//获取数据库连接
            conn = DatabaseConfig.getConnection(); // 从配置类DatabaseConfig获取数据库连接
            pstmt = conn.prepareStatement(sql);// 创建预编译语句，PreparedStatement可以防止SQL注入攻击
            for (int i = 0; i < params.length; i++) {// 遍历所有参数数组，设置到PreparedStatement中
                pstmt.setObject(i + 1, params[i]);// i+1是因为JDBC参数索引从1开始
            }
            int affectedRows = pstmt.executeUpdate();//执行更新操作
            return affectedRows;//返回受影响的行数
        } catch (SQLException e) {//捕获SQL异常，打印详细错误信息
            System.err.println("执行SQL更新失败");// 处理SQL异常
            System.err.println("SQL: " + sql);//打印执行的SQL语句
            System.err.println("参数: " + java.util.Arrays.toString(params));//打印参数
            System.err.println("错误: " + e.getMessage());//打印错误信息
            e.printStackTrace(); // 打印异常堆栈
            return -1; // 返回-1表示执行失败
        } finally {//释放资源，先关闭PreparedStatement，再关闭Connection
            closeResource(pstmt); // 关闭语句
            closeResource(conn); // 关闭连接
        }
    }
    public static <T> T queryForObject(String sql, RowMapper<T> mapper, Object... params) {//通用查询单个对象方法，执行select语句，返回单个Java对象。泛型T：返回对象的类型；RowMapper：结果集映射器，将ResultSet行转为Java对象
        Connection conn = null;// 声明资源变量连接
        PreparedStatement pstmt = null;//预编译语句
        ResultSet rs = null; // 结果集
        try {
            conn = DatabaseConfig.getConnection();//获取连接语句
            pstmt = conn.prepareStatement(sql);//创建预编译语句
            for (int i = 0; i < params.length; i++) {//设置参数，未SQL占位符赋值
                pstmt.setObject(i + 1, params[i]);
            }
            rs = pstmt.executeQuery();//执行查询，获取结果集
            if (rs.next()) { // 如果有数据,处理结果集
                return mapper.mapRow(rs);// 使用映射器将结果集行转换为Java对象
            } else {
                return null;// 没有查询到数据
            }
        } catch (SQLException e) {//捕获SQL异常
            System.err.println("查询单个对象失败");// 处理异常
            System.err.println("SQL: " + sql);//打印SQL语句
            System.err.println("错误: " + e.getMessage());//打印错误信息
            e.printStackTrace();//打印异常堆栈
            return null;//异常返回null
        } finally {// 注意关闭顺序：ResultSet → PreparedStatement → Connection
            closeResource(rs);//释放资源
            closeResource(pstmt);
            closeResource(conn);
        }
    }
    public static <T> List<T> queryForList(String sql, RowMapper<T> mapper, Object... params) {//通用查询列表方法：执行SELECT语句，返回多个Java对象的列表
        List<T> list = new ArrayList<>();// 创建空列表，用于存储查询结果（即使无数据也返回空列表）
        Connection conn = null;// 声明资源变量
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();//获取连接语句
            pstmt = conn.prepareStatement(sql);//创建预编译语句
            for (int i = 0; i < params.length; i++) {//设置参数，为SQL占位符赋值
                pstmt.setObject(i + 1, params[i]);
            }
            rs = pstmt.executeQuery();//执行查询，获取结果集
            while (rs.next()) {//遍历结果集（rs.next()移动到下一行，直到无数据）
                T obj = mapper.mapRow(rs); // 将每一行转换为Java对象并添加到列表
                list.add(obj);//添加列表
            }
            return list; // 返回结果列表（空列表/有数据的列表）
        } catch (SQLException e) {//捕获异常
            System.err.println("查询列表失败");// 处理异常
            System.err.println("SQL: " + sql);//打印SQL语句
            System.err.println("错误: " + e.getMessage());//打印错误信息
            e.printStackTrace();//打印异常堆栈
            return list; // 异常仍返回空列表（避免空指针）
        } finally {
            closeResource(rs);//释放资源
            closeResource(pstmt);
            closeResource(conn);
        }
    }
    public static int executeInsertAndReturnKey(String sql, Object... params) {//通用插入并返回主键方法：执行INSERT语句，返回自动生成的主键（如自增ID）
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;//存储生成的主键
        try {
            conn = DatabaseConfig.getConnection(); // 获取连接
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); // 创建预编译语句，指定需要返回生成的主键
            for (int i = 0; i < params.length; i++) { // 设置参数
                pstmt.setObject(i + 1, params[i]);//为SQL占位符赋值
            }
            int affectedRows = pstmt.executeUpdate();// 执行插入操作，返回受影响行数
            if (affectedRows > 0) {//插入成功，获取生成的主键
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1); // 返回第一个主键列的值
                }
            }
            return -1; // 插入失败或无主键返回-1
        } catch (SQLException e) {//捕获异常
            System.err.println("插入数据并获取主键失败");//打印失败内容
            System.err.println("SQL: " + sql);//打印SQL语句
            System.err.println("错误: " + e.getMessage());//打印错误信息
            e.printStackTrace();//打印错误堆栈
            return -1;//异常返回-1
        } finally {//释放资源
            closeResource(rs);
            closeResource(pstmt);
            closeResource(conn);
        }
    }
    public interface RowMapper<T> {//内部函数式接口，行映射器，用于将ResultSet的一行数据转为指定类的Java对象
        T mapRow(ResultSet rs) throws SQLException;//泛型T：目标对象类型；mapRow方法：核心映射逻辑，由调用方实现
    }
    private static void closeResource(Connection conn) {//私有工具方法，关闭数据库连接
        if (conn != null) {//非空才关闭，避免空指针
            try {
                conn.close(); // 关闭连接
            } catch (SQLException e) {//捕获关闭异常
                System.err.println("关闭数据库连接时出错: " + e.getMessage());//打印错误信息
            }
        }
    }
    private static void closeResource(Statement stmt) {//私有工具方法，关闭Statement/PrepaerdStatement
        if (stmt != null) {
            try {
                stmt.close(); // 关闭语句
            } catch (SQLException e) {//捕捉异常
                System.err.println("关闭Statement时出错: " + e.getMessage());
            }
        }
    }
    private static void closeResource(ResultSet rs) {//私有工具方法，关闭ResultSet
        if (rs != null) {
            try {
                rs.close(); // 关闭结果集
            } catch (SQLException e) {
                System.err.println("关闭ResultSet时出错: " + e.getMessage());
            }
        }
    }
    public static void beginTransaction(Connection conn) throws SQLException {//事务管理，开启事务（关闭自动提交）
        conn.setAutoCommit(false); // JDBC默认自动提交，关闭后需手段commit/rollback
        System.out.println("事务已开启");//输出内容
    }
    public static void commitTransaction(Connection conn) throws SQLException {//事务管理，提交事务
        conn.commit(); // 提交事务
        conn.setAutoCommit(true); // 恢复自动提交
        System.out.println("事务已提交");//输出内容
    }
    public static void rollbackTransaction(Connection conn) {//事务管理，回滚事务（执行失败后调用）
        try {
            if (conn != null) {
                conn.rollback(); // 回滚事务
                conn.setAutoCommit(true); // 恢复自动提交
                System.out.println("事务已回滚");
            }
        } catch (SQLException e) {
            System.err.println("回滚事务时出错: " + e.getMessage());
        }
    }
}