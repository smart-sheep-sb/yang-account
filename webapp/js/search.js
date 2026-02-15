//搜索功能
// ========== 搜索模块 ==========
let isSearching = false; // 定义是否处于搜索状态标志，默认为false
let searchKeyword = '';// 定义搜索关键字变量，默认为空字符串
let filteredRecords = [];// 定义过滤后的记录数组，用于存储本地搜索结果
let databaseSearchResults = [];// 定义数据库搜索结果数组，用于存储从数据库查询到的结果
let useDatabaseSearch = false;// 定义是否使用数据库搜索标志，默认为false

// 转义正则表达式
function escapeRegExp(string) {// 定义转义正则表达式函数，接收字符串参数
    return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');// 使用正则表达式匹配特殊字符，并在前面加反斜杠转义
}

// 处理搜索输入
function handleSearchInput(event) {// 定义处理搜索输入函数，接收事件对象参数
    if (event.key === 'Enter') {// 如果按下的键是回车键
        performSearch();// 执行搜索
    } else if (event.target.value.trim() === '') {// 如果输入框的值去除空格后为空
        clearSearch();// 清除搜索
    }
}

// 处理搜索框焦点
function handleSearchFocus(input) {// 定义处理搜索框焦点函数，接收输入框元素参数
    input.style.width = '200px';// 设置输入框宽度为200px
    input.style.background = 'white';// 设置输入框背景为白色
    input.style.borderColor = '#4CAF50';// 设置输入框边框颜色为绿色
    document.getElementById('searchStatus').style.display = 'block';// 显示搜索状态提示元素
}

// 处理搜索框失焦
function handleSearchBlur(input) {// 定义处理搜索框失焦函数，接收输入框元素参数
    input.style.width = '160px';// 设置输入框宽度为160px
    input.style.background = '#f9f9f9';// 设置输入框背景为浅灰色
    input.style.borderColor = '#ddd';// 设置输入框边框颜色为灰色
    document.getElementById('searchStatus').style.display = 'none';// 隐藏搜索状态提示元素
}

// 执行搜索
async function performSearch() {// 定义异步执行搜索函数
    const searchTerm = document.getElementById('searchInput').value.trim();// 获取搜索输入框的值并去除空格
    const searchMode = document.getElementById('searchMode').value;// 获取搜索模式选择器的值

    if (!searchTerm) {// 如果搜索词为空
        clearSearch();// 清除搜索
        return;// 直接返回
    }

    isSearching = true;// 设置搜索状态为true
    searchKeyword = searchTerm;// 将搜索词保存到全局变量
    useDatabaseSearch = (searchMode === 'database');// 根据选择的模式设置是否使用数据库搜索

    document.getElementById('recordsContainer').innerHTML = `
        <div class="loading">
            <div class="loading-spinner"></div>
            <p>${useDatabaseSearch ? '正在从数据库搜索...' : '正在搜索...'}</p>
        </div>
    `;

    if (useDatabaseSearch) {// 如果使用数据库搜索
        await performDatabaseSearch(searchTerm);// 异步执行数据库搜索
    } else {// 如果不使用数据库搜索
        performLocalSearch(searchTerm);// 执行本地搜索
    }

    currentPage = 1;// 将当前页码重置为第1页
    updateRecordList();// 更新记录列表显示
}

// 执行本地搜索
function performLocalSearch(keyword) {// 定义执行本地搜索函数，接收关键字参数
    filteredRecords = records.filter(record => {// 过滤records数组，返回匹配的记录
        const categoryNames = {// 定义类别键值到中文名称的映射
            'food': '餐饮', 'transport': '交通', 'shopping': '购物',
            'housing': '住房', 'entertainment': '娱乐', 'salary': '工资',
            'investment': '投资', 'other': '其他'
        };
        const category = categoryNames[record.category] || record.category;// 获取类别中文名称

        return (// 返回布尔值，表示是否匹配
            (record.description && record.description.toLowerCase().includes(keyword.toLowerCase())) ||// 描述中包含关键字（不区分大小写）
            category.toLowerCase().includes(keyword.toLowerCase()) ||// 类别中包含关键字
            record.amount.toString().includes(keyword) ||// 金额字符串中包含关键字
            record.date.includes(keyword) ||// 日期中包含关键字
            (record.type === 'income' && '收入'.includes(keyword)) ||// 如果是收入类型且关键字包含'收入'
            (record.type === 'expense' && '支出'.includes(keyword)) // 如果是支出类型且关键字包含'支出'
        );
    });
}

// 清除搜索
function clearSearch() {// 定义清除搜索函数
    document.getElementById('searchInput').value = '';// 清空搜索输入框的值
    isSearching = false; // 设置搜索状态为false
    searchKeyword = '';// 清空搜索关键字
    filteredRecords = [];// 清空过滤后的记录数组
    databaseSearchResults = [];// 清空数据库搜索结果数组
    useDatabaseSearch = false;// 设置数据库搜索标志为false
    currentPage = 1;// 将当前页码重置为第1页
    updateDisplay();// 更新页面显示
}