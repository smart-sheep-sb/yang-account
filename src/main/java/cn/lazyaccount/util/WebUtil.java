package cn.lazyaccount.util;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
public class WebUtil {
    private WebUtil() {
        // 工具类，不需要实例化
    }
    public static void sendJsonResponse(HttpExchange exchange, String json, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");// 设置响应头：Content-Type为application/json，字符集为UTF-8
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");// 设置CORS（跨域资源共享）头部，允许所有来源访问
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");// 允许的HTTP方法
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");// 允许的请求头
        byte[] response = json.getBytes(StandardCharsets.UTF_8);// 将JSON字符串转换为UTF-8字节数组
        exchange.sendResponseHeaders(statusCode, response.length);// 发送响应头：状态码和内容长度
        try (OutputStream os = exchange.getResponseBody()) {// 获取响应输出流
            os.write(response);// 将JSON字节数组写入输出流
            os.flush();// 刷新输出流，确保数据发送
        }
        // try-with-resources会自动关闭OutputStream
    }
    public static void sendSuccess(HttpExchange exchange, Object data) throws IOException {
        ApiResponse response = new ApiResponse(true, "操作成功", data);// 创建成功响应对象 - 注意：data参数类型是Object，可以接受任何类型
        String json = JsonUtil.toJson(response);// 转换为JSON字符串
        sendJsonResponse(exchange, json, 200);// 发送响应，状态码为200
    }
    public static void sendError(HttpExchange exchange, String message, int statusCode) throws IOException {
        ApiResponse response = new ApiResponse(false, message, null);// 创建错误响应对象
        String json = JsonUtil.toJson(response);// 转换为JSON字符串
        sendJsonResponse(exchange, json, statusCode); // 发送响应，使用指定的状态码
    }
    public static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {// 获取请求输入流
            byte[] bytes = is.readAllBytes();// 读取输入流中的所有字节
            return new String(bytes, StandardCharsets.UTF_8);// 将字节数组转换为UTF-8字符串
        }
        // try-with-resources会自动关闭InputStream
    }
    public static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();// 创建Map存储参数
        if (query == null || query.isEmpty()) {// 检查查询参数是否为空
            return params; // 返回空Map
        }
        String[] pairs = query.split("&");// 按&符号分割参数
        for (String pair : pairs) {// 遍历每个参数对
            String[] keyValue = pair.split("=");// 按=符号分割键值
            if (keyValue.length == 2) {// 确保键值对格式正确
                String key = decodeUrl(keyValue[0]);// 解码URL编码的参数值
                String value = decodeUrl(keyValue[1]);
                params.put(key, value);// 放入Map
            } else if (keyValue.length == 1) {
                String key = decodeUrl(keyValue[0]);// 只有键没有值的情况
                params.put(key, ""); // 值为空字符串
            }
        }
        return params; // 返回参数Map
    }
    private static String decodeUrl(String encoded) {
        try {
            return java.net.URLDecoder.decode(encoded, StandardCharsets.UTF_8.name());// 使用URLDecoder解码
        } catch (Exception e) {
            return encoded;// 解码失败返回原始字符串
        }
    }
    public static String getClientIp(HttpExchange exchange) {
        String xForwardedFor = exchange.getRequestHeaders().getFirst("X-Forwarded-For");// 从请求头中获取X-Forwarded-For（代理服务器传递的真实IP）
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();// 取第一个IP（客户端真实IP）
        }
        return exchange.getRemoteAddress().getAddress().getHostAddress();// 如果没有代理，直接获取远程地址
    }
    public static class ApiResponse {
        private boolean success;// 操作是否成功
        private String message;// 响应消息
        private Object data;// 响应数据 - 使用Object类型，可以接受任何类型：Map、List、String、自定义对象
        public ApiResponse(boolean success, String message, Object data) {
            this.success = success; // 设置成功标志
            this.message = message; // 设置消息
            this.data = data; // 设置数据
        }
        public boolean isSuccess() {// Getter方法
            return success;
        }
        public String getMessage() {
            return message;
        }
        public Object getData() {
            return data; // 返回Object类型，调用者需要自己转换
        }
        public void setSuccess(boolean success) {// Setter方法
            this.success = success;
        }
        public void setMessage(String message) {
            this.message = message;
        }
        public void setData(Object data) {
            this.data = data;
        }
    }
}