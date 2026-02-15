//配置变量
// ========== 配置变量 ==========
const CONFIG = {// 定义配置对象常量
    API_BASE_URL: 'http://localhost:8080/cn.lazyaccount', // 后端API地址,用于所有接口请求的基础URL
    RECORDS_PER_PAGE: 6 // 每页显示记录数
};

// 全局变量
let records = [];// 存储所有记账记录的数组，初始为空
let currentUser = null;// 存储当前登录用户信息的对象，初始为null
let currentUserId = null;// 存储当前登录用户的ID，初始为null，用于API请求