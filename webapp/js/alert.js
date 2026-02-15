//预警功能
// ========== 预警模块 ==========
let monthlyLimit = null;// 定义月度支出上限变量，初始值为null

// 打开预警设置
function openAlertSettings() {// 定义打开预警设置函数
    document.getElementById('monthlyLimit').value = monthlyLimit || '';// 获取月度上限输入框元素，设置其值为当前上限或空字符串
    document.getElementById('alertModal').style.display = 'flex';// 获取预警模态框元素，设置显示样式为flex（显示）
}

// 关闭预警设置
function closeAlertModal() {// 定义关闭预警模态框函数
    document.getElementById('alertModal').style.display = 'none';// 获取预警模态框元素，设置显示样式为none（隐藏）
}

// 保存预警设置
function saveAlertSettings() {// 定义保存预警设置函数
    const limit = parseFloat(document.getElementById('monthlyLimit').value);// 获取月度上限输入框的值并转换为浮点数

    if (limit && limit > 0) {// 如果limit存在且大于0
        monthlyLimit = limit;// 将全局变量monthlyLimit设置为输入的limit值

        if (currentUser) {// 如果当前用户存在
            const storageKey = currentUser.username === 'guest'// 判断用户名是否为guest
                ? 'guestMonthlyLimit'// 如果是访客，使用访客专用的存储键名
                : `monthlyLimit_${currentUser.username}`;// 如果是注册用户，使用带用户名的存储键名
            localStorage.setItem(storageKey, limit.toString());// 将月度上限以字符串形式保存到本地存储
        }

        alert('预警设置已保存！');// 弹出保存成功的提示框
        closeAlertModal(); // 调用关闭预警模态框函数
        checkMonthlyAlert();// 调用检查月度预警函数，立即检查支出情况
    } else {// 如果limit不存在或不大于0
        alert('请输入有效的金额');// 弹出提示要求输入有效金额
    }
}

// 加载预警设置
function loadAlertSettings() {// 定义加载预警设置函数
    if (!currentUser) return;// 如果当前用户不存在，直接返回

    const storageKey = currentUser.username === 'guest'// 判断用户名是否为guest
        ? 'guestMonthlyLimit'// 如果是访客，使用访客专用的存储键名
        : `monthlyLimit_${currentUser.username}`;// 如果是注册用户，使用带用户名的存储键名

    const savedLimit = localStorage.getItem(storageKey);// 从本地存储获取保存的上限值
    if (savedLimit) {// 如果保存的上限存在
        monthlyLimit = parseFloat(savedLimit);// 将保存的值转换为浮点数并赋值给monthlyLimit
    }
}

// 检查月度支出
function checkMonthlyAlert() {// 定义检查月度预警函数
    if (!monthlyLimit) return;// 如果月度上限未设置，直接返回

    const currentMonth = new Date().toISOString().slice(0, 7);// 获取当前年月（格式：YYYY-MM）
    let monthlyExpense = 0;// 初始化月度支出总额为0

    records.forEach(record => {// 遍历所有记账记录
        if (record.type === 'expense' && record.date.startsWith(currentMonth)) {// 如果是支出类型且日期属于当前月份
            monthlyExpense += record.amount;// 累加支出金额
        }
    });

    const isOverLimit = monthlyExpense > monthlyLimit;// 判断是否超出上限
    const percentage = ((monthlyExpense / monthlyLimit) * 100).toFixed(1);// 计算支出占比并保留一位小数
    updateAlertDisplay(isOverLimit, monthlyExpense, percentage);// 调用更新预警显示函数
}

// 更新预警显示
function updateAlertDisplay(isOverLimit, monthlyExpense, percentage) {// 定义更新预警显示函数，接收是否超限、支出金额、百分比三个参数
    const existingAlert = document.querySelector('.alert-warning');// 查找已存在的预警警告元素
    if (existingAlert) existingAlert.remove();// 如果存在，移除该元素

    const existingBadge = document.querySelector('.alert-badge');// 查找已存在的预警徽章元素
    if (existingBadge) existingBadge.remove();// 如果存在，移除该元素

    if (!monthlyLimit) return;// 如果月度上限未设置，直接返回

    const statsGrid = document.querySelector('.stats-grid');// 获取统计卡片网格元素
    if (!statsGrid) return;// 如果统计卡片网格不存在，直接返回

    if (isOverLimit) {// 如果超出上限
        const overAmount = monthlyExpense - monthlyLimit;// 计算超出金额
        const alertHtml = `
            <div class="alert-warning" style="border-left-color: #F44336; background: #FFEBEE;">
                <div style="display: flex; align-items: center; margin-bottom: 5px;">
                    <i class="fas fa-exclamation-triangle" style="color: #F44336; margin-right: 8px;"></i>
                    <strong style="color: #F44336;">月度支出超限预警</strong>
                </div>
                <div style="font-size: 13px;">
                    本月支出：¥${monthlyExpense.toFixed(2)}<br>
                    超出上限：¥${overAmount.toFixed(2)} (${percentage}%)
                </div>
            </div>
        `;
        statsGrid.insertAdjacentHTML('afterend', alertHtml);// 在统计卡片网格之后插入预警HTML

        const alertBtn = document.querySelector('.logout-btn[title="预警设置"]');// 查找预警设置按钮
        if (alertBtn) {// 如果按钮存在
            const badge = document.createElement('span');// 创建span元素作为徽章
            badge.className = 'alert-badge'; // 设置徽章类名为alert-badge
            badge.textContent = '!';// 设置徽章文本为感叹号
            alertBtn.style.position = 'relative';// 设置按钮定位为相对定位，为徽章绝对定位提供参考
            alertBtn.appendChild(badge);// 将徽章添加到按钮中
        }
    } else if (percentage > 80) {// 如果未超限但支出超过上限的80%
        const alertHtml = `
            <div class="alert-warning">
                <div style="display: flex; align-items: center; margin-bottom: 5px;">
                    <i class="fas fa-exclamation-circle" style="color: #FF9800; margin-right: 8px;"></i>
                    <strong style="color: #FF9800;">月度支出接近上限</strong>
                </div>
                <div style="font-size: 13px;">
                    本月支出：¥${monthlyExpense.toFixed(2)}<br>
                    已达到上限的 ${percentage}%
                </div>
                <div style="margin-top: 8px; height: 6px; background: #eee; border-radius: 3px; overflow: hidden;">
                    <div style="height: 100%; background: #FF9800; width: ${percentage}%; border-radius: 3px;"></div>
                </div>
            </div>
        `;
        statsGrid.insertAdjacentHTML('afterend', alertHtml);// 在统计卡片网格之后插入预警HTML
    }
}