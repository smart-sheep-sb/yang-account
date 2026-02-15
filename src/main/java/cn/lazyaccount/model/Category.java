//账单分类的实体模型，对应数据库中的t_bill_category表
package cn.lazyaccount.model;

public class Category {
    private Integer id;//分类ID，主键，对应数据库的id字段
    private String name;//分类名称，对应数据库的name字段
    private Integer type;//分类类型：1-收入，2-支出，对应数据库的type字段
    public Category(){
        //构造空函数
    }
    public Category(String name,Integer type){
        this.name = name;//设置分类名称
        this.type = type;//设置分类类型
    }
    public Integer getId(){//获取分类ID
        return id;//返回分类ID
    }
    public void setId(Integer id){//设置分类ID
        this.id = id;//设置分类ID
    }
    public String getName(){//获取分类名称
        return name;//返回分类名称
    }
    public void setName(String name){//设置分类名称
        this.name = name;//设置分类名称
    }
    public Integer getType() {
        return type; // 返回分类类型
    }
    public void setType(Integer type) {//设置分类类型
        this.type = type; // 设置分类类型
    }
    public boolean isIncome() {//判断是否为收入
        return type != null && type == 1; // 类型为1表示收入
    }
    public boolean isExpense() {//判断是否为支出
        return type != null && type == 2; // 类型为2表示支出
    }
    public String getTypeText() {//前端显示为支付或者收入
        if (type == null) {
            return "未知"; // 类型为空返回未知
        }
        return type == 1 ? "收入" : "支出"; // 1返回收入，2返回支出
    }
    @Override
    public String toString() {//分类信息的书写
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", typeText='" + getTypeText() + '\'' +
                '}';
    }
}
