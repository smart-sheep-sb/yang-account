//用户的实体模型，对应数据库中的t_user表
package cn.lazyaccount.model;

import java.util.Date;//导入日期类

public class User {
    private Integer id;//用户ID，对应数据库的id字段
    private String username;//用户名称，登录使用，对应数据库的username字段
    private String password;//密码，登录使用，对应数据库的password字段
    private String nickname;//昵称，对应数据库的nickname字段
    private Date createTime;//创建时间，用户的注册时间，对应数据库的create_time字段
    public User(){
        //用于反射创建对象,空函数
    }
    public User(String username,String password){
        this.username = username;//设置用户名
        this.password = password;//设置密码
    }
    public Integer getId(){//获取用户ID
        return id;//返回用户ID
    }
    public void setId(Integer id){//设置用户ID
        this.id = id;//设置用户ID
    }
    public String getUsername(){//获取用户名
        return username;//返回用户名
    }
    public void setUsername(String username){//设置用户名
        this.username = username;//设置用户名
    }
    public String getPassword(){//获取密码
        return password;//返回密码
    }
    public void setPassword(String password){//设置密码
        this.password = password;//设置密码
    }
    public String getNickname() {//获取昵称
        return nickname; // 返回昵称
    }
    public void setNickname(String nickname) {//设置昵称
        this.nickname = nickname; // 设置昵称
    }
    public Date getCreateTime() {//获取创建时间
        return createTime; // 返回创建时间
    }
    public void setCreateTime(Date createTime) {//设置创建时间
        this.createTime = createTime; // 设置创建时间
    }
    @Override
    public String toString() {//调试和日志的输出
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}

