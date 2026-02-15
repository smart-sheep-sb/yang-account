//账单数据的实体模型，对应数据库中的t_bill表
package cn.lazyaccount.model;

import java.util.Date; // 导入日期类
public class Bill {
    private Integer id;// 账单ID，主键，对应数据库中的id字段
    private Integer userId;// 用户ID，外键关联t_user表，对应数据库中的user_id字段
    private Integer categoryId;// 分类ID，外键关联t_bill_category表，对应数据库中的category_id字段
    private Double amount; // 金额，记账的数额，对应数据库中的amount字段
    private String remark;// 备注，对账单的说明，对应数据库中的remark字段
    private Date billTime;// 记账时间，账单发生的时间，对应数据库中的bill_time字段
    private Date createTime;// 创建时间，记录创建的时间，对应数据库中的create_time字段
    private String categoryName;// 分类名称，通过关联查询获取，方便前端显示
    private Integer categoryType;// 分类类型，1-收入，2-支出，通过关联查询获取
    public Bill() {
        // 空构造函数
    }
    public Integer getId() {//获取账单ID
        return id; // 返回账单ID
    }
    public void setId(Integer id) {//设置账单ID
        this.id = id; // 设置账单ID
    }
    public Integer getUserId() {//获取用户ID
        return userId; // 返回用户ID
    }
    public void setUserId(Integer userId) {//设置用户ID
        this.userId = userId; // 设置用户ID
    }
    public Integer getCategoryId() {//获取分类ID
        return categoryId; // 返回分类ID
    }
    public void setCategoryId(Integer categoryId) {//设置分类ID
        this.categoryId = categoryId; // 设置分类ID
    }
    public Double getAmount() {//获取金额
        return amount; // 返回金额
    }
    public void setAmount(Double amount) {//设置金额
        this.amount = amount; // 设置金额
    }
    public String getRemark() {//获取备注
        return remark; // 返回备注
    }
    public void setRemark(String remark) {//设置备注
        this.remark = remark; // 设置备注
    }
    public Date getBillTime() {//获取记账时间
        return billTime; // 返回记账时间
    }
    public void setBillTime(Date billTime) {//设置记账时间
        this.billTime = billTime; // 设置记账时间
    }
    public Date getCreateTime() {//获取创建时间
        return createTime; // 返回创建时间
    }
    public void setCreateTime(Date createTime) {//设置创建时间
        this.createTime = createTime; // 设置创建时间
    }
    public String getCategoryName() {//获取分类名称
        return categoryName; // 返回分类名称
    }
    public void setCategoryName(String categoryName) {//设置分类名称
        this.categoryName = categoryName; // 设置分类名称
    }
    public Integer getCategoryType() {//获取分类类型
        return categoryType; // 返回分类类型
    }
    public void setCategoryType(Integer categoryType) {//设置分类类型
        this.categoryType = categoryType; // 设置分类类型
    }
    public boolean isIncome() {//判断是否为收入
        return categoryType != null && categoryType == 1; // 类型为1表示收入
    }
    public boolean isExpense() {//判断是否为支出
        return categoryType != null && categoryType == 2; // 类型为2表示支出
    }
    @Override
    public String toString() {//账单信息
        return "Bill{" +
                "id=" + id +
                ", userId=" + userId +
                ", categoryId=" + categoryId +
                ", amount=" + amount +
                ", remark='" + remark + '\'' +
                ", billTime=" + billTime +
                ", categoryName='" + categoryName + '\'' +
                ", categoryType=" + categoryType +
                '}';
    }
}