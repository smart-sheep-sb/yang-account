//提供统一的JSON处理功能
package cn.lazyaccount.util;

import com.google.gson.Gson; // 导入Gson类
import com.google.gson.GsonBuilder; // 导入GsonBuilder类
import com.google.gson.reflect.TypeToken; // 导入TypeToken类
import java.lang.reflect.Type; // 导入Type类
import java.util.Date; // 导入日期类
import java.util.Map; // 导入Map接口
import java.util.List; // 导入List接口

public class JsonUtil {//提供JSON字符串和Java对象的转换功能
    // Gson实例，使用单例模式避免重复创建
    private static final Gson gson = new GsonBuilder()// 使用GsonBuilder配置日期格式
            .setDateFormat("yyyy-MM-dd HH:mm:ss") // 设置日期格式
            .create(); // 创建Gson实例
    private JsonUtil() {
        // 工具类，不需要实例化
    }
    public static String toJson(Object obj) { // 调用Gson的toJson方法进行转换
        return gson.toJson(obj);//json格式的字符串
    }
    public static <T> T fromJson(String json, Class<T> clazz) {// 调用Gson的fromJson方法进行转换
        return gson.fromJson(json, clazz);//转换后的Java对象
    }
    public static <T> T fromJson(String json, Type type) {// 调用Gson的fromJson方法进行转换，支持复杂类型
        return gson.fromJson(json, type);//转化后的Java对象
    }
    public static Map<String, Object> fromJsonToMap(String json) {// 创建TypeToken描述Map<String, Object>类型
        Type type = new TypeToken<Map<String, Object>>(){}.getType();// 调用Gson进行转换
        return gson.fromJson(json, type);//Map对象，键值对形式
    }
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {// 创建TypeToken描述List<T>类型
        Type type = TypeToken.getParameterized(List.class, clazz).getType();// 调用Gson进行转换
        return gson.fromJson(json, type);//List对象
    }
    public static boolean isValidJson(String json) {
        try {// 尝试解析JSON，如果成功则有效
            gson.fromJson(json, Object.class);
            return true; // 解析成功，返回true
        } catch (Exception e) {
            return false;// 解析失败，返回false
        }
    }
    public static String toPrettyJson(Object obj) {
        Gson prettyGson = new GsonBuilder()// 创建新的Gson实例，设置格式化输出
                .setPrettyPrinting() // 设置美化输出
                .setDateFormat("yyyy-MM-dd HH:mm:ss") // 设置日期格式
                .create(); // 创建Gson实例
        return prettyGson.toJson(obj); // 返回格式化后的JSON
    }
}