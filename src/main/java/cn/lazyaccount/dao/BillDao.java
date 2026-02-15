//账单模块的数据访问层类，基于BaseDao封装的通用数据库操作，实现账单的增删改查，按用户ID查询账单列表，统计用户总收入，并通过关联分类表补充账单的分类名称和类型信息
package cn.lazyaccount.dao;// 定义当前类所属包（数据访问层，处理账单的数据库操作）

import cn.lazyaccount.model.Bill;// 导入账单模型类，封装账单数据
import java.sql.ResultSet;// 导入结果集接口，用于映射查询结果
import java.sql.SQLException;// 导入SQL异常类，处理数据库操作异常
import java.util.List;// 导入List接口，存储账单列表

public class BillDao {//BillDao,专门处理账单表（t_bill）的数据库操作
    public boolean addBill(Bill bill) {//添加账单方法：将Bill对象插入t_bill表，返回是否添加成功
        String sql = "INSERT INTO t_bill (user_id, category_id, amount, remark, bill_time) VALUES (?, ?, ?, ?, ?)";// 定义插入SQL：字段为user_id/category_id/amount/remark/bill_time，值用?占位（防止SQL注入）
        int result = BaseDao.executeUpdate(sql,// 调用BaseDao的executeUpdate执行插入操作，传入SQL和对应参数
                bill.getUserId(),      //第1个？，账单所属用户ID
                bill.getCategoryId(),  //第2个？，账单分类ID
                bill.getAmount(),      //第3个？，账单金额
                bill.getRemark(),      //第4个？，账单备注（可为null）
                new java.sql.Timestamp(bill.getBillTime().getTime()) // 第5个?，账单时间（转换为java.sql.Timestamp，适配数据库的TIMESTAMP类型）
        );
        return result > 0; //结果>0表示插入成功（受影响行数≥1），返回true，否则返回false
    }
    public boolean deleteBill(int billId, int userId) {//删除账单方法，跟据账单ID和用户ID删除账单（仅删除该用户的账单，防止越权）
        String sql = "DELETE FROM t_bill WHERE id = ? AND user_id = ?";//定义SQL删除语句：只删除指定用户ID的账单
        int result = BaseDao.executeUpdate(sql, billId, userId);//调用BaseDao执行删除操作，传入账单ID和用户ID作为参数
        return result > 0; // 返回操作结果
    }
    public boolean updateBill(Bill bill) {//更新账单方法：根据账单ID和用户ID更新账单信息（仅更新该用户的账单）
        String sql = "UPDATE t_bill SET category_id = ?, amount = ?, remark = ?, bill_time = ? WHERE id = ? AND user_id = ?";//定义更新SQL：更新分类ID/金额/备注/账单时间，条件为账单ID=? 且 用户ID=?
        int result = BaseDao.executeUpdate(sql,// 执行更新操作，传入对应参数
                bill.getCategoryId(),  // 新的分类ID，第一个？
                bill.getAmount(),      // 新的金额，第二个？
                bill.getRemark(),      // 新的备注，第三个？
                new java.sql.Timestamp(bill.getBillTime().getTime()), //新的记账时间，第四个？
                bill.getId(),          // 要更新的账单ID，第五个？
                bill.getUserId()       // 用户ID（权限验证），第六个？
        );
        return result > 0; // 返回操作结果，>0表示更新成功，返回true；否则返回false
    }
    public List<Bill> findBillsByUserId(int userId) {//根据用户ID查询账单列表：关联分类表，返回该用户所有账单（含分类名称/类型）
        String sql = "SELECT b.*, c.name as category_name, c.type as category_type " +// SQL查询语句：关联账单分类表，获取分类名称和类型
                "FROM t_bill b " +
                "LEFT JOIN t_bill_category c ON b.category_id = c.id " +
                "WHERE b.user_id = ? " +
                "ORDER BY b.bill_time DESC"; // 按记账时间降序排列
        return BaseDao.queryForList(sql, new BaseDao.RowMapper<Bill>() {//调用BaseDao的queryForList执行查询，传入SQL、RowMapper（结果映射器）、用户ID参数
            @Override
            public Bill mapRow(ResultSet rs) throws SQLException {
                Bill bill = new Bill();// 创建Bill对象并设置所有属性
                bill.setId(rs.getInt("id"));//账单ID
                bill.setUserId(rs.getInt("user_id"));//用户ID
                bill.setCategoryId(rs.getInt("category_id"));//分类ID
                bill.setAmount(rs.getDouble("amount"));//金额（浮点型）
                bill.setRemark(rs.getString("remark"));//备注
                bill.setBillTime(rs.getTimestamp("bill_time"));//账单时间
                bill.setCreateTime(rs.getTimestamp("create_time"));//账单创建时间
                bill.setCategoryName(rs.getString("category_name"));//分类名称
                bill.setCategoryType(rs.getInt("category_type"));//分类类型（关联字段表）
                return bill; // 返回映射后的Bill账单对象
            }
        }, userId); //传入用户ID，userId是SQL参数
    }
    public Bill findById(int billId, int userId) {//根据账单ID和用户ID查询单个账单：关联分类表，返回该用户的指定账单
        String sql = "SELECT b.*, c.name as category_name, c.type as category_type " +// SQL查询语句
                "FROM t_bill b " +
                "LEFT JOIN t_bill_category c ON b.category_id = c.id " +
                "WHERE b.id = ? AND b.user_id = ?";
        return BaseDao.queryForObject(sql, new BaseDao.RowMapper<Bill>() {//调用BaseDao.queryForObject执行查询，返回单个Bill对象
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
        }, billId, userId); // 传入账单ID和用户ID，billId和userId是SQL参数
    }
    public double getTotalIncome(int userId) {//查询用户总收入：统计该用户所有类型为1（收入）的账单金额总和
        String sql = "SELECT SUM(b.amount) FROM t_bill b " +// SQL查询：关联分类表，只统计类型为1（收入）的账单
                "LEFT JOIN t_bill_category c ON b.category_id = c.id " +
                "WHERE b.user_id = ? AND c.type = 1";
        Double total = BaseDao.queryForObject(sql, new BaseDao.RowMapper<Double>() {//调用BaseDao查询单个值（Double型）
            @Override
            public Double mapRow(ResultSet rs) throws SQLException {
                return rs.getDouble(1); // 获取SUM结果
            }
        }, userId);
        return total != null ? total : 0.0; // 如果为null返回0.0
    }
    public double getTotalExpense(int userId) {//查询用户总支出：统计该用户所有类型为2（支出）的账单金额总和
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