const fs = require('fs');
const path = require('path');

// 需要清理的文件列表
const files = [
  'shangnantea-web/src/views/tea/manage/TeaManagePage.vue',
  'shangnantea-web/src/views/tea/detail/TeaDetailPage.vue',
  'shangnantea-web/src/views/tea/list/TeaListPage.vue'
];

// 清理规则：删除TODO标记和紧随其后的旧消息调用
function cleanupFile(filePath) {
  console.log(`\n处理文件: ${filePath}`);
  
  let content = fs.readFileSync(filePath, 'utf8');
  let originalContent = content;
  let todoCount = 0;
  let oldMessageCount = 0;
  
  // 规则1: 删除TODO注释行
  const todoPattern = /\s*\/\/ TODO:.*迁移到新消息系统.*\n/g;
  content = content.replace(todoPattern, () => {
    todoCount++;
    return '';
  });
  
  // 规则2: 删除旧的teaMessages.success/error调用（但保留teaMessages.prompt调用）
  const oldMessagePattern = /\s*teaMessages\.(success|error)\.show\w+\([^)]*\)\n/g;
  content = content.replace(oldMessagePattern, () => {
    oldMessageCount++;
    return '';
  });
  
  // 规则3: 删除空行（连续超过2个空行的情况）
  content = content.replace(/\n\n\n+/g, '\n\n');
  
  if (content !== originalContent) {
    fs.writeFileSync(filePath, content, 'utf8');
    console.log(`✓ 已清理 ${todoCount} 个TODO标记`);
    console.log(`✓ 已删除 ${oldMessageCount} 个旧消息调用`);
    return { todoCount, oldMessageCount };
  } else {
    console.log('✓ 文件已经是干净的');
    return { todoCount: 0, oldMessageCount: 0 };
  }
}

// 执行清理
console.log('开始清理茶叶模块的TODO标记和旧消息调用...\n');

let totalTodos = 0;
let totalOldMessages = 0;

files.forEach(file => {
  try {
    const result = cleanupFile(file);
    totalTodos += result.todoCount;
    totalOldMessages += result.oldMessageCount;
  } catch (error) {
    console.error(`✗ 处理文件失败: ${file}`);
    console.error(error.message);
  }
});

console.log(`\n========== 清理完成 ==========`);
console.log(`总共清理了 ${totalTodos} 个TODO标记`);
console.log(`总共删除了 ${totalOldMessages} 个旧消息调用`);
console.log(`处理了 ${files.length} 个文件`);
