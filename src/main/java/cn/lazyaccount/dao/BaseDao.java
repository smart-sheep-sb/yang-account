package cn.lazyaccount.dao;

import cn.lazyaccount.config.DatabaseConfig; // 数据库配置类
import java.sql.Connection; // 数据库连接接口
import java.sql.PreparedStatement; // 预编译SQL语句接口
import java.sql.ResultSet; // 查询结果集接口
import java.sql.SQLException; // SQL异常类
import java.sql.Statement; // SQL语句接口
import java.util.ArrayList; // 动态数组列表
import java.util.List; // 列表接口

public class BaseDao {
    public static int executeUpdate(String sql, Object... params) {
        // 声明连接和语句变量（在try外面声明，以便在finally中关闭）
        Connection conn = null; // 数据库连接
        PreparedStatement pstmt = null; // 预编译语句
        try {
            // 获取数据库连接
            conn = DatabaseConfig.getConnection(); // 从配置类获取连接
            // 创建预编译语句
            pstmt = conn.prepareStatement(sql);// PreparedStatement可以防止SQL注入攻击
            // 设置SQL参数
            for (int i = 0; i < params.length; i++) {// 遍历所有参数，设置到PreparedStatement中
                pstmt.setObject(i + 1, params[i]);// i+1是因为JDBC参数索引从1开始
            }
            int affectedRows = pstmt.executeUpdate();//执行更新操作
            return affectedRows;//返回受影响的行数
        } catch (SQLException e) {
            System.err.println("执行SQL更新失败");// 处理SQL异常
            System.err.println("SQL: " + sql);
            System.err.println("参数: " + java.util.Arrays.toString(params));
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace(); // 打印异常堆栈
            return -1; // 返回-1表示失败
        } finally {
            // 释放资源
            // 先关闭PreparedStatement，再关闭Connection
            closeResource(pstmt); // 关闭语句
            closeResource(conn); // 关闭连接
        }
    }
    public static <T> T queryForObject(String sql, RowMapper<T> mapper, Object... params) {
        Connection conn = null;// 声明资源变量
        PreparedStatement pstmt = null;
        ResultSet rs = null; // 结果集

        try {
            conn = DatabaseConfig.getConnection();//获取连接和创建语句
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {//设置参数
                pstmt.setObject(i + 1, params[i]);
            }
            rs = pstmt.executeQuery();//执行查询，获取结果集
            if (rs.next()) { // 如果有数据,处理结果集
                return mapper.mapRow(rs);// 使用映射器将结果集行转换为Java对象
            } else {
                return null;// 没有查询到数据
            }
        } catch (SQLException e) {
            System.err.println("查询单个对象失败");// 处理异常
            System.err.println("SQL: " + sql);
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            // 注意关闭顺序：ResultSet → PreparedStatement → Connection
            closeResource(rs);//释放资源
            closeResource(pstmt);
            closeResource(conn);
        }
    }
    public static <T> List<T> queryForList(String sql, RowMapper<T> mapper, Object... params) {
        List<T> list = new ArrayList<>();// 创建列表用于存储结果
        Connection conn = null;// 声明资源变量
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();//获取连接和创建语句
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {//设置参数
                pstmt.setObject(i + 1, params[i]);
            }
            rs = pstmt.executeQuery();//执行查询
            while (rs.next()) {//遍历结果集
                T obj = mapper.mapRow(rs); // 将每一行转换为对象并添加到列表
                list.add(obj);
            }
            return list; // 返回列表
        } catch (SQLException e) {
            System.err.println("查询列表失败");// 处理异常
            System.err.println("SQL: " + sql);
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
            return list; // 返回空列表
        } finally {
            closeResource(rs);//释放资源
            closeResource(pstmt);
            closeResource(conn);
        }
    }
    public static int executeInsertAndReturnKey(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection(); // 获取连接
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); // 创建语句，指定需要返回生成的主键
            for (int i = 0; i < params.length; i++) { // 设置参数
                pstmt.setObject(i + 1, params[i]);
            }
            int affectedRows = pstmt.executeUpdate();// 执行更新
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();// 获取生成的主键
                if (rs.next()) {
                    return rs.getInt(1); // 返回第一个主键列的值
                }
            }
            return -1; // 插入失败
        } catch (SQLException e) {
            System.err.println("插入数据并获取主键失败");
            System.err.println("SQL: " + sql);
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            closeResource(rs);
            closeResource(pstmt);
            closeResource(conn);
        }
    }
    public interface RowMapper<T> {
        T mapRow(ResultSet rs) throws SQLException;
    }
    private static void closeResource(Connection conn) {
        if (conn != null) {
            try {
                conn.close(); // 关闭连接
            } catch (SQLException e) {
                System.err.println("关闭数据库连接时出错: " + e.getMessage());
            }
        }
    }
    private static void closeResource(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close(); // 关闭语句
            } catch (SQLException e) {
                System.err.println("关闭Statement时出错: " + e.getMessage());
            }
        }
    }
    private static void closeResource(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close(); // 关闭结果集
            } catch (SQLException e) {
                System.err.println("关闭ResultSet时出错: " + e.getMessage());
            }
        }
    }
    public static void beginTransaction(Connection conn) throws SQLException {
        conn.setAutoCommit(false); // 关闭自动提交
        System.out.println("事务已开启");
    }
    public static void commitTransaction(Connection conn) throws SQLException {
        conn.commit(); // 提交事务
        conn.setAutoCommit(true); // 恢复自动提交
        System.out.println("事务已提交");
    }
    public static void rollbackTransaction(Connection conn) {
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