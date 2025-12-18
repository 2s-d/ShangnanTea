# 测试文件夹

## 文档说明

本目录存放所有测试相关的文件和文档。

**创建日期**: 2024-12-17

---

## 目录结构

```
tests/
├── test-cases/              # 测试用例文件夹
│   └── [模块名]/           # 各模块的测试用例
│       └── [功能名].spec.js
├── test-case-templates/    # 测试用例模板文件夹
│   └── README.md           # 测试用例模板与规范（待编写）
└── test-data-templates/    # 测试数据模板文件夹
    └── README.md           # 清除修改伪代码的经验和写后端测试数据的经验（待更新）
```

---

## 目录说明

### test-cases/ - 测试用例文件夹

存放所有测试用例文件。

- **位置**：`tests/test-cases/[模块名]/[功能名].spec.js`
- **编写规范**：必须依照 `test-case-templates/` 目录下的模板编写
- **参考文档**：`test-case-templates/README.md`

### test-case-templates/ - 测试用例模板文件夹

存放测试用例的规范与模板。

- **位置**：`tests/test-case-templates/`
- **状态**：待编写
- **内容**：测试用例模板文件和编写规范

### test-data-templates/ - 测试数据模板文件夹

存放测试数据编写的经验文档。

- **位置**：`tests/test-data-templates/README.md`
- **状态**：待更新
- **内容**：
  - 清除修改伪代码的经验
  - 写后端测试数据的经验

---

## 使用流程

1. **编写测试用例**：
   - 查看 `test-case-templates/README.md` 了解编写规范
   - 选择合适的模板
   - 编写测试用例到 `test-cases/[模块名]/[功能名].spec.js`

2. **编写测试数据**：
   - 查看 `test-data-templates/README.md` 了解编写经验
   - 在后端Controller中编写测试数据

3. **执行测试**：
   - 执行 `test-cases/` 目录下的测试用例

---

## 相关文档

- **测试方案文档**：`docs/tasks/00-testing-standard.md`
- **任务分解文档**：`docs/tasks/0X-[模块名]-module-tasks.md`

---

**文档版本**: 0.1  
**创建日期**: 2024-12-17

