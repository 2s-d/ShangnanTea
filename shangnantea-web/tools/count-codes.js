const fs = require('fs');
const path = require('path');

const content = fs.readFileSync(path.join(__dirname, '../docs/tasks/code-message-mapping.md'), 'utf8');
const lines = content.split('\n');

const allCodes = [];
const codeMap = new Map();
const silentCodes = new Set();
let inTable = false;

for (let i = 46; i < 1294; i++) {
  const line = lines[i].trim();
  if (line.includes('| Code |')) {
    inTable = true;
    continue;
  }
  if (inTable && line.startsWith('|') && line.includes('|')) {
    const parts = line.split('|').map(p => p.trim()).filter(p => p);
    if (parts.length >= 2 && /^\d+$/.test(parts[0])) {
      const code = parseInt(parts[0]);
      const message = parts[1];
      const isSilent = parts[3] && parts[3].includes('静默');
      
      allCodes.push(code);
      if (!codeMap.has(code)) {
        codeMap.set(code, message);
      }
      if (isSilent) {
        silentCodes.add(code);
      }
    }
  }
  if (line === '' && inTable) {
    inTable = false;
  }
}

console.log('=== 统计结果 ===');
console.log('状态码总出现次数（含重复）:', allCodes.length);
console.log('去重后状态码数:', codeMap.size);
console.log('静默状态码数:', silentCodes.size);
console.log('\n=== 去重后状态码列表（按数字排序）===');
const sortedCodes = Array.from(codeMap.entries()).sort((a, b) => a[0] - b[0]);
sortedCodes.forEach(([code, msg]) => {
  console.log(`${code}: '${msg}'`);
});
console.log('\n=== 静默状态码列表 ===');
Array.from(silentCodes).sort((a, b) => a - b).forEach(code => {
  console.log(code);
});

