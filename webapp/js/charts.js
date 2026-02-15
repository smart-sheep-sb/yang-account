//图表相关
// ========== 图表模块 ==========
let incomeChartInstance = null;// 收入图表实例变量，用于保存Chart.js实例，初始为null
let expenseChartInstance = null;// 支出图表实例变量，用于保存Chart.js实例，初始为null

// 更新收入图表
function updateIncomeChart() {// 定义更新收入图表函数
    const ctx = document.getElementById('incomeChart').getContext('2d');// 获取收入图表的Canvas上下文

    const categoryTotals = {};// 初始化空对象，用于存储各收入类别的总额
    records.forEach(record => {// 遍历所有记账记录
        if (record.type === 'income') {// 如果是收入类型
            categoryTotals[record.category] = (categoryTotals[record.category] || 0) + record.amount;// 累加该类别的收入金额
        }
    });

    const categories = Object.keys(categoryTotals);// 获取所有收入类别名称数组
    const amounts = Object.values(categoryTotals);// 获取所有收入类别金额数组

    if (categories.length === 0) {// 如果没有收入数据
        document.getElementById('incomeChart').parentElement.innerHTML = `
            <h2 style="margin-bottom: 20px;">收入分类统计</h2>
            <div class="empty-state" style="padding: 20px;">
                <p>暂无收入数据</p>
            </div>
        `;
        return;// 直接返回，不创建图表
    }

    if (incomeChartInstance) incomeChartInstance.destroy();// 如果已存在收入图表实例，则销毁它，避免内存泄漏

    incomeChartInstance = new Chart(ctx, {// 创建新的Chart.js图表实例并赋值给incomeChartInstance
        type: 'pie',// 图表类型为饼图
        data: {// 图表数据
            labels: categories.map(cat => ({// 将类别键值映射为中文显示名称
                'food': '餐饮', 'transport': '交通', 'shopping': '购物',// 支出类别映射
                'housing': '住房', 'entertainment': '娱乐', 'salary': '工资',// 支出和收入类别映射
                'investment': '投资', 'other': '其他'// 收入和其他类别映射
            })[cat] || cat),// 如果映射不到则使用原值
            datasets: [{// 数据集
                data: amounts,// 各分类的金额数据
                backgroundColor: [// 各分类对应的背景颜色数组
                    '#4CAF50', '#2196F3', '#FF9800', '#9C27B0',// 绿色、蓝色、橙色、紫色
                    '#00BCD4', '#FFC107', '#795548', '#607D8B'// 青色、黄色、棕色、蓝灰色
                ]
            }]
        },
        options: {// 图表配置选项
            responsive: true,// 响应式布局，适应容器大小变化
            plugins: { legend: { position: 'bottom' } }// 图例插件配置，设置图例位置在底部
        }
    });
}

// 更新支出图表
function updateExpenseChart() {// 定义更新支出图表函数
    const ctx = document.getElementById('expenseChart').getContext('2d');// 获取支出图表的Canvas上下文

    const categoryTotals = {};// 初始化空对象，用于存储各支出类别的总额
    records.forEach(record => {// 遍历所有记账记录
        if (record.type === 'expense') {// 如果是支出类型
            categoryTotals[record.category] = (categoryTotals[record.category] || 0) + record.amount;// 累加该类别的支出金额
        }
    });

    const categories = Object.keys(categoryTotals);// 获取所有支出类别名称数组
    const amounts = Object.values(categoryTotals);// 获取所有支出类别金额数组

    if (categories.length === 0) {// 如果没有支出数据
        document.getElementById('expenseChart').parentElement.innerHTML = `
            <h2 style="margin-bottom: 20px;">支出分类统计</h2>
            <div class="empty-state" style="padding: 20px;">
                <p>暂无支出数据</p>
            </div>
        `;
        return;// 直接返回，不创建图表
    }

    if (expenseChartInstance) expenseChartInstance.destroy();// 如果已存在支出图表实例，则销毁它

    expenseChartInstance = new Chart(ctx, { // 创建新的Chart.js图表实例并赋值给expenseChartInstance
        type: 'pie',// 图表类型为饼图
        data: { // 图表数据
            labels: categories.map(cat => ({// 将类别键值映射为中文显示名称
                'food': '餐饮', 'transport': '交通', 'shopping': '购物', // 支出类别映射
                'housing': '住房', 'entertainment': '娱乐', 'salary': '工资',// 支出和收入类别映射
                'investment': '投资', 'other': '其他'// 投资和其他类别映射
            })[cat] || cat),// 如果映射不到则使用原值
            datasets: [{// 数据集
                data: amounts,// 各分类的金额数据
                backgroundColor: [// 各分类对应的背景颜色数组
                    '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0',// 粉色、蓝色、黄色、青色
                    '#9966FF', '#FF9F40', '#8AC926', '#FF595E'// 紫色、橙色、亮绿色、亮红色
                ]
            }]
        },
        options: { // 图表配置选项
            responsive: true,// 响应式布局
            plugins: { legend: { position: 'bottom' } }// 图例位置在底部
        }
    });
}