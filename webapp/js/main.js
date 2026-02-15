//主入口文件
// ========== 核心功能 ==========

// 加载数据
function loadData() {// 定义加载数据函数
    if (!currentUser) return;// 如果当前用户不存在，直接返回

    const storageKey = currentUser.username === 'guest'// 根据用户名确定存储键名
        ? 'guestAccountingData'// 访客用户使用通用键名
        : `accountingData_${currentUser.username}`;// 注册用户使用带用户名的键名

    const data = localStorage.getItem(storageKey);// 从本地存储获取数据
    records = data ? JSON.parse(data) : [];// 如果有数据则解析为对象数组，否则设为空数组
    currentPage = 1;// 重置当前页码为第1页
    isSearching = false;// 重置搜索状态为false
    searchKeyword = '';// 清空搜索关键字

    loadAlertSettings();// 加载预警设置
    updateDisplay(); // 更新页面显示
}

// 保存数据
function saveData() { // 定义保存数据函数
    if (!currentUser) return;// 如果当前用户不存在，直接返回

    const storageKey = currentUser.username === 'guest'// 根据用户名确定存储键名
        ? 'guestAccountingData'// 访客用户使用通用键名
        : `accountingData_${currentUser.username}`;// 注册用户使用带用户名的键名

    localStorage.setItem(storageKey, JSON.stringify(records));// 将records数组转换为JSON字符串并保存到本地存储
}

// 更新显示  // 更新页面上的所有显示内容
function updateDisplay() {  // 定义更新显示函数
    updateStatistics();  // 更新统计卡片
    updateRecordList();  // 更新记录列表
    updateIncomeChart();  // 更新收入图表
    updateExpenseChart();  // 更新支出图表
    checkMonthlyAlert();  // 检查月度预警
}

// 更新统计  // 计算并更新总收入、总支出和结余
function updateStatistics() {  // 定义更新统计函数
    let totalIncome = 0;  // 初始化总收入为0
    let totalExpense = 0;  // 初始化总支出为0

    records.forEach(record => {  // 遍历所有记录
        if (record.type === 'income') {  // 如果是收入类型
            totalIncome += record.amount;  // 累加到总收入
        } else {  // 如果是支出类型
            totalExpense += record.amount;  // 累加到总支出
        }
    });

    const totalBalance = totalIncome - totalExpense;  // 计算结余

    document.getElementById('totalIncome').textContent = `¥ ${totalIncome.toFixed(2)}`;  // 更新总收入显示，保留两位小数
    document.getElementById('totalExpense').textContent = `¥ ${totalExpense.toFixed(2)}`;  // 更新总支出显示
    document.getElementById('totalBalance').textContent = `¥ ${totalBalance.toFixed(2)}`;  // 更新结余显示
}

// 添加记录  // 添加新的记账记录
function addRecord() {  // 定义添加记录函数
    const type = document.getElementById('recordType').value;  // 获取记录类型（收入/支出）
    const amount = parseFloat(document.getElementById('amount').value);  // 获取金额并转换为浮点数
    const category = document.getElementById('category').value;  // 获取类别
    const date = document.getElementById('recordDate').value;  // 获取日期
    const description = document.getElementById('description').value.trim();  // 获取描述并去除首尾空格

    if (!amount || amount <= 0) {  // 如果金额不存在或小于等于0
        alert('请输入有效的金额');  // 弹出提示
        return;  // 直接返回，不添加记录
    }

    if (!date) {  // 如果日期不存在
        alert('请选择日期');  // 弹出提示
        return;  // 直接返回
    }

    const record = {  // 创建新记录对象
        id: Date.now(),  // 使用当前时间戳作为唯一ID
        type: type,  // 记录类型
        amount: amount,  // 金额
        category: category,  // 类别
        date: date,  // 日期
        description: description,  // 描述
        timestamp: new Date().toISOString()  // 创建时间戳
    };

    records.unshift(record);  // 将新记录添加到数组开头（最新的显示在最前面）
    saveData();  // 保存数据到本地存储

    document.getElementById('amount').value = '';  // 清空金额输入框
    document.getElementById('description').value = '';  // 清空描述输入框

    if (isSearching) {  // 如果当前处于搜索状态
        performSearch();  // 重新执行搜索，更新搜索结果
    } else {  // 如果不是搜索状态
        updateDisplay();  // 更新页面显示
    }

    alert('记录添加成功！');  // 弹出成功提示
}

// 删除记录  // 根据ID删除记账记录
function deleteRecord(id) {  // 定义删除记录函数，接收记录ID作为参数
    if (!confirm('确定要删除这条记录吗？')) return;  // 弹出确认对话框，如果用户取消则直接返回

    records = records.filter(record => record.id !== id);  // 从records数组中过滤掉指定ID的记录

    if (isSearching && !useDatabaseSearch) {  // 如果处于搜索状态且不是数据库搜索
        filteredRecords = filteredRecords.filter(record => record.id !== id);  // 同时从搜索结果中过滤
    }

    const displayRecords = isSearching ? filteredRecords : records;  // 获取当前显示的记录（搜索或全部）
    const totalPages = Math.ceil(displayRecords.length / CONFIG.RECORDS_PER_PAGE);  // 计算总页数
    if (currentPage > totalPages && currentPage > 1) {  // 如果当前页码大于总页数且大于1
        currentPage = totalPages;  // 将当前页码设置为总页数
    }

    saveData();  // 保存数据到本地存储
    updateDisplay();  // 更新页面显示
}

// 打开编辑弹窗  // 打开编辑记录的模态框
function openEditModal(recordId) {  // 定义打开编辑弹窗函数，接收记录ID作为参数
    const record = records.find(r => r.id === recordId);  // 在records数组中查找指定ID的记录
    if (!record) return;  // 如果记录不存在，直接返回

    document.getElementById('editRecordId').value = record.id;  // 设置编辑表单中的记录ID
    document.getElementById('editRecordType').value = record.type;  // 设置编辑表单中的记录类型
    document.getElementById('editAmount').value = record.amount;  // 设置编辑表单中的金额
    document.getElementById('editCategory').value = record.category;  // 设置编辑表单中的类别
    document.getElementById('editRecordDate').value = record.date;  // 设置编辑表单中的日期
    document.getElementById('editDescription').value = record.description || '';  // 设置编辑表单中的描述

    document.getElementById('editModal').style.display = 'flex';  // 显示编辑模态框
}

// 关闭编辑弹窗  // 关闭编辑记录的模态框
function closeEditModal() {  // 定义关闭编辑弹窗函数
    document.getElementById('editModal').style.display = 'none';  // 隐藏编辑模态框
    document.getElementById('editRecordId').value = '';  // 清空记录ID输入框
    document.getElementById('editAmount').value = '';  // 清空金额输入框
    document.getElementById('editDescription').value = '';  // 清空描述输入框
}

// 保存编辑记录  // 保存编辑后的记录
function saveEditRecord() {  // 定义保存编辑记录函数
    const recordId = parseInt(document.getElementById('editRecordId').value);  // 获取编辑表单中的记录ID并转换为整数
    const type = document.getElementById('editRecordType').value;  // 获取编辑后的记录类型
    const amount = parseFloat(document.getElementById('editAmount').value);  // 获取编辑后的金额并转换为浮点数
    const category = document.getElementById('editCategory').value;  // 获取编辑后的类别
    const date = document.getElementById('editRecordDate').value;  // 获取编辑后的日期
    const description = document.getElementById('editDescription').value.trim();  // 获取编辑后的描述

    if (!amount || amount <= 0) {  // 如果金额无效
        alert('请输入有效的金额');  // 弹出提示
        return;  // 直接返回
    }
    if (!date) {  // 如果日期无效
        alert('请选择日期');  // 弹出提示
        return;  // 直接返回
    }

    const recordIndex = records.findIndex(r => r.id === recordId);  // 查找记录在数组中的索引
    if (recordIndex !== -1) {  // 如果找到了记录
        records[recordIndex] = {  // 更新该索引位置的记录
            ...records[recordIndex],  // 保留原有记录的id和timestamp等属性
            type,  // 更新类型
            amount,  // 更新金额
            category,  // 更新类别
            date,  // 更新日期
            description  // 更新描述
        };

        saveData();  // 保存数据到本地存储
        records.sort((a, b) => new Date(b.date) - new Date(a.date));  // 按日期降序排序（最新的在前）
        closeEditModal();  // 关闭编辑模态框

        if (isSearching) {  // 如果当前处于搜索状态
            performSearch();  // 重新执行搜索
        } else {  // 如果不是搜索状态
            updateDisplay();  // 更新页面显示
        }

        alert('记录修改成功！');  // 弹出成功提示
    } else {  // 如果未找到记录
        alert('未找到要编辑的记录');  // 弹出错误提示
    }
}

// 更新记录列表  // 根据当前状态更新记录列表显示
function updateRecordList() {  // 定义更新记录列表函数
    const container = document.getElementById('recordsContainer');  // 获取记录列表容器元素
    const pageInfo = document.getElementById('pageInfo');  // 获取页码信息元素
    const prevBtn = document.getElementById('prevBtn');  // 获取上一页按钮
    const nextBtn = document.getElementById('nextBtn');  // 获取下一页按钮

    let displayRecords = isSearching ? filteredRecords : records;  // 确定要显示的记录（搜索结果或全部记录）

    if (displayRecords.length === 0) {  // 如果没有要显示的记录
        if (isSearching && records.length > 0) {  // 如果是搜索状态且有原始记录
            const searchMode = document.getElementById('searchMode').value;  // 获取搜索模式
            const modeText = searchMode === 'database' ? '数据库' : '本地';  // 根据模式确定显示文本

            container.innerHTML = `  
                <div class="empty-state">
                    <i class="fas fa-search" style="font-size: 48px; margin-bottom: 15px;"></i>
                    <p>${modeText}搜索未找到包含"${searchKeyword}"的记录</p>  
                    <button class="btn" onclick="clearSearch()" style="margin-top: 10px; padding: 8px 16px; background: #eee; color: #333;">
                        <i class="fas fa-times"></i> 清除搜索  
                    </button>
                </div>
            `;
            pageInfo.textContent = `${modeText}搜索: "${searchKeyword}"`;  // 更新页码信息
        } else {  // 如果没有记录且不是搜索状态
            container.innerHTML = `  
                <div class="empty-state">
                    <i class="fas fa-inbox" style="font-size: 48px; margin-bottom: 15px;"></i>
                    <p>还没有记录，添加第一条记录吧！</p>
                </div>
            `;
            pageInfo.textContent = '第 1 页';  // 更新页码信息
        }
        prevBtn.disabled = true;  // 禁用上一页按钮
        nextBtn.disabled = true;  // 禁用下一页按钮
        return;  // 直接返回
    }

    const totalPages = Math.ceil(displayRecords.length / CONFIG.RECORDS_PER_PAGE);  // 计算总页数
    if (currentPage > totalPages) currentPage = totalPages;  // 如果当前页码大于总页数，设置为总页数
    if (currentPage < 1) currentPage = 1;  // 如果当前页码小于1，设置为1

    const startIndex = (currentPage - 1) * CONFIG.RECORDS_PER_PAGE;  // 计算当前页起始索引
    const endIndex = Math.min(startIndex + CONFIG.RECORDS_PER_PAGE, displayRecords.length);  // 计算当前页结束索引
    const pageRecords = displayRecords.slice(startIndex, endIndex);  // 获取当前页要显示的记录

    if (isSearching) {  // 如果是搜索状态
        const searchMode = document.getElementById('searchMode').value;  // 获取搜索模式
        const modeText = searchMode === 'database' ? '数据库' : '本地';  // 根据模式确定显示文本
        pageInfo.textContent = `${modeText}搜索: "${searchKeyword}" (${displayRecords.length}条)`;  // 显示搜索信息和结果数量
    } else {  // 如果不是搜索状态
        pageInfo.textContent = `第 ${currentPage} / ${totalPages} 页`;  // 显示当前页码和总页数
    }

    prevBtn.disabled = currentPage === 1;  // 如果在第一页，禁用上一页按钮
    nextBtn.disabled = currentPage === totalPages;  // 如果在最后一页，禁用下一页按钮

    const categoryNames = {  // 定义类别键值到中文名称的映射
        'food': '餐饮', 'transport': '交通', 'shopping': '购物',
        'housing': '住房', 'entertainment': '娱乐', 'salary': '工资',
        'investment': '投资', 'other': '其他'
    };

    let html = '';  // 初始化HTML字符串
    pageRecords.forEach(record => {  // 遍历当前页的记录
        const category = categoryNames[record.category] || record.category;  // 获取类别中文名称
        const typeClass = record.type === 'income' ? 'income-type' : 'expense-type';  // 根据类型确定CSS类
        const typeSign = record.type === 'income' ? '+' : '-';  // 根据类型确定符号
        const amountColor = record.type === 'income' ? '#4CAF50' : '#F44336';  // 根据类型确定金额颜色

        let descriptionHtml = record.description || '';  // 初始化描述HTML
        let categoryHtml = category;  // 初始化类别HTML

        if (isSearching && searchKeyword) {  // 如果是搜索状态且有搜索关键字
            const regex = new RegExp(`(${escapeRegExp(searchKeyword)})`, 'gi');  // 创建正则表达式，转义特殊字符
            if (record.description) {  // 如果记录有描述
                descriptionHtml = record.description.replace(regex, '<span class="search-highlight">$1</span>');  // 高亮显示描述中的关键字
            }
            categoryHtml = category.replace(regex, '<span class="search-highlight">$1</span>');  // 高亮显示类别中的关键字
        }

        html += `  
            <div class="record-item">
                <div class="record-type ${typeClass}">
                    ${record.type === 'income' ? '收' : '支'}  
                </div>
                <div class="record-info">
                    <div class="record-category">${categoryHtml}</div>  
                    <div class="record-meta">
                        ${record.date} ${descriptionHtml ? '· ' + descriptionHtml : ''}  
                        ${useDatabaseSearch ? '<span style="color:#888;font-size:11px;margin-left:5px;">[数据库]</span>' : ''}  
                    </div>
                </div>
                <div class="record-amount" style="color: ${amountColor}">
                    ${typeSign}¥ ${record.amount.toFixed(2)}  
                </div>
                <button class="edit-btn" onclick="openEditModal(${record.id})" title="编辑记录">  
                    <i class="fas fa-edit"></i>
                </button>
                <button class="delete-btn" onclick="${useDatabaseSearch ? `deleteRecordFromDB(${record.id})` : `deleteRecord(${record.id})`}" title="删除记录">  
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `;
    });

    html += `  
        <div class="pagination-footer" style="padding: 15px 20px; text-align: center; border-top: 1px solid #eee; color: #666; font-size: 14px;">
            显示 ${startIndex + 1}-${endIndex} 条，共 ${displayRecords.length} 条记录  
            ${isSearching ? `<button class="clear-search-btn" onclick="clearSearch()" style="margin-left: 10px;">  
                <i class="fas fa-times"></i> 清除搜索
            </button>` : ''}
        </div>
    `;

    container.innerHTML = html;  // 将生成的HTML设置到容器中
}

// 初始化弹窗事件  // 初始化模态框的事件处理
function initModalEvents() {  // 定义初始化弹窗事件函数
    const modals = ['editModal', 'alertModal'];  // 定义需要初始化的模态框ID数组
    modals.forEach(modalId => {  // 遍历每个模态框
        document.getElementById(modalId).addEventListener('click', function(e) {  // 为模态框添加点击事件监听
            if (e.target === this) {  // 如果点击的是模态框背景本身
                if (modalId === 'editModal') closeEditModal();  // 如果是编辑模态框，调用关闭编辑弹窗函数
                if (modalId === 'alertModal') closeAlertModal();  // 如果是预警模态框，调用关闭预警弹窗函数
            }
        });
    });

    document.addEventListener('keydown', function(e) {  // 添加键盘事件监听
        if (e.key === 'Escape') {  // 如果按下了ESC键
            if (document.getElementById('editModal').style.display === 'flex') {  // 如果编辑模态框显示中
                closeEditModal();  // 关闭编辑模态框
            }
            if (document.getElementById('alertModal').style.display === 'flex') {  // 如果预警模态框显示中
                closeAlertModal();  // 关闭预警模态框
            }
        }
    });
}

// 页面初始化  // DOM加载完成后执行的初始化函数
document.addEventListener('DOMContentLoaded', function() {  // 添加DOM内容加载完成事件监听
    const today = new Date().toISOString().split('T')[0];  // 获取今天的日期（YYYY-MM-DD格式）
    document.getElementById('recordDate').value = today;  // 设置日期输入框的默认值为今天

    if (checkLoginStatus()) {  // 检查登录状态，如果已登录
        loadData();  // 加载用户数据
        initModalEvents();  // 初始化弹窗事件
    }
});