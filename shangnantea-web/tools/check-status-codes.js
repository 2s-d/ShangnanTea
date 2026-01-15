const fs = require('fs');
const doc = fs.readFileSync('docs/tasks/code-message-mapping.md', 'utf8');
const lines = doc.split('\n');

// 提取上半部分接口状态码
let inTopSection = false;
let currentModule = '';
let interfaceNum = 0;
let currentInterface = '';
let interfaceCodes = {};

for (let i = 0; i < lines.length; i++) {
  const line = lines[i].trim();
  
  if (line.startsWith('### ') && line.includes('模块接口')) {
    inTopSection = true;
    const match = line.match(/[一二三四五六七]、(.+?)模块/);
    if (match) {
      currentModule = match[1];
      interfaceNum = 0;
    }
    continue;
  }
  
  if (line.startsWith('## 状态码列表')) {
    inTopSection = false;
    continue;
  }
  
  if (line.startsWith('## 接口状态码映射')) {
    break;
  }
  
  if (inTopSection && line.startsWith('#### 接口')) {
    const match = line.match(/接口(\d+):\s*(\w+)\s*-\s*(.+?)\s*\(/);
    if (match) {
      interfaceNum = parseInt(match[1]);
      currentInterface = match[2];
      const codes = [];
      let j = i + 3;
      while (j < lines.length && !lines[j].trim().startsWith('####')) {
        const codeLine = lines[j].trim();
        if (codeLine.startsWith('|') && /^\|\s*\d+\s*\|/.test(codeLine)) {
          const code = parseInt(codeLine.split('|')[1].trim());
          if (!isNaN(code)) codes.push(code);
        }
        j++;
      }
      interfaceCodes[`${currentModule}-${interfaceNum}-${currentInterface}`] = codes.sort((a, b) => a - b);
    }
  }
}

// 提取映射表状态码
let mappingStart = false;
let mappingModule = '';
let mappingNum = 0;
let mappingCodes = {};

const moduleMap = {
  '1': '用户',
  '2': '茶叶',
  '3': '店铺',
  '4': '订单',
  '5': '论坛',
  '6': '消息',
  '7': '系统通用'
};

for (let i = 0; i < lines.length; i++) {
  const line = lines[i].trim();
  
  if (line.startsWith('## 接口状态码映射')) {
    mappingStart = true;
    continue;
  }
  
  if (mappingStart && line.startsWith('## ')) {
    break;
  }
  
  if (mappingStart && line.startsWith('### ') && /^\d+\./.test(line)) {
    const match = line.match(/^(\d+)\./);
    if (match) {
      mappingModule = match[1];
      mappingNum = 0;
    }
    continue;
  }
  
  if (mappingStart && line.startsWith('|') && /^\|\s*\d+\s*\|/.test(line)) {
    const parts = line.split('|').map(p => p.trim()).filter(p => p);
    if (parts.length >= 6) {
      mappingNum = parseInt(parts[0]);
      const funcName = parts[1];
      const successCodes = parts[4].split(',').map(c => parseInt(c.trim())).filter(c => !isNaN(c));
      const failCodes = parts[5].split(',').map(c => parseInt(c.trim())).filter(c => !isNaN(c));
      const allCodes = [...successCodes, ...failCodes].sort((a, b) => a - b);
      mappingCodes[`${mappingModule}-${mappingNum}-${funcName}`] = allCodes;
    }
  }
}

// 比较状态码
const issues = [];
for (const key in interfaceCodes) {
  const [module, num, func] = key.split('-');
  const mappingKey = Object.keys(mappingCodes).find(k => {
    const [m, n, f] = k.split('-');
    return moduleMap[m] === module && parseInt(n) === parseInt(num) && f === func;
  });
  
  if (mappingKey) {
    const interfaceCodeList = interfaceCodes[key].join(',');
    const mappingCodeList = mappingCodes[mappingKey].join(',');
    if (interfaceCodeList !== mappingCodeList) {
      issues.push({
        module,
        num,
        func,
        interface: interfaceCodeList,
        mapping: mappingCodeList
      });
    }
  } else {
    issues.push({
      module,
      num,
      func,
      interface: interfaceCodes[key].join(','),
      mapping: '未找到映射'
    });
  }
}

if (issues.length > 0) {
  console.log('发现以下状态码不一致的问题:');
  issues.forEach(issue => {
    console.log(`\n${issue.module}模块 - 接口${issue.num} - ${issue.func}:`);
    console.log(`  上半部分: ${issue.interface}`);
    console.log(`  映射表: ${issue.mapping}`);
  });
  process.exit(1);
} else {
  console.log('✓ 所有接口的状态码都与映射表一致');
  console.log(`✓ 共检查了 ${Object.keys(interfaceCodes).length} 个接口`);
}

