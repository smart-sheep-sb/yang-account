//分页功能
// ========== 分页模块 ==========
let currentPage = 1; // 定义当前页码变量，初始值为第1页

// 上一页
function prevPage() { // 定义上一页函数
    if (currentPage > 1) {// 如果当前页码大于1（即不是第一页）
        currentPage--;// 当前页码减1
        updateRecordList();// 更新记录列表显示
    }
}

// 下一页
function nextPage() {// 定义下一页函数
    const displayRecords = isSearching ? filteredRecords : records;// 获取当前显示的记录（搜索结果或全部记录）
    const totalPages = Math.ceil(displayRecords.length / CONFIG.RECORDS_PER_PAGE);// 计算总页数（向上取整）
    if (currentPage < totalPages) {// 如果当前页码小于总页数（即不是最后一页）
        currentPage++;// 当前页码加1
        updateRecordList();// 更新记录列表显示
    }
}