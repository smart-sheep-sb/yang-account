//处理前端发起的增删改查账单和查询账单分类的HTTP请求
package cn.lazyaccount.controller;
// 导入必要的类和项目类
import cn.lazyaccount.model.Bill; // 导入账单模型类
import cn.lazyaccount.service.BillService; // 导入账单服务类
import cn.lazyaccount.util.JsonUtil; // 导入JSON工具类
import cn.lazyaccount.util.WebUtil; // 导入Web工具类
import com.sun.net.httpserver.HttpExchange; // 导入HTTP交换对象
import com.sun.net.httpserver.HttpHandler; // 导入HTTP处理器接口
import java.io.IOException; // 导入IO异常
import java.util.Date; // 导入日期类
import java.util.Map; // 导入Map接口
import java.util.HashMap; // 导入HashMap实现

public class BillController {//账单控制器类，处理账单相关的HTTP请求（增删改查）
    private final BillService billService = new BillService(); // 创建账单服务实例
    public HttpHandler addBillHandler = new HttpHandler() {//添加账单处理器，处理添加账单的POST请求
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {// 检查请求方法是否为POST方法
                WebUtil.sendError(exchange, "只允许POST方法", 405);//非POST方法返回405错误
                return;
            }
            try {
                System.out.println("处理添加账单请求");//输出内容
                String requestBody = WebUtil.readRequestBody(exchange);//读取HTTP请求体中的内容（JSON格式的账单数据）
                System.out.println("   请求数据: " + requestBody);//输出内容
                Bill bill = JsonUtil.fromJson(requestBody, Bill.class);//将JSON转换为Bill对象（反序列化）
                if (bill.getUserId() == null) {//验证必要字段，用户ID不能为空
                    WebUtil.sendError(exchange, "用户ID不能为空", 400);
                    return;
                }
                if (bill.getCategoryId() == null) {//验证必要字段，分类ID不能为空
                    WebUtil.sendError(exchange, "分类ID不能为空", 400);
                    return;
                }
                if (bill.getAmount() == null || bill.getAmount() <= 0) {//验证必要字段，金额必须大于0
                    WebUtil.sendError(exchange, "金额必须大于0", 400);
                    return;
                }
                if (bill.getBillTime() == null) {//如果没有设置记账时间，系统默认当前使用时间
                    bill.setBillTime(new Date()); // 设置为当前时间
                }
                boolean success = billService.addBill(bill);//调用服务层添加账单，返回是否成功
                if (success) {//根据结果返回响应
                    System.out.println("账单添加成功: 用户" + bill.getUserId() +//打印日志，账单添加成功
                            ", 金额" + bill.getAmount());//String类型可以直接传递给sendSuccess
                    WebUtil.sendSuccess(exchange, "账单添加成功");//返回200成功响应，提升信息为账单添加成功
                } else {
                    System.out.println("账单添加失败");//输出内容
                    WebUtil.sendError(exchange, "账单添加失败", 500);//返回500（服务器内部错误），提示添加失败
                }
            } catch (Exception e) {//捕获所有异常，打印错误日志
                System.err.println("添加账单过程发生异常");//处理异常
                e.printStackTrace();//打印日志
                WebUtil.sendError(exchange, "添加账单失败: " + e.getMessage(), 500);//返回500错误，携带异常信息
            }
        }
    };
    public HttpHandler deleteBillHandler = new HttpHandler() {//删除账单处理器，处理删除账单的DELETE请求
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"DELETE".equals(exchange.getRequestMethod())) {// 检查请求方法是否为DELETE
                WebUtil.sendError(exchange, "只允许DELETE方法", 405);//非DELETE则返回405错误
                return;
            }
            try {
                System.out.println("🗑️ 处理删除账单请求");//内容输出
                String path = exchange.getRequestURI().getPath();//获取请求的URL格式
                System.out.println("   请求路径: " + path);// 路径格式：/api/bill/delete/123
                String[] pathParts = path.split("/");//按“/“分割路径，提取账单ID
                if (pathParts.length < 5) {//路径格式验证：至少要5个部分（比如/api/bill/delete/123 分割后长度为5）
                    WebUtil.sendError(exchange, "URL格式错误，缺少账单ID", 400);//如果格式错误，则返回400错误，提示词
                    return;
                }
                int billId; //定义账单ID变量，存储要删除的账单ID
                try {//尝试将路径最后一位转换位整数（账单ID）
                    billId = Integer.parseInt(pathParts[pathParts.length - 1]);
                } catch (NumberFormatException e) {//转换失败（非数字）
                    WebUtil.sendError(exchange, "账单ID必须是数字", 400);//转换失败则返回400错误，提示账单ID必须为数字
                    return;
                }
                // URL格式：/api/bill/delete/123?userId=1
                String query = exchange.getRequestURI().getQuery(); //从查询参数中获取用户ID
                Map<String, String> params = parseQuery(query);//调用自定义方法解析查询参数为Map
                if (!params.containsKey("userId")){//验证查询参数，必须包含userId
                    WebUtil.sendError(exchange, "缺少用户ID参数", 400);//返回400错误
                    return;
                }
                int userId;//定义用户ID变量，存储操作的用户ID
                try {//尝试userId参数转换为整数
                    userId = Integer.parseInt(params.get("userId"));
                } catch (NumberFormatException e) {//转换失败
                    WebUtil.sendError(exchange, "用户ID必须是数字", 400);//返回400错误
                    return;
                }
                System.out.println("   删除账单: ID=" + billId + ", 用户ID=" + userId);//打印日志，记录要删除的账单ID和用户ID
                boolean success = billService.deleteBill(billId, userId);//调用服务层deleteBill方法删除账单，返回布尔值表示是否删除成功
                if (success) {//删除成功
                    System.out.println("账单删除成功: ID=" + billId);//打印成功日志
                    WebUtil.sendSuccess(exchange, "账单删除成功");//发送200成功响应
                } else {//删除失败（账单不存在或无权限）
                    System.out.println("账单删除失败: 账单不存在或无权限");//打印失败原因
                    WebUtil.sendError(exchange, "账单删除失败或账单不存在", 404); // 404 Not Found
                }
            } catch (Exception e) {//捕获所有为预期的异常
                System.err.println("删除账单过程发生异常");//打印异常提示
                e.printStackTrace();//打印异常堆栈
                WebUtil.sendError(exchange, "删除账单失败: " + e.getMessage(), 500);//发送500错误响应，携带具体异常信息
            }
        }
    };
    public HttpHandler getBillsHandler = new HttpHandler() {//定义获取账单列表的HTTP处理器，实现HttpHandle接口处理get请求
        @Override
        public void handle(HttpExchange exchange) throws IOException {//重写HANDLE方法，核心逻辑，跟据用户ID查询账单列表
            if (!"GET".equals(exchange.getRequestMethod())) {//判断请求方法是否为GET
                WebUtil.sendError(exchange, "只允许GET方法", 405);//非GET则返回405错误
                return;
            }
            try {//捕获处理请求过程中的异常
                System.out.println("处理获取账单列表请求");//输出内容
                // URL格式：/api/bill/list?userId=1
                String query = exchange.getRequestURI().getQuery();//从查询参数中获取用户ID
                Map<String, String> params = parseQuery(query);//解析查询参数为Map
                if (!params.containsKey("userId")) {//验证查询参数，必须包含userId
                    WebUtil.sendError(exchange, "缺少用户ID参数", 400);//否则返回400Id
                    return;
                }
                int userId;//定义用户ID变量
                try {//尝试将userId参数转换为整数
                    userId = Integer.parseInt(params.get("userId"));
                } catch (NumberFormatException e) {//转换失败
                    WebUtil.sendError(exchange, "用户ID必须是数字", 400);//返回400错误
                    return;
                }
                System.out.println("获取用户账单: 用户ID=" + userId);//打印日志，记录要查询的用户ID
                var bills = billService.getUserBills(userId);//调用服务层getUserBills方法，获取用户的所有账单列表
                System.out.println("返回账单列表，数量: " + bills.size());//打印日志，记录返回的账单数量
                WebUtil.sendSuccess(exchange, bills);//发送200成功响应，携带账单列表数据（自动序列化为JSON）
            } catch (Exception e) {//捕获所有未预期的异常
                System.err.println("获取账单列表过程发生异常");//打印异常
                e.printStackTrace();//打印异常堆栈
                WebUtil.sendError(exchange, "获取账单列表失败: " + e.getMessage(), 500);//发送500错误响应，携带具体异常信息
            }
        }
    };
    public HttpHandler getCategoriesHandler = new HttpHandler() {//定义获取分类列表的HTTP处理器，实现HttpHandler接口处理GET请求
        @Override
        public void handle(HttpExchange exchange) throws IOException {//重写handle方法，核心逻辑：查询所有账单分类
            if (!"GET".equals(exchange.getRequestMethod())) {//判断请求方法是否为GET
                WebUtil.sendError(exchange, "只允许GET方法", 405);//非GET则返回405错误
                return;
            }
            try {//捕获处理请求过程中的异常
                System.out.println("处理获取分类列表请求");//打印日志
                var categories = billService.getAllCategories();//调用服务层的getAllCategories方法，获取所有账单分类列表
                System.out.println("返回分类列表，数量: " + categories.size());//返回分类列表，打印日志
                WebUtil.sendSuccess(exchange, categories);//发送200成功响应，携带分类列表数据（自动序列化为JSON）
            } catch (Exception e) {//捕获所有未预期的异常
                System.err.println("获取分类列表过程发生异常");//打印异常
                e.printStackTrace();//打印异常堆栈
                WebUtil.sendError(exchange, "获取分类列表失败: " + e.getMessage(), 500);//发送500错误响应，携带具体异常信息
            }
        }
    };
    private Map<String, String> parseQuery(String query) {//私有工具方法，解析URL查询参数为Map键值对
        Map<String, String> params = new HashMap<>();//创建空的HashMap存储参数
        if (query == null || query.isEmpty()) {//如果查询参数为空
            return params; // 直接返回空Map
        }
        String[] pairs = query.split("&");// 按&分割多个参数
        for (String pair : pairs) {//便历每个参数键值对
            String[] keyValue = pair.split("=");// 按=分割键值
            if (keyValue.length == 2) {//有键有值的情况
                String key = decodeUrl(keyValue[0]);// 解码URL编码
                String value = decodeUrl(keyValue[1]);//解码值
                params.put(key, value);//将键值对存入Map
            } else if (keyValue.length == 1) {//只有键没有值的情况
                String key = decodeUrl(keyValue[0]);//解码键
                params.put(key, "");//值设置为空字符串，存入Map
            }
        }
        return params;//返回解析后的参数Map
    }
    public HttpHandler updateBillHandler = new HttpHandler() {//定义更新账单的HTTP处理器，处理PUT请求
        @Override
        public void handle(HttpExchange exchange) throws IOException {//重写handle方法，核心逻辑，更新账单信息
            if("PUT".equals(exchange.getRequestMethod())){//判断请求是否为PUT方法
                try {//捕获处理请求过程中的异常
                    String requestBody = WebUtil.readRequestBody(exchange);//读取请求体中的JSON数据
                    Bill bill = JsonUtil.fromJson(requestBody , Bill.class);//将JSON字符串转化为Bill对象
                    boolean success = billService.updateBill(bill);//调用服务层的updateBill方法更新账单，返回布尔值表示是否更新成功
                    if(success){//更新成功
                        WebUtil.sendSuccess(exchange,"更新成功");//发送200成功响应
                    }else {//更新失败
                        WebUtil.sendError(exchange , "更新失败" , 400);//发送400错误响应
                    }
                }catch (Exception e){//捕获所有未预期的异常
                    WebUtil.sendError(exchange,"更新失败:" + e.getMessage(),500);//发送500错误响应，携带具体异常信息
                }
            }

        }
    };
    private String decodeUrl(String encoded) {//私有工具方法，解码URL编码的字符串
        try {
            return java.net.URLDecoder.decode(encoded, "UTF-8");// 使用UTF-8编码解码URL字符串
        } catch (Exception e) {//解码失败
            return encoded;//返回原始字符串
        }
    }
}