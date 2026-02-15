//管理账单分类数据的增删查改（CRUD）操作
package cn.lazyaccount.dao;

import cn.lazyaccount.model.Category;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
public class CategoryDao {
    public List<Category> findAll() {
        String sql = "SELECT id, name, type FROM t_bill_category ORDER BY type, name";// SQL查询语句：按类型和名称排序
        return BaseDao.queryForList(sql, new BaseDao.RowMapper<Category>() {// 执行查询并返回Category对象列表
            @Override
            public Category mapRow(ResultSet rs) throws SQLException {//结果集映射方法，将每一行转换为Category对象
                Category category = new Category();// 创建Category对象并设置属性
                category.setId(rs.getInt("id"));//设置分类ID
                category.setName(rs.getString("name"));//设置分类名称
                category.setType(rs.getInt("type"));//设置分类类型
                return category; // 返回映射完成的Category对象
            }
        });
    }
    public List<Category> findByType(int type) {//根据类型查询分类
        String sql = "SELECT id, name, type FROM t_bill_category WHERE type = ? ORDER BY name"; // SQL查询语句：根据类型筛选并按照名称排序
        return BaseDao.queryForList(sql, new BaseDao.RowMapper<Category>() {// 执行查询并返回Category列表
            @Override
            public Category mapRow(ResultSet rs) throws SQLException {//结果集映射方法
                Category category = new Category();// 创建Category对象并设置属性
                category.setId(rs.getInt("id"));//从结果集中获取ID字段
                category.setName(rs.getString("name"));//从结果集中获取name字段
                category.setType(rs.getInt("type"));//从结果集中获取type字段
                return category; // 返回构建好的Category对象
            }
        }, type); // type参数替换SQL中的？占位符
    }
    public Category findById(int id) {//根据ID查询单个分类
        String sql = "SELECT id, name, type FROM t_bill_category WHERE id = ?";// SQL查询语句，根据主键ID查询
        return BaseDao.queryForObject(sql, new BaseDao.RowMapper<Category>() {//执行查询并返回单个对象
            @Override
            public Category mapRow(ResultSet rs) throws SQLException {//结果集映射方法
                Category category = new Category(); // 创建Category对象并设置属性
                category.setId(rs.getInt("id"));//映射ID字段
                category.setName(rs.getString("name"));//映射名称字段
                category.setType(rs.getInt("type"));//映射类型字段
                return category; // 返回查询到的分类对象
            }
        }, id); // id是SQL参数的？占位符
    }
    public boolean addCategory(Category category) {//添加新分类
        String sql = "INSERT INTO t_bill_category (name, type) VALUES (?, ?)";// SQL插入语句，向分类表插入新记录
        int result = BaseDao.executeUpdate(sql,//执行插入操作，返回受影响的行数
                category.getName(),//第一个参数，分类名称
                category.getType()//第二个参数，分类类型
        );
        return result > 0; //如果受影响行数>0，表示插入成功
    }
    public boolean updateCategory(Category category) {//更新分类信息
        String sql = "UPDATE t_bill_category SET name = ?, type = ? WHERE id = ?";// SQL更新语句，根据ID更新分类信息
        int result = BaseDao.executeUpdate(sql,// 执行更新操作
                category.getName(),//新分类名称
                category.getType(),//新分类类型
                category.getId()//要更新的分类ID（where条件）
        );
        return result > 0; // 返回是否更新成功
    }
    public boolean deleteCategory(int id) {//删除分类
        String sql = "DELETE FROM t_bill_category WHERE id = ?";// SQL删除语句，根据ID删除分类记录
        int result = BaseDao.executeUpdate(sql, id);// 执行删除操作，传入ID参数
        return result > 0; // 返回是否删除成功
    }
}