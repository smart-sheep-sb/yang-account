package cn.lazyaccount.service;

import cn.lazyaccount.dao.BillDao;
import cn.lazyaccount.dao.CategoryDao;
import cn.lazyaccount.model.Bill;
import cn.lazyaccount.model.Category;
import java.util.List;
public class BillService {
    private final BillDao billDao = new BillDao();// 账单数据访问对象
    private final CategoryDao categoryDao = new CategoryDao();// 分类数据访问对象
    public boolean addBill(Bill bill) {
        System.out.println("添加账单: 用户ID=" + bill.getUserId() +
                ", 金额=" + bill.getAmount());
        boolean success = billDao.addBill(bill);// 调用DAO层添加账单
        if (success) {
            System.out.println("账单添加成功");
        } else {
            System.out.println("账单添加失败");
        }
        return success; // 返回操作结果
    }
    public boolean deleteBill(int billId, int userId) {
        System.out.println("删除账单: ID=" + billId + ", 用户ID=" + userId);
        boolean success = billDao.deleteBill(billId, userId);// 调用DAO层删除账单
        if (success) {
            System.out.println("账单删除成功");
        } else {
            System.out.println("账单删除失败");
        }
        return success; // 返回操作结果
    }
    public boolean updateBill(Bill bill) {
        System.out.println("更新账单: ID=" + bill.getId());
        boolean success = billDao.updateBill(bill);// 调用DAO层更新账单
        if (success) {
            System.out.println("账单更新成功");
        } else {
            System.out.println("账单更新失败");
        }
        return success; // 返回操作结果
    }
    public List<Bill> getUserBills(int userId) {
        System.out.println("获取用户账单列表: 用户ID=" + userId);
        List<Bill> bills = billDao.findBillsByUserId(userId);// 调用DAO层获取账单列表
        System.out.println("获取到 " + bills.size() + " 条账单记录");
        return bills; // 返回账单列表
    }
    public List<Category> getAllCategories() {
        System.out.println("获取所有账单分类");
        List<Category> categories = categoryDao.findAll();// 调用DAO层获取分类列表
        System.out.println("获取到 " + categories.size() + " 个分类");
        return categories; // 返回分类列表
    }
    public List<Category> getCategoriesByType(int type) {
        System.out.println("获取类型为 " + type + " 的分类");
        List<Category> categories = categoryDao.findByType(type);// 调用DAO层根据类型获取分类
        System.out.println("获取到 " + categories.size() + " 个该类型分类");
        return categories; // 返回分类列表
    }
    public Bill getBillById(int billId, int userId) {
        System.out.println("根据ID获取账单: ID=" + billId + ", 用户ID=" + userId);
        Bill bill = billDao.findById(billId, userId);// 调用DAO层获取账单
        if (bill != null) {
            System.out.println("找到账单: ID=" + billId);
        } else {
            System.out.println("未找到账单: ID=" + billId);
        }
        return bill; // 返回账单对象或null
    }
}