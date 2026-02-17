羊氏记账 🐑💰
一个简单、智能的个人记账管理系统，帮助你轻松管理每一笔收支，让财务管理更简单。

功能特性 ✨
✅ 记账管理：支持收入 / 支出记录，可按分类（餐饮、住房、购物等）管理
✅ 增删改查：对账单记录进行灵活编辑和删除
✅ 收支统计：通过饼图直观展示收入 / 支出分类占比
✅ 支出预警：月度支出超限自动提醒，避免超支
✅ 记录搜索：支持按备注本地搜索历史账单
✅ 用户系统：支持用户注册、登录、切换，保障数据隔离

技术栈 🛠️
后端：Java
前端：HTML、CSS、JavaScript
数据库：SQLite
开发工具：IntelliJ IDEA

快速开始 🚀
克隆或下载项目到本地
使用 IntelliJ IDEA 打开项目
找到 Main.java 主类并运行
访问 http://localhost:8080/main.html 即可使用

界面展示 📸
登录 / 注册页面
输入用户名和密码即可登录，新用户可点击 “立即注册” 创建账号。
首页概览
展示总收入、总支出、当前余额，并提供月度支出超限预警。
最近记录
查看所有收支记录，支持本地搜索和分页浏览，可直接编辑或删除单条记录。
分类统计
通过饼图直观展示收入和支出的分类占比，帮助你了解资金流向。

项目结构 📁
yang-account1.0/
├── .idea/                      # IDEA 配置文件
├── src/
│   └── main/
│       └── java/
│           └── cn.lazyaccount/
│               ├── config/
│               │   └── DatabaseConfig.java      # 数据库配置
│               ├── controller/
│               │   ├── BillController.java      # 账单控制器
│               │   └── UserController.java      # 用户控制器
│               ├── dao/
│               │   ├── BaseDao.java             # 数据访问基类
│               │   ├── BillDao.java             # 账单数据访问
│               │   ├── CategoryDao.java         # 分类数据访问
│               │   └── UserDao.java             # 用户数据访问
│               ├── model/
│               │   ├── Bill.java                # 账单实体
│               │   ├── Category.java            # 分类实体
│               │   └── User.java                # 用户实体
│               ├── server/
│               │   └── SimpleHttpServer.java    # 简易HTTP服务器
│               ├── service/
│               │   ├── BillService.java         # 账单业务逻辑
│               │   └── UserService.java         # 用户业务逻辑
│               └── util/
│                   ├── JsonUtil.java            # JSON工具类
│                   └── WebUtil.java             # Web工具类
│       └── resources/
│           └── init_database.sql                # 数据库初始化脚本
├── test/                             # 测试目录
├── target/                           # 编译输出目录
├── webapp/
│   ├── css/                          # 样式文件
│   ├── images/                       # 图片资源
│   ├── js/
│   │   ├── alert.js                  # 弹窗提示
│   │   ├── api.js                    # API请求封装
│   │   ├── auth.js                   # 认证相关
│   │   ├── charts.js                 # 图表绘制
│   │   ├── config.js                 # 前端配置
│   │   ├── main.js                   # 主页面逻辑
│   │   ├── pagination.js             # 分页功能
│   │   └── search.js                 # 搜索功能
│   ├── index.html                    # 首页
│   ├── main.html                     # 记账主页面
│   └── register.html                 # 注册页面
├── account_book.db                   # SQLite数据库文件
├── .gitignore                        # Git忽略文件
└── pom.xml                           # Maven项目配置

贡献🤝
欢迎提交 Issue 和 Pull Request 来完善这个项目！

许可证 📄
MIT License

作者 👨‍💻
smart-sheep-sb
