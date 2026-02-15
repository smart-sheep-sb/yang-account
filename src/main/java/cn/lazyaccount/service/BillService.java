//账单业务逻辑层，处理记账相关的业务流程，连接Controller和Dao
package cn.lazyaccount.service;

import cn.lazyaccount.dao.BillDao;//账单数据访问对象，负责数据库操作
import cn.lazyaccount.dao.CategoryDao;//分类数据访问对象，负责分类数据库操作
import cn.lazyaccount.model.Bill;//账单实体类
import cn.lazyaccount.model.Category;//分类实体类
import java.util.List;//集合接口
public class BillService {//账单业务逻辑类
    private final BillDao billDao = new BillDao();// 账单数据访问对象，负责t_bill表的CRUD操作
    private final CategoryDao categoryDao = new CategoryDao();// 分类数据访问对象，负责t_bill_category表的查询操作
    public boolean addBill(Bill bill) {//添加账单
        System.out.println("添加账单: 用户ID=" + bill.getUserId() + ", 金额=" + bill.getAmount());//业务日志，记录添加操作
        boolean success = billDao.addBill(bill);// 调用DAO层执行数据库插入操作
        if (success) {//结束日志
            System.out.println("账单添加成功");//输出内容
        } else {
            System.out.println("账单添加失败");//输出内容
        }
        return success; // 返回操作结果
    }
    public boolean deleteBill(int billId, int userId) {//删除账单
        System.out.println("删除账单: ID=" + billId + ", 用户ID=" + userId);//业务日志，记录删除操作
        boolean success = billDao.deleteBill(billId, userId);// 调用DAO层执行数据库删除账单
        if (success) {//结果日志
            System.out.println("账单删除成功");
        } else {
            System.out.println("账单删除失败");
        }
        return success; // 返回操作结果给Controller层
    }
    public boolean updateBill(Bill bill) {//更新账单
        System.out.println("更新账单: ID=" + bill.getId());//业务日志，记录更新操作
        boolean success = billDao.updateBill(bill);// 调用DAO层执行数据库更新账单操作
        if (success) {//结束日志
            System.out.println("账单更新成功");
        } else {
            System.out.println("账单更新失败");
        }
        return success; // 返回操作结果给Controller层
    }
    public List<Bill> getUserBills(int userId) {//获取指定用户的所有账单
        System.out.println("获取用户账单列表: 用户ID=" + userId);//业务日志，记录查询操作
        List<Bill> bills = billDao.findBillsByUserId(userId);// 调用DAO层执行数据库查询账单列表
        System.out.println("获取到 " + bills.size() + " 条账单记录");//结果日志，返回记录数
        return bills; // 返回账单列表给Controller层
    }
    public List<Category> getAllCategories() {//获取所有账单分类
        System.out.println("获取所有账单分类");//业务日志，记录查询操作
        List<Category> categories = categoryDao.findAll();// 调用DAO层获取分类列表
        System.out.println("获取到 " + categories.size() + " 个分类");//结果日志，返回记录数
        return categories; // 返回分类列表给Controller层
    }
    public List<Category> getCategoriesByType(int type) {//根据类型获取分类
        System.out.println("获取类型为 " + type + " 的分类");//业务日志，记录查询操作
        List<Category> categories = categoryDao.findByType(type);// 调用DAO层根据类型获取分类
        System.out.println("获取到 " + categories.size() + " 个该类型分类");//结果日志，返回记录数
        return categories; // 返回分类列表给Controller层
    }
    public Bill getBillById(int billId, int userId) {//根据ID获取单个账单
        System.out.println("根据ID获取账单: ID=" + billId + ", 用户ID=" + userId);//业务日志，记录查询操作
        Bill bill = billDao.findById(billId, userId);// 调用DAO层执行数据库查询操作
        if (bill != null) {//结果日志，是否找到
            System.out.println("找到账单: ID=" + billId);
        } else {
            System.out.println("未找到账单: ID=" + billId);
        }
        return bill; // 返回账单对象或null给Controller层
    }
}