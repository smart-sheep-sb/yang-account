package cn.lazyaccount.dao;

import cn.lazyaccount.model.Bill;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BillDao {
    public boolean addBill(Bill bill) {
        String sql = "INSERT INTO t_bill (user_id, category_id, amount, remark, bill_time) VALUES (?, ?, ?, ?, ?)";// SQL插入语句
        int result = BaseDao.executeUpdate(sql,// 执行插入操作
                bill.getUserId(),      // 用户ID
                bill.getCategoryId(),  // 分类ID
                bill.getAmount(),      // 金额
                bill.getRemark(),      // 备注
                new java.sql.Timestamp(bill.getBillTime().getTime()) // 记账时间
        );
        return result > 0; // 返回操作结果
    }
    public boolean deleteBill(int billId, int userId) {
        String sql = "DELETE FROM t_bill WHERE id = ? AND user_id = ?";// SQL删除语句：只删除指定用户ID的账单
        int result = BaseDao.executeUpdate(sql, billId, userId);// 执行删除操作
        return result > 0; // 返回操作结果
    }
    public boolean updateBill(Bill bill) {
        String sql = "UPDATE t_bill SET category_id = ?, amount = ?, remark = ?, bill_time = ? WHERE id = ? AND user_id = ?";// SQL更新语句
        int result = BaseDao.executeUpdate(sql,// 执行更新操作
                bill.getCategoryId(),  // 分类ID
                bill.getAmount(),      // 金额
                bill.getRemark(),      // 备注
                new java.sql.Timestamp(bill.getBillTime().getTime()), // 记账时间
                bill.getId(),          // 账单ID
                bill.getUserId()       // 用户ID（权限验证）
        );
        return result > 0; // 返回操作结果
    }
    public List<Bill> findBillsByUserId(int userId) {
        String sql = "SELECT b.*, c.name as category_name, c.type as category_type " +// SQL查询语句：关联账单分类表，获取分类名称和类型
                "FROM t_bill b " +
                "LEFT JOIN t_bill_category c ON b.category_id = c.id " +
                "WHERE b.user_id = ? " +
                "ORDER BY b.bill_time DESC"; // 按记账时间降序排列
        return BaseDao.queryForList(sql, new BaseDao.RowMapper<Bill>() {// 执行查询并返回列表
            @Override
            public Bill mapRow(ResultSet rs) throws SQLException {
                Bill bill = new Bill();// 创建Bill对象并设置所有属性
                bill.setId(rs.getInt("id"));
                bill.setUserId(rs.getInt("user_id"));
                bill.setCategoryId(rs.getInt("category_id"));
                bill.setAmount(rs.getDouble("amount"));
                bill.setRemark(rs.getString("remark"));
                bill.setBillTime(rs.getTimestamp("bill_time"));
                bill.setCreateTime(rs.getTimestamp("create_time"));
                bill.setCategoryName(rs.getString("category_name"));
                bill.setCategoryType(rs.getInt("category_type"));
                return bill; // 返回账单对象
            }
        }, userId); // userId是SQL参数
    }
    public Bill findById(int billId, int userId) {
        String sql = "SELECT b.*, c.name as category_name, c.type as category_type " +// SQL查询语句
                "FROM t_bill b " +
                "LEFT JOIN t_bill_category c ON b.category_id = c.id " +
                "WHERE b.id = ? AND b.user_id = ?";
        return BaseDao.queryForObject(sql, new BaseDao.RowMapper<Bill>() {// 执行查询
            @Override
            public Bill mapRow(ResultSet rs) throws SQLException {
                Bill bill = new Bill();// 创建Bill对象并设置属性
                bill.setId(rs.getInt("id"));
                bill.setUserId(rs.getInt("user_id"));
                bill.setCategoryId(rs.getInt("category_id"));
                bill.setAmount(rs.getDouble("amount"));
                bill.setRemark(rs.getString("remark"));
                bill.setBillTime(rs.getTimestamp("bill_time"));
                bill.setCreateTime(rs.getTimestamp("create_time"));
                bill.setCategoryName(rs.getString("category_name"));
                bill.setCategoryType(rs.getInt("category_type"));
                return bill; // 返回账单对象
            }
        }, billId, userId); // billId和userId是SQL参数
    }
    public double getTotalIncome(int userId) {
        String sql = "SELECT SUM(b.amount) FROM t_bill b " +// SQL查询：关联分类表，只统计类型为1（收入）的账单
                "LEFT JOIN t_bill_category c ON b.category_id = c.id " +
                "WHERE b.user_id = ? AND c.type = 1";
        Double total = BaseDao.queryForObject(sql, new BaseDao.RowMapper<Double>() {//执行查询
            @Override
            public Double mapRow(ResultSet rs) throws SQLException {
                return rs.getDouble(1); // 获取SUM结果
            }
        }, userId);
        return total != null ? total : 0.0; // 如果为null返回0.0
    }
    public double getTotalExpense(int userId) {
        String sql = "SELECT SUM(b.amount) FROM t_bill b " +// SQL查询：关联分类表，只统计类型为2（支出）的账单
                "LEFT JOIN t_bill_category c ON b.category_id = c.id " +
                "WHERE b.user_id = ? AND c.type = 2";
        Double total = BaseDao.queryForObject(sql, new BaseDao.RowMapper<Double>() {//执行查询
            @Override
            public Double mapRow(ResultSet rs) throws SQLException {
                return rs.getDouble(1); // 获取SUM结果
            }
        }, userId);
        return total != null ? total : 0.0; // 如果为null返回0.0
    }
}