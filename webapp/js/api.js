//API接口封装
// ========== API接口封装 ==========

// 从数据库删除记录
async function deleteRecordFromDB(recordId) {// 定义异步函数，接收记录ID作为参数
    if (!confirm('确定要从数据库删除这条记录吗？')) return;// 弹出确认对话框，如果用户取消则直接返回

    try {// 尝试执行以下代码，捕获可能的错误
        const response = await fetch(`${CONFIG.API_BASE_URL}/bill/${recordId}`, {// 发送DELETE请求到指定API地址
            method: 'DELETE'// 设置请求方法为DELETE
        });

        if (!response.ok) {// 如果响应状态码不是2xx
            throw new Error(`HTTP ${response.status}`);// 抛出错误，包含HTTP状态码
        }

        const data = await response.json();// 解析响应JSON数据

        if (data.success) {// 如果API返回成功标志
            alert('数据库记录删除成功！');// 弹出成功提示
            if (isSearching && useDatabaseSearch) {// 如果当前处于搜索状态且使用数据库搜索
                await performDatabaseSearch(searchKeyword);// 重新执行数据库搜索，更新搜索结果
                updateRecordList();// 更新记录列表显示
            } else {// 如果不是搜索状态
                loadData();// 重新加载所有数据
            }
        } else {// 如果API返回失败
            alert('数据库删除失败: ' + data.message);// 弹出失败提示，显示API返回的错误信息
        }
    } catch (error) {// 捕获try块中的错误
        console.error('数据库删除错误:', error);// 在控制台输出错误信息
        alert('数据库删除过程中发生错误: ' + error.message);// 弹出错误提示
    }
}

// 执行数据库搜索
async function performDatabaseSearch(keyword) {// 定义异步函数，接收搜索关键字作为参数
    try {// 尝试执行以下代码
        if (!currentUserId) {// 如果当前用户ID不存在
            alert('用户ID不存在，无法进行数据库搜索'); // 弹出错误提示
            isSearching = false;// 将搜索状态设置为false
            updateDisplay();// 更新显示
            return;// 直接返回
        }

        const response = await fetch(`${CONFIG.API_BASE_URL}/bill/search`, {// 发送POST请求到搜索API
            method: 'POST',// 设置请求方法为POST
            headers: {// 设置请求头
                'Content-Type': 'application/json',// 内容类型为JSON
            },
            body: JSON.stringify({// 将请求体转换为JSON字符串
                userId: currentUserId,// 用户ID
                keyword: keyword // 搜索关键字
            })
        });

        if (!response.ok) {// 如果响应状态码不是2xx
            throw new Error(`HTTP ${response.status}`);// 抛出错误
        }

        const data = await response.json();// 解析响应JSON

        if (data.success) {// 如果API返回成功
            databaseSearchResults = data.data || [];// 保存数据库搜索结果，如果没有数据则设为空数组
            filteredRecords = convertDatabaseRecords(databaseSearchResults);// 转换数据库记录为前端格式并保存到过滤后的记录
        } else {// 如果API返回失败
            alert('数据库搜索失败: ' + data.message);// 弹出失败提示
            isSearching = false;// 搜索状态设为false
            updateDisplay(); // 更新显示
        }
    } catch (error) {// 捕获错误
        console.error('数据库搜索错误:', error);// 控制台输出错误
        alert('数据库搜索过程中发生错误: ' + error.message);// 弹出错误提示
        isSearching = false; // 搜索状态设为false
        updateDisplay();// 更新显示
    }
}

// 转换数据库记录为前端格式
function convertDatabaseRecords(dbRecords) {// 定义转换函数，接收数据库记录数组作为参数
    const categoryMap = {// 定义类别映射，将数据库类别名称映射为前端类别键值
        '餐饮': 'food', '交通': 'transport', '购物': 'shopping',
        '住房': 'housing', '娱乐': 'entertainment', '工资': 'salary',
        '奖金': 'salary', '兼职': 'investment', '投资': 'investment',
        '其他': 'other'
    };// 支出类别映射,支出和收入类别映射,收入类别映射,其他类别映射

    return dbRecords.map(dbRecord => {// 遍历数据库记录数组并返回新数组
        const categoryName = dbRecord.category_name || '其他';// 获取类别名称，如果没有则默认为'其他'
        const categoryKey = categoryMap[categoryName] || 'other';// 根据类别名称获取映射的键值，如果没有匹配则使用'other'
        const type = dbRecord.category_type === 1 ? 'income' : 'expense';// 根据类别类型判断是收入(1)还是支出

        let dateStr = '';// 初始化日期字符串
        if (dbRecord.bill_time) {// 如果账单时间存在
            const billDate = new Date(dbRecord.bill_time);// 将账单时间字符串转换为Date对象
            dateStr = billDate.toISOString().split('T')[0];// 转换为ISO格式并只取日期部分（YYYY-MM-DD）
        }

        return {// 返回转换后的记录对象
            id: dbRecord.id,// 记录ID
            type: type,// 记录类型（收入/支出）
            amount: dbRecord.amount || 0,// 金额，如果没有则为0
            category: categoryKey, // 类别键值
            date: dateStr,// 日期字符串
            description: dbRecord.remark || '',// 描述/备注，如果没有则为空字符串
            timestamp: dbRecord.create_time || new Date().toISOString(),// 创建时间戳，如果没有则使用当前时间
            dbRecord: dbRecord// 保存原始数据库记录，以备后用
        };
    });
}