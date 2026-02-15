//登录认证相关
// ========== 用户认证模块 ==========

// 检查登录状态
function checkLoginStatus() {// 定义检查登录状态函数
    const userData = localStorage.getItem('currentUser');// 从本地存储中获取当前用户数据
    if (!userData) {// 如果用户数据不存在
        window.location.href = 'index.html';// 跳转到登录首页
        return false;// 返回false表示未登录
    }

    try {// 尝试解析用户数据
        currentUser = JSON.parse(userData);// 将JSON字符串解析为对象并赋值给currentUser
        document.getElementById('welcomeMessage').textContent =// 获取欢迎消息元素并设置其文本内容
            `欢迎，${currentUser.username}`;// 显示欢迎消息，包含用户名
        if (currentUser.id) {// 如果用户对象中包含id属性
            currentUserId = currentUser.id;// 将用户ID赋值给全局变量currentUserId
        }
        return true;// 返回true表示已登录
    } catch (e) {// 捕获解析错误
        window.location.href = 'index.html';// 发生错误时也跳转到登录首页
        return false;// 返回false
    }
}

// 退出登录
function logout() {// 定义退出登录函数
    if (confirm('确定要退出登录吗？')) {// 弹出确认对话框，询问用户是否确定退出
        localStorage.removeItem('currentUser');// 从本地存储中移除当前用户数据
        window.location.href = 'index.html';// 跳转到登录首页
    }
}