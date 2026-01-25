const fs = require('fs');

const filePath = 'shangnantea-web/src/views/tea/manage/TeaManagePage.vue';
const content = fs.readFileSync(filePath, 'utf8');

// 查找所有TODO行
const lines = content.split('\n');
const todoLines = [];

lines.forEach((line, index) => {
  if (line.includes('TODO') && line.includes('迁移')) {
    todoLines.push({
      lineNumber: index + 1,
      content: line.trim()
    });
  }
});

console.log(`文件: ${filePath}`);
console.log(`总行数: ${lines.length}`);
console.log(`找到 ${todoLines.length} 个TODO标记:\n`);

todoLines.slice(0, 5).forEach(item => {
  console.log(`行 ${item.lineNumber}: ${item.content}`);
});

// 查找旧消息调用
const oldMessageLines = [];
lines.forEach((line, index) => {
  if (line.match(/teaMessages\.(success|error)\.show/)) {
    oldMessageLines.push({
      lineNumber: index + 1,
      content: line.trim()
    });
  }
});

console.log(`\n找到 ${oldMessageLines.length} 个旧消息调用:\n`);
oldMessageLines.slice(0, 5).forEach(item => {
  console.log(`行 ${item.lineNumber}: ${item.content}`);
});
