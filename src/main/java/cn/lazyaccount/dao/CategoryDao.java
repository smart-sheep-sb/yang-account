package cn.lazyaccount.dao;

import cn.lazyaccount.model.Category;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
public class CategoryDao {
    public List<Category> findAll() {
        String sql = "SELECT id, name, type FROM t_bill_category ORDER BY type, name";// SQL查询语句：按类型和名称排序
        return BaseDao.queryForList(sql, new BaseDao.RowMapper<Category>() {// 执行查询并返回列表
            @Override
            public Category mapRow(ResultSet rs) throws SQLException {
                Category category = new Category();// 创建Category对象并设置属性
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setType(rs.getInt("type"));
                return category; // 返回分类对象
            }
        });
    }
    public List<Category> findByType(int type) {
        String sql = "SELECT id, name, type FROM t_bill_category WHERE type = ? ORDER BY name"; // SQL查询语句：根据类型筛选
        return BaseDao.queryForList(sql, new BaseDao.RowMapper<Category>() {// 执行查询并返回列表
            @Override
            public Category mapRow(ResultSet rs) throws SQLException {
                Category category = new Category();// 创建Category对象并设置属性
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setType(rs.getInt("type"));
                return category; // 返回分类对象
            }
        }, type); // type是SQL参数
    }
    public Category findById(int id) {
        String sql = "SELECT id, name, type FROM t_bill_category WHERE id = ?";// SQL查询语句
        return BaseDao.queryForObject(sql, new BaseDao.RowMapper<Category>() {//执行查询
            @Override
            public Category mapRow(ResultSet rs) throws SQLException {
                Category category = new Category(); // 创建Category对象并设置属性
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setType(rs.getInt("type"));
                return category; // 返回分类对象
            }
        }, id); // id是SQL参数
    }
    public boolean addCategory(Category category) {
        String sql = "INSERT INTO t_bill_category (name, type) VALUES (?, ?)";// SQL插入语句
        int result = BaseDao.executeUpdate(sql,//执行插入操作
                category.getName(),
                category.getType()
        );
        return result > 0; // 返回操作结果
    }
    public boolean updateCategory(Category category) {
        String sql = "UPDATE t_bill_category SET name = ?, type = ? WHERE id = ?";// SQL更新语句
        int result = BaseDao.executeUpdate(sql,// 执行更新操作
                category.getName(),
                category.getType(),
                category.getId()
        );
        return result > 0; // 返回操作结果
    }
    public boolean deleteCategory(int id) {
        String sql = "DELETE FROM t_bill_category WHERE id = ?";// SQL删除语句
        int result = BaseDao.executeUpdate(sql, id);// 执行删除操作
        return result > 0; // 返回操作结果
    }
}