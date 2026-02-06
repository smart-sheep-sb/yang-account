package cn.lazyaccount.controller;
// 导入必要的类和项目类
import cn.lazyaccount.model.Bill; // 账单模型类
import cn.lazyaccount.service.BillService; // 账单服务类
import cn.lazyaccount.util.JsonUtil; // JSON工具类
import cn.lazyaccount.util.WebUtil; // Web工具类
import com.sun.net.httpserver.HttpExchange; // HTTP交换对象
import com.sun.net.httpserver.HttpHandler; // HTTP处理器接口
import java.io.IOException; // IO异常
import java.util.Date; // 日期类
import java.util.Map; // Map接口
import java.util.HashMap; // HashMap实现

public class BillController {
    private final BillService billService = new BillService(); // 账单服务实例
    public HttpHandler addBillHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {// 检查HTTP方法
                WebUtil.sendError(exchange, "只允许POST方法", 405);
                return;
            }
            try {
                System.out.println("处理添加账单请求");
                String requestBody = WebUtil.readRequestBody(exchange);//读取请求体
                System.out.println("   请求数据: " + requestBody);
                Bill bill = JsonUtil.fromJson(requestBody, Bill.class);//将JSON转换为Bill对象
                if (bill.getUserId() == null) {//验证必要字段
                    WebUtil.sendError(exchange, "用户ID不能为空", 400);
                    return;
                }
                if (bill.getCategoryId() == null) {
                    WebUtil.sendError(exchange, "分类ID不能为空", 400);
                    return;
                }
                if (bill.getAmount() == null || bill.getAmount() <= 0) {
                    WebUtil.sendError(exchange, "金额必须大于0", 400);
                    return;
                }
                if (bill.getBillTime() == null) {//如果没有设置记账时间，使用当前时间
                    bill.setBillTime(new Date()); // 设置为当前时间
                }
                boolean success = billService.addBill(bill);//调用服务层添加账单
                if (success) {//根据结果返回响应
                    System.out.println("账单添加成功: 用户" + bill.getUserId() +
                            ", 金额" + bill.getAmount());//String类型可以直接传递给sendSuccess
                    WebUtil.sendSuccess(exchange, "账单添加成功");
                } else {
                    System.out.println("账单添加失败");
                    WebUtil.sendError(exchange, "账单添加失败", 500);
                }
            } catch (Exception e) {
                System.err.println("添加账单过程发生异常");//处理异常
                e.printStackTrace();
                WebUtil.sendError(exchange, "添加账单失败: " + e.getMessage(), 500);
            }
        }
    };
    public HttpHandler deleteBillHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"DELETE".equals(exchange.getRequestMethod())) {// 检查HTTP方法
                WebUtil.sendError(exchange, "只允许DELETE方法", 405);
                return;
            }
            try {
                System.out.println("🗑️ 处理删除账单请求");
                String path = exchange.getRequestURI().getPath();//从URL路径中提取账单ID
                System.out.println("   请求路径: " + path);
                String[] pathParts = path.split("/");// 路径格式：/api/bill/delete/123
                if (pathParts.length < 5) {
                    WebUtil.sendError(exchange, "URL格式错误，缺少账单ID", 400);
                    return;
                }
                int billId; // 获取账单ID
                try {
                    billId = Integer.parseInt(pathParts[pathParts.length - 1]);
                } catch (NumberFormatException e) {
                    WebUtil.sendError(exchange, "账单ID必须是数字", 400);
                    return;
                }
                // URL格式：/api/bill/delete/123?userId=1
                String query = exchange.getRequestURI().getQuery(); //从查询参数中获取用户ID
                Map<String, String> params = parseQuery(query);
                if (!params.containsKey("userId")) {
                    WebUtil.sendError(exchange, "缺少用户ID参数", 400);
                    return;
                }
                int userId;
                try {
                    userId = Integer.parseInt(params.get("userId"));
                } catch (NumberFormatException e) {
                    WebUtil.sendError(exchange, "用户ID必须是数字", 400);
                    return;
                }
                System.out.println("   删除账单: ID=" + billId + ", 用户ID=" + userId);
                boolean success = billService.deleteBill(billId, userId);//调用服务层删除账单
                if (success) {//根据结果返回响应
                    System.out.println("账单删除成功: ID=" + billId);
                    WebUtil.sendSuccess(exchange, "账单删除成功");
                } else {
                    System.out.println("账单删除失败: 账单不存在或无权限");
                    WebUtil.sendError(exchange, "账单删除失败或账单不存在", 404); // 404 Not Found
                }
            } catch (Exception e) {
                System.err.println("删除账单过程发生异常");//处理异常
                e.printStackTrace();
                WebUtil.sendError(exchange, "删除账单失败: " + e.getMessage(), 500);
            }
        }
    };
    public HttpHandler getBillsHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {//检查HTTP方法
                WebUtil.sendError(exchange, "只允许GET方法", 405);
                return;
            }
            try {
                System.out.println("处理获取账单列表请求");
                // URL格式：/api/bill/list?userId=1
                String query = exchange.getRequestURI().getQuery();//从查询参数中获取用户ID
                Map<String, String> params = parseQuery(query);
                if (!params.containsKey("userId")) {
                    WebUtil.sendError(exchange, "缺少用户ID参数", 400);
                    return;
                }
                int userId;
                try {
                    userId = Integer.parseInt(params.get("userId"));
                } catch (NumberFormatException e) {
                    WebUtil.sendError(exchange, "用户ID必须是数字", 400);
                    return;
                }
                System.out.println("获取用户账单: 用户ID=" + userId);
                var bills = billService.getUserBills(userId);//调用服务层获取账单列表
                System.out.println("返回账单列表，数量: " + bills.size());//返回账单列表
                WebUtil.sendSuccess(exchange, bills);
            } catch (Exception e) {
                System.err.println("获取账单列表过程发生异常");// 处理异常
                e.printStackTrace();
                WebUtil.sendError(exchange, "获取账单列表失败: " + e.getMessage(), 500);
            }
        }
    };
    public HttpHandler getCategoriesHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {//检查HTTP方法
                WebUtil.sendError(exchange, "只允许GET方法", 405);
                return;
            }
            try {
                System.out.println("处理获取分类列表请求");
                var categories = billService.getAllCategories();//调用服务层获取所有分类
                System.out.println("返回分类列表，数量: " + categories.size());//返回分类列表
                WebUtil.sendSuccess(exchange, categories);
            } catch (Exception e) {
                System.err.println("获取分类列表过程发生异常");//处理异常
                e.printStackTrace();
                WebUtil.sendError(exchange, "获取分类列表失败: " + e.getMessage(), 500);
            }
        }
    };
    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params; // 返回空Map
        }
        String[] pairs = query.split("&");// 按&分割参数
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");// 按=分割键值
            if (keyValue.length == 2) {
                String key = decodeUrl(keyValue[0]);// 解码URL编码
                String value = decodeUrl(keyValue[1]);
                params.put(key, value);
            } else if (keyValue.length == 1) {
                String key = decodeUrl(keyValue[0]);// 只有键没有值
                params.put(key, "");
            }
        }
        return params;
    }
    public HttpHandler updataBillHandler = new HttpHandler() {//实现账单编辑功能
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if("PUT".equals(exchange.getRequestMethod())){
                try {
                    String requestBody = WebUtil.readRequestBody(exchange);
                    Bill bill = JsonUtil.fromJson(requestBody , Bill.class);
                    boolean success = billService.updateBill(bill);
                    if(success){
                        WebUtil.sendSuccess(exchange,"更新成功");
                    }else {
                        WebUtil.sendError(exchange , "更新失败" , 400);
                    }
                }catch (Exception e){
                    WebUtil.sendError(exchange,"更新失败:" + e.getMessage(),500);
                }
            }

        }
    };
    private String decodeUrl(String encoded) {
        try {
            return java.net.URLDecoder.decode(encoded, "UTF-8");// 使用Java内置的URLDecoder
        } catch (Exception e) {
            return encoded;// 解码失败返回原始字符串
        }
    }
}